/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
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

package io.github.shiruka.shiruka.world.anvil;

import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.Dimension;
import io.github.shiruka.api.world.options.WorldCreateSpec;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.log.Loggers;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.world.ShirukaWorldLoader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.jetbrains.annotations.NotNull;

/**
 * a class that loads Shiru ka's worlds.
 */
public final class AnvilWorldLoader extends ShirukaWorldLoader {

  @NotNull
  @Override
  public final World create(@NotNull final String name, @NotNull final WorldCreateSpec spec) {
    return this.worlds.compute(name, (k, v) -> {
      if (v != null) {
        throw new IllegalArgumentException("World \"" + name + "\" already exists");
      }
      Loggers.log("Creating world \"%s\"...", name);
      final var world = new AnvilWorld(name, Misc.HOME_PATH.resolve(name), spec);
      world.loadSpawnChunks();
      world.save();
      Loggers.log("Finished creating \"%s\".", name);
      return world;
    });
  }

  @Override
  public void loadAll() {
    final var defaultWorldName = ServerConfig.DEFAULT_WORLD_NAME.getValue()
      .orElseThrow();
    try {
      Files.walkFileTree(Misc.HOME_PATH, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
          final var levelDat = dir.resolve("level.dat");
          if (Files.exists(levelDat)) {
            AnvilWorldLoader.this.load(dir.getFileName().toString(), dir, Dimension.OVER_WORLD);
            return FileVisitResult.SKIP_SUBTREE;
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    if (this.worlds.isEmpty() || !this.worlds.containsKey(defaultWorldName)) {
      this.create(defaultWorldName, WorldCreateSpec.getDefaultOptions());
    }
  }

  /**
   * loads method for shortcutting NBT decoding.
   *
   * @param name the name of the world to be loaded.
   * @param directory the directory folder.
   *
   * @return the world, once it has loaded.
   */
  @NotNull
  private World load(@NotNull final String name, @NotNull final Path directory, @NotNull final Dimension dimension) {
    Loggers.log("Loading world \"%s\"...", name);
    final var world = new AnvilWorld(name, directory, dimension);
    world.loadSpawnChunks();
    this.worlds.put(name, world);
    Loggers.log("Finished loading \"%s\".", name);
    return world;
  }
}
