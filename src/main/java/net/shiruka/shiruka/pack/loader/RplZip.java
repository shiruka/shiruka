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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.shiruka.api.pack.PackLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple zip implementation for {@link PackLoader}.
 */
public final class RplZip implements PackLoader {

  /**
   * the factory instance.
   */
  public static final Factory FACTORY = new ZipFactory();

  /**
   * the path.
   */
  @NotNull
  private final Path path;

  /**
   * the zip file.
   */
  @NotNull
  private final ZipFile zipFile;

  /**
   * the prepared file.
   */
  @Nullable
  private CompletableFuture<Path> preparedFile;

  /**
   * ctor.
   *
   * @param path tha path.
   * @param zipFile the zip file.
   */
  private RplZip(@NotNull final Path path, @NotNull final ZipFile zipFile) {
    this.path = path;
    this.zipFile = zipFile;
  }

  /**
   * ctor.
   *
   * @param path the path.
   *
   * @throws IOException if an I/O error has occurred.
   */
  public RplZip(@NotNull final Path path) throws IOException {
    this(path, new ZipFile(path.toFile()));
  }

  @Override
  public void close() throws IOException {
    this.zipFile.close();
  }

  @Override
  public void forEachIn(@NotNull final Path path, final boolean recurse, @NotNull final Consumer<Path> consumer) {
    final var entries = this.zipFile.entries();
    while (entries.hasMoreElements()) {
      final var entry = entries.nextElement();
      final var entryPath = Paths.get(entry.getName());
      if (entryPath.getParent().equals(path)) {
        consumer.accept(entryPath);
      }
      if (entry.isDirectory()) {
        this.forEachIn(entryPath, true, consumer);
      }
    }
  }

  @NotNull
  @Override
  public Optional<InputStream> getAsset(@NotNull final Path path) throws IOException {
    final var entry = this.entry(path);
    if (entry == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.zipFile.getInputStream(entry));
  }

  @NotNull
  @Override
  public Path getLocation() {
    return this.path;
  }

  @NotNull
  @Override
  public CompletableFuture<Path> getPreparedFile() {
    if (this.preparedFile == null) {
      this.preparedFile = CompletableFuture.completedFuture(this.path);
    }
    return this.preparedFile;
  }

  @Override
  public boolean hasAsset(@NotNull final Path path) {
    final var entry = this.entry(path);
    return entry != null && !entry.isDirectory();
  }

  @Override
  public boolean hasFolder(@NotNull final Path path) {
    final var entry = this.entry(path);
    return entry != null && entry.isDirectory();
  }

  @Override
  public void shutdown() {
    // ignored.
  }

  /**
   * obtains the zip entry.
   *
   * @param path the path to get.
   *
   * @return zip entry.
   */
  @Nullable
  private ZipEntry entry(@NotNull final Path path) {
    return this.zipFile.getEntry(path.toString());
  }

  /**
   * a class that represents zip pack loader factories.
   */
  private static final class ZipFactory implements PackLoader.Factory {

    /**
     * the logger.
     */
    private static final Logger LOGGER = LogManager.getLogger("ZipFactory");

    /**
     * zip pack loader name matcher.
     */
    private static final PathMatcher MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.{zip,mcpack}");

    @Override
    public boolean canLoad(@NotNull final Path path) {
      return ZipFactory.MATCHER.matches(path);
    }

    @Nullable
    @Override
    public PackLoader create(@NotNull final Path path) {
      try {
        return new RplZip(path);
      } catch (final IOException e) {
        ZipFactory.LOGGER.error("The path is not a zip file!", e);
      }
      return null;
    }
  }
}
