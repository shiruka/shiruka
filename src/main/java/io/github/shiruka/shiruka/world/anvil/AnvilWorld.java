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

import io.github.shiruka.api.base.HeightMap;
import io.github.shiruka.api.base.Location;
import io.github.shiruka.api.block.Block;
import io.github.shiruka.api.world.Chunk;
import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.Dimension;
import io.github.shiruka.api.world.options.GeneratorOptions;
import io.github.shiruka.api.world.options.WorldCreateSpec;
import io.github.shiruka.shiruka.nbt.Tag;
import io.github.shiruka.shiruka.world.options.ShirukaGeneratorOptions;
import io.github.shiruka.shiruka.world.options.ShirukaWeather;
import io.github.shiruka.shiruka.world.options.ShirukaWorldBorder;
import io.github.shiruka.shiruka.world.options.ShirukaWorldOptions;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link World}.
 *
 * @todo #1:60m Implement methods of anvil world.
 */
public final class AnvilWorld implements World {

  /**
   * the implementation of the world border.
   */
  private final ShirukaWorldBorder border = new ShirukaWorldBorder(this);

  /**
   * the chunk map.
   */
  @NotNull
  private final AnvilChunkMap chunks = new AnvilChunkMap(this);

  /**
   * the dimension.
   */
  @NotNull
  private final Dimension dimension;

  /**
   * the directory.
   */
  @NotNull
  private final Path directory;

  /**
   * the implementation generator options
   */
  @NotNull
  private final ShirukaGeneratorOptions generatorOptions;

  /**
   * the name.
   */
  @NotNull
  private final String name;

  /**
   * the current world time, in ticks.
   */
  private final AtomicInteger time = new AtomicInteger();

  /**
   * the unique id.
   */
  private final UUID uniqueId = UUID.randomUUID();

  /**
   * the implementation of the world's current weather.
   */
  private final ShirukaWeather weather = new ShirukaWeather(this);

  /**
   * The implementation world options
   */
  @NotNull
  private final ShirukaWorldOptions worldOptions;

  /**
   * ctor.
   *
   * @param dimension the dimension.
   * @param directory the directory.
   * @param name the name.
   * @param worldOptions the world options.
   * @param generatorOptions the generator options.
   */
  public AnvilWorld(@NotNull final Dimension dimension, @NotNull final Path directory,
                    @NotNull final String name, @NotNull final ShirukaWorldOptions worldOptions,
                    @NotNull final ShirukaGeneratorOptions generatorOptions) {
    this.dimension = dimension;
    this.directory = directory;
    this.name = name;
    this.worldOptions = worldOptions;
    this.generatorOptions = generatorOptions;
  }

  /**
   * creates a new world with the given name, folder, and creation options.
   *
   * @param name the name of the new world.
   * @param directory the directory folder.
   * @param spec the world spec.
   */
  public AnvilWorld(@NotNull final String name, @NotNull final Path directory, @NotNull final WorldCreateSpec spec) {
    this.name = name;
    this.directory = directory;
    this.dimension = spec.getDimension();
    this.generatorOptions = new ShirukaGeneratorOptions(spec);
    this.worldOptions = new ShirukaWorldOptions(this, spec);
  }

  /**
   * loads a new world with the given name and folder.
   *
   * @param name the name of the world.
   * @param directory the directory folder.
   * @param dimension the dimension.
   */
  public AnvilWorld(@NotNull final String name, @NotNull final Path directory, @NotNull final Dimension dimension) {
    this.name = name;
    this.directory = directory;
    this.dimension = dimension;
    try (final var stream = Tag.createGZIPReader(
      new FileInputStream(this.directory.resolve("level.dat").toFile()))) {
      final var root = stream.readCompoundTag();
      final var compound = root.get("Data").orElseThrow().asCompound();
      this.generatorOptions = new ShirukaGeneratorOptions(compound);
      this.worldOptions = new ShirukaWorldOptions(this, compound);
      this.weather.read(compound);
      this.border.read(compound);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public final String getName() {
    return this.name;
  }

  @NotNull
  @Override
  public Block getBlockAt(final int x, final int y, final int z) {
    return null;
  }

  @NotNull
  @Override
  public Block getBlockAt(@NotNull final Location location) {
    return null;
  }

  @NotNull
  @Override
  public Chunk getChunkAt(@NotNull final Location location) {
    return null;
  }

  @NotNull
  @Override
  public final Chunk getChunkAt(final int x, final int z) {
    return Objects.requireNonNull(this.chunks.get(x, z, true));
  }

  @Nullable
  @Override
  public final Chunk getChunkAt(final int x, final int z, final boolean gen) {
    return this.chunks.get(x, z, gen);
  }

  @NotNull
  @Override
  public final Dimension getDimension() {
    return this.dimension;
  }

  @NotNull
  @Override
  public final Path getDirectory() {
    return this.directory;
  }

  @NotNull
  @Override
  public GeneratorOptions getGeneratorOptions() {
    return this.generatorOptions;
  }

  @NotNull
  @Override
  public Block getHighestBlockAt(final int x, final int z, @NotNull final HeightMap heightMap) {
    return null;
  }

  @NotNull
  @Override
  public Block getHighestBlockAt(@NotNull final Location location, @NotNull final HeightMap heightMap) {
    return null;
  }

  @Override
  public int getHighestBlockYAt(final int x, final int z, @NotNull final HeightMap heightMap) {
    return 0;
  }

  @Override
  public int getHighestBlockYAt(@NotNull final Location location, @NotNull final HeightMap heightMap) {
    return 0;
  }

  @Override
  public final int getHighestY(final int x, final int z) {
    return this.getChunkAt(x >> 4, z >> 4).getHighestY(x & 15, z & 15);
  }

  @Override
  public final Collection<Chunk> getLoadedChunks() {
    return Collections.unmodifiableCollection(this.chunks.values());
  }

  @Override
  public int getTime() {
    return this.time.get();
  }

  @Override
  public boolean isChunkGenerated(final int x, final int z) {
    return false;
  }

  @Override
  public boolean isChunkLoaded(final int x, final int z) {
    return false;
  }

  @Override
  public final void loadSpawnChunks() {
    final var centerX = this.worldOptions.getSpawn().getIntX() >> 4;
    final var centerZ = this.worldOptions.getSpawn().getIntZ() >> 4;
    final var radius = 3;
    for (var x = centerX - radius; x < centerX + radius; x++) {
      for (var z = centerZ - radius; z < centerZ + radius; z++) {
        this.getChunkAt(x, z);
      }
    }
  }

  @Nullable
  @Override
  public AnvilChunk removeChunkAt(final int x, final int z) {
    return this.chunks.remove(x, z);
  }

  @Override
  public void save() {
    final var level = this.directory.resolve("level.dat");
    final var regionDir = this.directory.resolve("region");
    try {
      if (!Files.exists(this.directory)) {
        Files.createDirectory(this.directory);
      }
      if (!Files.exists(level)) {
        Files.createFile(level);
      }
      if (!Files.exists(regionDir)) {
        Files.createDirectory(regionDir);
      }
      final var worldRoot = Tag.createCompound();
      final var worldData = Tag.createCompound();
      worldData.setString("LevelName", this.name);
      this.worldOptions.write(worldData);
      this.generatorOptions.write(worldData);
      this.weather.write(worldData);
      this.border.write(worldData);
      worldRoot.set("Data", worldData);
      try (final var stream = Tag.createGZIPWriter(new FileOutputStream(level.toFile()))) {
        stream.write(worldRoot);
      }
      this.chunks.forEach(c -> {
        final AnvilRegion region = AnvilRegion.getFile(c, true);
        if (region == null) {
          return;
        }
        try (final var writer =
               Tag.createGZIPWriter(region.getChunkDataOutputStream(c.getX() & 31, c.getZ() & 31))) {
          final var rootChunk = Tag.createCompound();
          final var chunkData = Tag.createCompound();
          c.write(chunkData);
          rootChunk.set("Level", chunkData);
          writer.write(rootChunk);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  /**
   * obtains the world options.
   *
   * @return the world options.
   */
  @NotNull
  public ShirukaWorldOptions getWorldOptions() {
    return this.worldOptions;
  }
}
