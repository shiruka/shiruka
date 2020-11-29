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
import io.github.shiruka.shiruka.nbt.Tag;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link World}.
 */
public final class AnvilWorld implements World {

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
   * the name.
   */
  @NotNull
  private final String name;

  /**
   * ctor.
   *
   * @param name the name.
   * @param directory the enclosing.
   * @param dimension the dimension.
   */
  public AnvilWorld(@NotNull final String name, @NotNull final Path directory, @NotNull final Dimension dimension) {
    this.name = name;
    this.directory = directory;
    this.dimension = dimension;
    try (final var stream =
           Tag.createGZIPReader(new FileInputStream(this.directory.resolve("level.dat").toFile()))) {
      stream.readCompoundTag();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * ctor.
   *
   * @param name the name.
   * @param directory the enclosing.
   * @param spec the spec.
   */
  public AnvilWorld(@NotNull final String name, @NotNull final Path directory, @NotNull final WorldCreateSpec spec) {
    this.name = name;
    this.directory = directory;
    this.dimension = spec.getDimension();
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void loadSpawnChunks() {
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
//      this.worldOptions.write(worldData);
//      this.generatorOptions.write(worldData);
//      this.weather.write(worldData);
//      this.border.write(worldData);
      worldRoot.set("Data", worldData);
      try (final var stream = Tag.createGZIPWriter(new FileOutputStream(level.toFile()))) {
        stream.write(worldRoot);
      }
//      this.chunks.forEach(c -> {
//        Region region = Region.getFile(c, true);
//        try (DataOutputStream out = region.getChunkDataOutputStream(c.getX() & 31, c.getZ() & 31)) {
//          Tag.Compound rootChunk = new Tag.Compound("");
//          Tag.Compound chunkData = new Tag.Compound("Level");
//          c.write(chunkData);
//          rootChunk.putCompound(chunkData);
//          rootChunk.write(out);
//        } catch (IOException e) {
//          throw new RuntimeException(e);
//        }
//      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
