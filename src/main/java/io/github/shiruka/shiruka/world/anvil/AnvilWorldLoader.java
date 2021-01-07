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

import com.google.common.base.Preconditions;
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
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that loads Shiru ka's worlds.
 */
public final class AnvilWorldLoader extends ShirukaWorldLoader {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("AnvilWorldLoader");

  @NotNull
  @Override
  public final World create(@NotNull final String name, @NotNull final WorldCreateSpec spec) {
    final var compute = this.worldsByName.compute(name, (k, v) -> {
      Preconditions.checkArgument(v == null, "World \"%s\" already exists!", name);
      AnvilWorldLoader.LOGGER.info("§eCreating world \"{}\".", name);
      final var world = new AnvilWorld(name, Misc.HOME_PATH.resolve(name), spec);
      world.loadSpawnChunks();
      world.save();
      AnvilWorldLoader.LOGGER.info("§eFinished creating \"{}\".", name);
      return world;
    });
    this.worldsByUniqueId.put(compute.getUniqueId(), compute);
    return compute;
  }

  @Override
  public final boolean delete(@NotNull final World world) {
    final var nameRemoved = this.worldsByName.remove(world.getName());
    final var uniqueIdRemoved = this.worldsByUniqueId.remove(world.getUniqueId());
    if (nameRemoved == null && uniqueIdRemoved == null) {
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
  public final Optional<World> get(@NotNull final String name) {
    final var world = this.worldsByName.get(name);
    if (world != null) {
      return Optional.of(world);
    }
    final var path = Misc.HOME_PATH.resolve(name);
    Preconditions.checkArgument(Files.isDirectory(path), "%s has no world!", name);
    final var levelDat = path.resolve("level.dat");
    Preconditions.checkArgument(Files.exists(levelDat), "%s has no world!", name);
    return Optional.of(this.load(name, path, Dimension.OVER_WORLD));
  }

  @NotNull
  @Override
  public Optional<World> get(@NotNull final UUID uniqueId) {
    final var world = this.worldsByUniqueId.get(uniqueId);
    if (world != null) {
      return Optional.of(world);
    }
    return Optional.empty();
  }

  @NotNull
  @Override
  public World load(@NotNull final String name, @NotNull final Path directory, @NotNull final Dimension dimension) {
    AnvilWorldLoader.LOGGER.info("§eLoading world \"{}\".", name);
    final var world = new AnvilWorld(name, directory, dimension);
    world.loadSpawnChunks();
    this.worldsByName.put(name, world);
    this.worldsByUniqueId.put(world.getUniqueId(), world);
    AnvilWorldLoader.LOGGER.info("§eFinished loading \"{}\".", name);
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
    if (this.worldsByName.isEmpty() || !this.worldsByName.containsKey(defaultWorldName)) {
      this.create(defaultWorldName, WorldCreateSpec.getDefaultOptions());
    }
  }
}
