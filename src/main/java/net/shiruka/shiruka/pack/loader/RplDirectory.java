/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.shiruka.shiruka.pack.loader;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.pack.PackLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple directory implementation for {@link PackLoader}.
 */
@RequiredArgsConstructor
public final class RplDirectory implements PackLoader {

  /**
   * the factory instance.
   */
  public static final Factory FACTORY = new DirectoryFactory();

  /**
   * the temp files.
   */
  private static final List<Path> TEMP_FILES = new ObjectArrayList<>();

  /**
   * the location.
   */
  @NotNull
  @Getter
  private final Path location;

  /**
   * the prepared file.
   */
  @Nullable
  private CompletableFuture<Path> preparedFile;

  @Override
  public void close() {
    // ignored.
  }

  @Override
  public void forEachIn(@NotNull final Path path, final boolean recurse, @NotNull final Consumer<Path> consumer) {
    final var resolved = this.location.resolve(path);
    try (final var stream = Files.newDirectoryStream(resolved)) {
      for (final var entry : stream) {
        if (Files.isDirectory(entry) && recurse) {
          this.forEachIn(entry, true, consumer);
        } else {
          consumer.accept(this.location.relativize(entry));
        }
      }
    } catch (final IOException ignored) {
      // ignored.
    }
  }

  @NotNull
  @Override
  public Optional<InputStream> getAsset(@NotNull final Path path) throws IOException {
    return Optional.of(Files.newInputStream(this.location.resolve(path)));
  }

  @NotNull
  @Override
  public CompletableFuture<Path> getPreparedFile() {
    if (this.preparedFile == null) {
      this.preparedFile = CompletableFuture.supplyAsync(() -> {
        try {
          final var temp = Files.createTempFile("temp", String.valueOf(System.currentTimeMillis()));
          try (final var out = Files.newOutputStream(temp);
               final var zipOut = new ZipOutputStream(out)) {
            this.forEachIn(Paths.get(""), true, entry -> {
              try {
                final var zipEntry = new ZipEntry(entry.toString());
                zipOut.putNextEntry(zipEntry);
                Files.copy(entry, zipOut);
                zipOut.closeEntry();
              } catch (final IOException ignored) {
                // ignored.
              }
            });
          }
          RplDirectory.TEMP_FILES.add(temp);
          return temp;
        } catch (final IOException e) {
          throw new CompletionException(e);
        }
      });
    }
    return this.preparedFile;
  }

  @Override
  public boolean hasAsset(@NotNull final Path path) {
    final var asset = this.location.resolve(path);
    return Files.exists(asset) && Files.isRegularFile(asset);
  }

  @Override
  public boolean hasFolder(@NotNull final Path path) {
    final var folder = this.location.resolve(path);
    return Files.exists(folder) && Files.isDirectory(folder);
  }

  @Override
  public void shutdown() {
    RplDirectory.TEMP_FILES.forEach(temp -> {
      try {
        Files.deleteIfExists(temp);
      } catch (final IOException ignored) {
        // ignored.
      }
    });
  }

  /**
   * a class that represents directory pack loader factories.
   */
  private static final class DirectoryFactory implements PackLoader.Factory {

    @Override
    public boolean canLoad(@NotNull final Path path) {
      return Files.isDirectory(path);
    }

    @Override
    public PackLoader create(@NotNull final Path path) {
      return new RplDirectory(path);
    }
  }
}
