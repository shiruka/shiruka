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

import io.github.shiruka.shiruka.misc.NibbleArray;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public final class AnvilChunkSection {

  /**
   * empty chunk section which does not write skylight.
   */
  public static final AnvilChunkSection EMPTY_WITHOUT_SKYLIGHT = new AnvilChunkSection(false);

  /**
   * empty chunk section for writing over world chunks.
   */
  public static final AnvilChunkSection EMPTY_WITH_SKYLIGHT = new AnvilChunkSection(true);

  /**
   * the amount of blocks in a chunk section.
   */
  private static final int BLOCKS_PER_SECTION = 4096;

  /**
   * The number of block state values that can be stored in a single long.
   */
  private static final int SHORTS_PER_LONG = 4;

  /**
   * the nibble array of light emitted from blocks.
   */
  private final NibbleArray blockLight = new NibbleArray(AnvilChunkSection.BLOCKS_PER_SECTION / 2);

  /**
   * the data array, which contains palette indexes at the XYZ index in the array.
   */
  private final AtomicLongArray data =
    new AtomicLongArray(AnvilChunkSection.BLOCKS_PER_SECTION / AnvilChunkSection.SHORTS_PER_LONG);

  /**
   * The flag for writing skylight in other dimensions
   */
  private final boolean doSkylight;

  /**
   * the palette that caches the block states used by this ChunkSection and evicts unused entries when the
   * chunk is written.
   */
  private final ShortOpenHashSet mainPalette = new ShortOpenHashSet();

  /**
   * the nibble array of light reaching from the sky.
   */
  private final NibbleArray skyLight = new NibbleArray(AnvilChunkSection.BLOCKS_PER_SECTION / 2);

  /**
   * ctor.
   *
   * @param doSkylight whether to write skylight.
   */
  public AnvilChunkSection(final boolean doSkylight) {
    this.doSkylight = doSkylight;
    this.mainPalette.add((short) 0);
    IntStream.range(0, this.data.length())
      .forEach(i -> this.data.set(i, 0L));
    this.blockLight.fill((byte) 0xF);
    this.skyLight.fill((byte) 0xF);
  }

  /**
   * Loads block data from the NBT tag read at the chunk's
   * region file into this chunk section.
   *
   * @param section the section to load NBT data
   */
  public void read(@NotNull final CompoundTag section) {
    final var blocks = section.getByteArray("Blocks").orElseThrow();
    final var add = section.get("Add");
    final var data = section.getByteArray("Data").orElseThrow();
    final var skylight = section.getByteArray("SkyLight").orElseThrow();
    final var blocklight = section.getByteArray("BlockLight").orElseThrow();
    this.skyLight.read(skylight);
    this.blockLight.read(blocklight);
    for (var y = 0; y < 16; y++) {
      for (var z = 0; z < 16; z++) {
        for (var x = 0; x < 16; x++) {
          final var realIdx = y << 8 | z << 4 | x;
          final var block = blocks[realIdx];
          final var blockData = NibbleArray.getNibble(data, realIdx);
          if (add.isPresent()) {
            final var blockId = block + ((int) NibbleArray.getNibble(add.get().asByteArray().value(), realIdx) << 8);
            final var state = (short) (blockId << 4 | blockData);
            this.set(realIdx, state);
          } else {
            final var state = (short) (block << 4 | blockData);
            this.set(realIdx, state);
          }
        }
      }
    }
  }

  /**
   * sets the block at the given position in the chunk section to the given block getState.
   *
   * @param idx the XYZ index.
   * @param state the block getState to set.
   */
  public void set(final int idx, final short state) {
    synchronized (this.mainPalette) {
      this.mainPalette.add(state);
    }
    final var spliceIdx = idx >>> 2;
    final var shift = idx % AnvilChunkSection.SHORTS_PER_LONG << 4;
    final var placeMask = ~(0xFFFFL << shift);
    final var shiftedState = (long) state << shift;
    long oldSplice;
    long newSplice;
    do {
      oldSplice = this.data.get(spliceIdx);
      newSplice = oldSplice & placeMask | shiftedState;
    } while (!this.data.compareAndSet(spliceIdx, oldSplice, newSplice));
    // @todo #1:15m relighting.
  }

  /**
   * writes the section data to the given byte stream.
   *
   * @param buf the buffer to write the section data.
   */
  public void write(@NotNull final ByteBuf buf) {
    var dataLen = 0;
    final var dataBuffer = buf.alloc().buffer();
    ShortArrayList palette = null;
    int bitsPerBlock;
    final boolean doPalette;
    synchronized (this.mainPalette) {
      final var paletteSize = this.mainPalette.size();
      bitsPerBlock = Integer.highestOneBit(paletteSize);
      if (bitsPerBlock < 4) {
        bitsPerBlock = 4;
      }
      doPalette = bitsPerBlock < 9;
      this.mainPalette.clear();
      this.mainPalette.add((short) 0);
      if (!doPalette) {
        bitsPerBlock = 13;
      } else {
        palette = new ShortArrayList(paletteSize);
        palette.add((short) 0);
      }
      final var individualValueMask = (1 << bitsPerBlock) - 1;
      var bitsWritten = 0;
      var cur = 0;
      for (var y = 0; y < 16; y++) {
        for (var z = 0; z < 16; z++) {
          for (var x = 0; x < 16; x++) {
            final var realIdx = y << 8 | z << 4 | x;
            final var shortData = this.dataAt(realIdx);
            var data = (int) shortData;
            final var added = this.mainPalette.add(shortData);
            if (doPalette) {
              if (added) {
                palette.add(shortData);
                data = palette.size() + 1;
              } else {
                data = palette.indexOf(shortData);
                if (data == -1) {
                  throw new IllegalStateException("Failed to lock");
                }
              }
            }
            final var shift = realIdx * bitsPerBlock % 64;
            final var or = data & individualValueMask;
            bitsWritten += bitsPerBlock;
            if (bitsWritten == 64) {
              dataLen++;
              dataBuffer.writeLong(cur | (long) or << shift);
              cur = 0;
              bitsWritten = 0;
            } else if (bitsWritten > 64) {
              bitsWritten -= 64;
              final var lowerLen = bitsPerBlock - bitsWritten;
              final var lowerMask = (1 << lowerLen) - 1;
              dataLen++;
              dataBuffer.writeLong(cur | (long) (or & lowerMask) << shift);
              cur = (or & ~lowerMask) >> lowerLen;
            } else {
              cur |= or << shift;
            }
          }
        }
      }
    }
    buf.writeByte(bitsPerBlock);
    VarInts.writeVarInt(buf, doPalette ? palette.size() : 0);
    final var max = doPalette ? palette.size() : 0;
    for (var i = 0; i < max; i++) {
      VarInts.writeVarInt(buf, palette.getShort(i));
    }
    VarInts.writeVarInt(buf, dataLen);
    buf.writeBytes(dataBuffer);
    if (dataBuffer != null) {
      dataBuffer.release();
    }
    this.blockLight.write(buf);
    if (this.doSkylight) {
      this.skyLight.write(buf);
    }
  }

  /**
   * writes the data from this chunk section into the given NBT data going into a chunk's {@code Sections} list.
   *
   * @param section the section to write.
   */
  public void write(@NotNull final CompoundTag section) {
    section.setByteArray("SkyLight", this.skyLight.write());
    section.setByteArray("BlockLight", this.skyLight.write());
    final var blocks = new byte[4096];
    final var add = new byte[2048];
    final var data = new byte[2048];
    for (var y = 0; y < 16; y++) {
      for (var z = 0; z < 16; z++) {
        for (var x = 0; x < 16; x++) {
          final var realIdx = y << 8 | z << 4 | x;
          final var state = this.dataAt(realIdx);
          final var blockId = state >> 4;
          blocks[realIdx] = (byte) (blockId & 0xFF);
          NibbleArray.setNibble(data, realIdx, (byte) (state & 0xF));
          if (blockId > 255) {
            NibbleArray.setNibble(add, realIdx, (byte) (blockId >> 8));
          }
        }
      }
    }
    section.setByteArray("Blocks", blocks);
    section.setByteArray("Add", add);
    section.setByteArray("Data", data);
  }

  /**
   * obtains the data for a block contained in this chunk section with the given position.
   *
   * @param idx The position of the block.
   *
   * @return A tuple consisting of substance and meta.
   */
  private short dataAt(final int idx) {
    final var spliceIdx = idx >>> 2;
    final var shift = idx % AnvilChunkSection.SHORTS_PER_LONG << 4;
    return (short) (this.data.get(spliceIdx) >>> shift & 0xFFFF);
  }
}
