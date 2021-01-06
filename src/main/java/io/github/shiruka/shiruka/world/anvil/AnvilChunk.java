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

import io.github.shiruka.api.misc.Optionals;
import io.github.shiruka.api.world.Chunk;
import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.generators.GeneratorContainer;
import io.github.shiruka.api.world.options.Dimension;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.misc.CdlUnchecked;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import io.github.shiruka.shiruka.nbt.Tag;
import io.github.shiruka.shiruka.world.generators.ShirukaGeneratorContext;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.*;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Chunk}.
 */
public final class AnvilChunk implements Chunk {

  /**
   * thread pool used for arbitrary container generation.
   */
  private static final ServerThreadPool ARBITRARY_POOL = ServerThreadPool.forSpec(PoolSpec.CHUNKS);

  /**
   * thread pool used for default container generation.
   */
  private static final ServerThreadPool DEFAULT_POOL = ServerThreadPool.forSpec(PoolSpec.PLUGINS);

  /**
   * the transition constant.
   */
  private static final int TRANSITION = 0;

  /**
   * the unusable constant.
   */
  private static final int UNUSABLE = 1;

  /**
   * the usable constant.
   */
  private static final int USABLE = -1;

  /**
   * an empty ChunkSection which is used in place of a
   * null element to save memory.
   */
  private final AnvilChunkSection emptyPlaceholder;

  /**
   * whether or not this chunk is being generated.
   */
  private final AtomicBoolean generationInProgress = new AtomicBoolean();

  /**
   * the height map for this chunk, 16x16 indexed by x across then adding z (x << 4 | z & 0xF)
   */
  private final AtomicIntegerArray heights = new AtomicIntegerArray(256);

  /**
   * the number of ticks that all players have spent in this chunk.
   */
  private final LongAdder inhabited = new LongAdder();

  /**
   * the ready getState for this chunk, whether it has fully generated yet.
   */
  private final CdlUnchecked ready = new CdlUnchecked(1);

  /**
   * The sections that the chunk has generated
   */
  private final AtomicReferenceArray<AnvilChunkSection> sections = new AtomicReferenceArray<>(16);

  /**
   * Whether or not this chunk is usable
   */
  private final AtomicInteger useState = new AtomicInteger(AnvilChunk.USABLE);

  /**
   * the world.
   */
  @NotNull
  private final AnvilWorld world;

  /**
   * the x;
   */
  private final int x;

  /**
   * the z.
   */
  private final int z;

  /**
   * ctor.
   *
   * @param world the world.
   * @param x the x.
   * @param z the z.
   */
  public AnvilChunk(@NotNull final AnvilWorld world, final int x, final int z) {
    this.world = world;
    this.x = x;
    this.z = z;
    if (world.getDimension() == Dimension.OVER_WORLD) {
      this.emptyPlaceholder = AnvilChunkSection.EMPTY_WITH_SKYLIGHT;
    } else {
      this.emptyPlaceholder = AnvilChunkSection.EMPTY_WITHOUT_SKYLIGHT;
    }
  }

  @Override
  public boolean canUse() {
    int state;
    do {
      state = this.useState.get();
    } while (state == AnvilChunk.TRANSITION);
    return state == AnvilChunk.USABLE;
  }

  @Override
  public void generate() {
    if (!this.generationInProgress.compareAndSet(false, true)) {
      return;
    }
    final var region = AnvilRegion.getFile(this, false);
    if (region == null) {
      this.runGenerator();
      return;
    }
    final var rX = this.x & 31;
    final var rZ = this.z & 31;
    if (!region.hasChunk(rX, rZ)) {
      this.runGenerator();
      return;
    }
    try (final var stream =
           Tag.createGZIPReader(Objects.requireNonNull(region.getChunkDataInputStream(rX, rZ)))) {
      final var compound = stream.readCompoundTag();
      CompletableFuture.runAsync(() -> this.read(compound), AnvilChunk.ARBITRARY_POOL)
        .whenCompleteAsync((v, t) -> {
          if (this.ready.getCount() == 1) {
            this.runGenerator();
          }
        }, AnvilChunk.ARBITRARY_POOL);
      this.waitReady();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getHighestY(final int x, final int z) {
    return this.heights.get(x << 4 | z & 0xF);
  }

  @NotNull
  @Override
  public World getWorld() {
    return this.world;
  }

  @Override
  public int getX() {
    return this.x;
  }

  @Override
  public int getZ() {
    return this.z;
  }

  @Override
  public AnvilChunk waitReady() {
    this.ready.await();
    return this;
  }

  /**
   * updates the usability state field in order to check if this chunk may still be used or is reclaimable.
   */
  public void checkValidForGc() {
    this.useState.set(AnvilChunk.TRANSITION);
    final var centerX = this.world.getWorldOptions().getSpawn().getIntX() >> 4;
    final var centerZ = this.world.getWorldOptions().getSpawn().getIntZ() >> 4;
    if (/* @todo #1:30m this.holders.isEmpty() &&*/ Math.abs(centerX - this.x) > 3 || Math.abs(centerZ - this.z) > 3) {
      this.useState.set(AnvilChunk.UNUSABLE);
      if (this.world.removeChunkAt(this.x, this.z) == null) {
        return;
      }
      final var region = Objects.requireNonNull(AnvilRegion.getFile(this, true));
      final var rX = this.x & 31;
      final var rZ = this.z & 31;
      if (region.hasChunk(rX, rZ)) {
        try (final var stream = Tag.createGZIPReader(region.getChunkDataInputStream(rX, rZ))) {
          final var root = stream.readCompoundTag();
          final var compound = root.get("Level").orElseThrow().asCompound();
          CompletableFuture.runAsync(() -> this.write(compound), AnvilChunk.ARBITRARY_POOL)
            .whenCompleteAsync((v, t) -> {
              try (final var writer = Tag.createGZIPWriter(region.getChunkDataOutputStream(rX, rZ))) {
                writer.write(root);
              } catch (final IOException e) {
                throw new RuntimeException(e);
              }
            }, AnvilChunk.ARBITRARY_POOL);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
        return;
      }
      AnvilChunk.ARBITRARY_POOL.execute(() -> {
        final var root = Tag.createCompound();
        final var level = Tag.createCompound();
        root.set("Level", level);
        this.write(level);
        try (final var writer = Tag.createGZIPWriter(region.getChunkDataOutputStream(rX, rZ))) {
          writer.write(root);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      });
    } else {
      this.useState.set(AnvilChunk.USABLE);
    }
  }

  /**
   * writes the given {@code buf} byte buf.
   *
   * @param buf the buf to write.
   * @param continuous the continous to write.
   */
  public void write(@NotNull final ByteBuf buf, final boolean continuous) {
    final var len = this.sections.length();
    var mask = 0;
    final var anvilChunkSections = new AnvilChunkSection[16];
    for (int i = 0; i < 16; i++) {
      final var sec = this.sections.get(i);
      anvilChunkSections[i] = sec;
      if (sec != null) {
        mask |= 1 << i;
      }
    }
    VarInts.writeVarInt(buf, mask);
    final var chunkData = buf.alloc().buffer();
    try {
      for (var i = 0; i < len; i++) {
        if ((mask & 1 << i) != 1 << i) {
          continue;
        }
        final var sec = anvilChunkSections[i];
        if (sec != null) {
          sec.write(chunkData);
        } else {
          this.emptyPlaceholder.write(chunkData);
        }
      }
      VarInts.writeVarInt(buf, chunkData.readableBytes() + (continuous ? 256 : 0));
      buf.writeBytes(chunkData);
    } finally {
      chunkData.release();
    }
    if (continuous) {
      IntStream.range(0, 256)
        .map(i -> 1)
        .forEach(buf::writeByte);
    }
    // @todo #1:15m Tile entities.
    VarInts.writeVarInt(buf, 0);
  }

  /**
   * writes the chunk data to the region file compound.
   * <p>
   *
   * @param compound the compound to write.
   *
   * @todo #1:60m Biomes (byte_array)
   * @todo #1:60m TileEntities (list [tag compound?])
   * @todo #1:60m Entities (list [tag compound?])
   */
  public void write(@NotNull final CompoundTag compound) {
    compound.setInteger("xPos", this.x);
    compound.setInteger("zPos", this.z);
    final var hasGenerated = (byte) (this.ready.getCount() == 0 ? 1 : 0);
    compound.setByte("TerrainPopulated", hasGenerated);
    compound.setByte("LightPopulated", hasGenerated);
    compound.setLong("InhabitedTime", this.inhabited.longValue());
    compound.setLong("LastUpdate", (long) this.world.getTime());
    final var sectionList = Tag.createList(Tag.createCompound());
    IntStream.range(0, this.sections.length()).forEach(i ->
      Optional.ofNullable(this.sections.get(i)).ifPresent(section -> {
        final var sectionCompound = Tag.createCompound();
        sectionCompound.setByte("Y", (byte) i);
        section.write(sectionCompound);
        sectionList.add(sectionCompound);
      }));
    compound.set("Sections", sectionList);
    final var heightMap = new int[this.heights.length()];
    Arrays.setAll(heightMap, this.heights::get);
    compound.setIntArray("HeightMap", heightMap);
  }

  /**
   * reads the chunk data from the region file compound.
   *
   * @param compound the compound to read.
   */
  private void read(@NotNull final CompoundTag compound) {
    this.inhabited.add(compound.getLong("InhabitedTime").orElseThrow());
    final var sectionList = compound.getList("Sections").orElseThrow();
    sectionList.stream()
      .filter(Tag::isCompound)
      .map(Tag::asCompound)
      .forEach(c -> {
        final var section = new AnvilChunkSection(this.world.getDimension() == Dimension.OVER_WORLD);
        section.read(c);
        final var y = c.getByte("Y").orElseThrow();
        this.sections.set(y, section);
      });
    final var heightMap = compound.getIntArray("HeightMap").orElseThrow();
    IntStream.range(0, heightMap.length)
      .forEach(i -> this.heights.set(i, heightMap[i]));
    if (compound.getByte("TerrainPopulated").orElseThrow() == 1) {
      this.generationInProgress.set(true);
      this.ready.countDown();
    }
  }

  /**
   * runs the custom generator function when the data is not loaded into memory, or if the chunk has no data to load.
   */
  private void runGenerator() {
    final var opts = this.world.getGeneratorOptions();
    final var provider = opts.getProvider();
    var container = (Executor) provider.getGenerationContainer();
    if (container == GeneratorContainer.DEFAULT) {
      container = AnvilChunk.DEFAULT_POOL;
    } else if (container == GeneratorContainer.ARBITRARY) {
      container = AnvilChunk.ARBITRARY_POOL;
    }
    final var terrain = provider.getTerrainGenerator(this.world);
    final var props = provider.getPropGenerators(this.world);
    final var features = provider.getFeatureGenerators(this.world);
    final var context = new ShirukaGeneratorContext(container, opts.getSeed(),
      this.world.getDimension() == Dimension.OVER_WORLD);
    CompletableFuture.supplyAsync(() -> {
      terrain.generate(this.x, this.z, context);
      features.forEach(generator -> generator.generate(this.x, this.z, context));
      return Optionals.useAndGet(context.getCount(), context::doRun);
    }, container)
      .thenApplyAsync(l -> {
        l.await();
        context.reset();
        props.forEach(generator -> generator.generate(this.x, this.z, context));
        return Optionals.useAndGet(context.getCount(), context::doRun);
      }, container)
      .thenAcceptAsync(l -> {
        l.await();
        context.copySections(this.sections);
        context.copyHeights(this.heights);
        this.ready.countDown();
      }, container);
    this.waitReady();
  }
}
