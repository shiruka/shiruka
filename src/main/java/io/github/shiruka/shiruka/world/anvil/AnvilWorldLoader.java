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
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.world.ShirukaWorldLoader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class that loads Shiru ka's worlds.
 */
public final class AnvilWorldLoader extends ShirukaWorldLoader {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("AnvilWorldLoader");

  @NotNull
  @Override
  public final World create(@NotNull final String name, @NotNull final WorldCreateSpec spec) {
    return this.worlds.compute(name, (k, v) -> {
      if (v != null) {
        throw new IllegalArgumentException("World \"" + name + "\" already exists!");
      }
      AnvilWorldLoader.LOGGER.info("Creating world \"{}\".", name);
      final var world = new AnvilWorld(name, Misc.HOME_PATH.resolve(name), spec);
      world.loadSpawnChunks();
      world.save();
      AnvilWorldLoader.LOGGER.info("Finished creating \"{}\".", name);
      return world;
    });
  }

  @Override
  public final boolean delete(@NotNull final World world) {
    if (this.worlds.remove(world.getName()) == null) {
      return false;
    }
    final var path = world.getDirectory();
    try {
      Files.walkFileTree(path, ShirukaWorldLoader.DELETE_FILES);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  @NotNull
  @Override
  public final World get(@NotNull final String name) {
    final var world = this.worlds.get(name);
    if (world != null) {
      return world;
    }
    final var path = Misc.HOME_PATH.resolve(name);
    if (Files.isDirectory(path)) {
      final var levelDat = path.resolve("level.dat");
      if (Files.exists(levelDat)) {
        return this.load(name, path, Dimension.OVER_WORLD);
      }
    }
    throw new IllegalArgumentException(name + " has no world!");
  }

  @NotNull
  @Override
  public World load(@NotNull final String name, @NotNull final Path directory, @NotNull final Dimension dimension) {
    AnvilWorldLoader.LOGGER.info("Loading world \"{}\".", name);
    final var world = new AnvilWorld(name, directory, dimension);
    world.loadSpawnChunks();
    this.worlds.put(name, world);
    AnvilWorldLoader.LOGGER.info("Finished loading \"{}\".", name);
    return world;
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
}
