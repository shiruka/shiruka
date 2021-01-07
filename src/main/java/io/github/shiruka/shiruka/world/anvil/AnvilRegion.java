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

import io.github.shiruka.api.world.Chunk;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * represents a region file stored on file which maps out the data in a chunk to be saved.
 */
public final class AnvilRegion {

  private static final Map<Path, AnvilRegion> CACHE = new ConcurrentHashMap<>();

  private static final int CHUNK_HEADER_SIZE = 5;

  private static final int SECTOR_BYTES = 4096;

  private static final int SECTOR_INTS = AnvilRegion.SECTOR_BYTES / 4;

  private static final int VERSION_DEFLATE = 2;

  private static final int VERSION_GZIP = 1;

  private static final byte[] emptySector = new byte[4096];

  /**
   * the file.
   */
  private final RandomAccessFile file;

  /**
   * the offsets.
   */
  private final int[] offsets;

  /**
   * the path.
   */
  private final Path path;

  /**
   * the region's x.
   */
  private final int regionX;

  /**
   * the region's z.
   */
  private final int regionZ;

  /**
   * the sector free.
   */
  private final ArrayList<Boolean> sectorFree;

  /**
   * ctor.
   *
   * @param path the path.
   */
  private AnvilRegion(final Path path) {
    this.path = path;
    this.offsets = new int[AnvilRegion.SECTOR_INTS];
    try {
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
      this.file = new RandomAccessFile(path.toFile(), "rw");
      if (this.file.length() < AnvilRegion.SECTOR_BYTES) {
        for (var i = 0; i < AnvilRegion.SECTOR_INTS; i++) {
          this.file.writeInt(0);
        }
        for (var i = 0; i < AnvilRegion.SECTOR_INTS; i++) {
          this.file.writeInt(0);
        }
      }
      if ((this.file.length() & 0xfff) != 0) {
        for (var i = 0; i < (this.file.length() & 0xfff); ++i) {
          this.file.write((byte) 0);
        }
      }
      final var nSectors = (int) this.file.length() / AnvilRegion.SECTOR_BYTES;
      this.sectorFree = new ArrayList<>(nSectors);
      IntStream.range(0, nSectors)
        .mapToObj(i -> true)
        .forEach(this.sectorFree::add);
      this.sectorFree.set(0, false);
      this.sectorFree.set(1, false);
      this.file.seek(0);
      for (var i = 0; i < AnvilRegion.SECTOR_INTS; i++) {
        final var offset = this.file.readInt();
        this.offsets[i] = offset;
        if (offset != 0 && (offset >> 8) + (offset & 0xFF) <= this.sectorFree.size()) {
          IntStream.range(0, offset & 0xFF)
            .forEach(sectorNum -> this.sectorFree.set((offset >> 8) + sectorNum, false));
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    final String[] split = path.getFileName().toString().split(Pattern.quote("."));
    this.regionX = Integer.parseInt(split[1]);
    this.regionZ = Integer.parseInt(split[2]);
  }

  /**
   * obtains the chunk file for the given chunk, creating if it doesn't exist and if specified.
   *
   * @param chunk the chunk to obtain the region file.
   * @param create {@code true} to create if it doesn't exist.
   *
   * @return the region file, or {@code null} if it doesn't exist and don't create it.
   */
  @Nullable
  public static AnvilRegion getFile(@NotNull final Chunk chunk, final boolean create) {
    final var path = chunk.getWorld().getDirectory().resolve("region").
      resolve("r." + (chunk.getX() >> 5) + '.' + (chunk.getZ() >> 5) + ".mca");
    if (!Files.exists(path) && !create) {
      return null;
    }
    return AnvilRegion.CACHE.computeIfAbsent(path, AnvilRegion::new);
  }

  /**
   * closes the region file.
   *
   * @throws IOException if something went wrong when closing the file.
   */
  public void close() throws IOException {
    AnvilRegion.CACHE.remove(this.path);
    this.file.close();
  }

  /**
   * obtains the chunk data input stream.
   *
   * @param x the x to get.
   * @param z the z to get.
   *
   * @return obtained chunk data input stream.
   */
  @Nullable
  public synchronized DataInputStream getChunkDataInputStream(final int x, final int z) {
    if (this.outOfBounds(x, z)) {
      return null;
    }
    final var offset = this.getOffset(x, z);
    if (offset == 0) {
      return null;
    }
    final var sectorNumber = offset >> 8;
    final var numSectors = offset & 0xFF;
    if (sectorNumber + numSectors > this.sectorFree.size()) {
      return null;
    }
    try {
      this.file.seek((long) sectorNumber * AnvilRegion.SECTOR_BYTES);
      final var length = this.file.readInt();
      if (length > AnvilRegion.SECTOR_BYTES * numSectors) {
        return null;
      }
      final var version = this.file.readByte();
      if (version == AnvilRegion.VERSION_GZIP) {
        final var data = new byte[length - 1];
        this.file.read(data);
        return new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
      }
      if (version == AnvilRegion.VERSION_DEFLATE) {
        final var data = new byte[length - 1];
        this.file.read(data);
        return new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
      }
      return null;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * obtains the chunk data output stream at {@code x} and {@code z}.
   *
   * @param x the x to get.
   * @param z the z to get.
   *
   * @return obtained chunk data out put stream.
   */
  @NotNull
  public DataOutputStream getChunkDataOutputStream(final int x, final int z) {
    if (this.outOfBounds(x, z)) {
      throw new ArrayIndexOutOfBoundsException("Chunk couldn't get!");
    }
    return new DataOutputStream(new DeflaterOutputStream(new ChunkBuffer(x, z)));
  }

  /**
   * obtains region's x.
   *
   * @return region's x.
   */
  public int getRegionX() {
    return this.regionX;
  }

  /**
   * obtains region's z.
   *
   * @return region's z.
   */
  public int getRegionZ() {
    return this.regionZ;
  }

  /**
   * checks if the region has chunk at {@code x} and {@code z}.
   *
   * @param x the x to check.
   * @param z the z to check.
   *
   * @return {@code true} if region has the chunk at specified x and z coordinates.
   */
  public boolean hasChunk(final int x, final int z) {
    return this.getOffset(x, z) != 0;
  }

  /**
   * writes the given {@code data} into the file.
   *
   * @param x the x to write.
   * @param z the z to write.
   * @param data the data to write.
   * @param length the length to write.
   */
  public synchronized void write(final int x, final int z, final byte[] data, final int length) {
    try {
      final var offset = this.getOffset(x, z);
      var sectorNumber = offset >> 8;
      final var sectorsAllocated = offset & 0xFF;
      final var sectorsNeeded = (length + AnvilRegion.CHUNK_HEADER_SIZE) / AnvilRegion.SECTOR_BYTES + 1;
      if (sectorsNeeded >= 256) {
        return;
      }
      if (sectorNumber != 0 && sectorsAllocated == sectorsNeeded) {
        this.write(sectorNumber, data, length);
      } else {
        for (var i = 0; i < sectorsAllocated; ++i) {
          this.sectorFree.set(sectorNumber + i, true);
        }
        var runStart = this.sectorFree.indexOf(true);
        var runLength = 0;
        if (runStart != -1) {
          for (var i = runStart; i < this.sectorFree.size(); ++i) {
            if (runLength != 0) {
              if (this.sectorFree.get(i)) {
                runLength++;
              } else {
                runLength = 0;
              }
            } else if (this.sectorFree.get(i)) {
              runStart = i;
              runLength = 1;
            }
            if (runLength >= sectorsNeeded) {
              break;
            }
          }
        }
        if (runLength >= sectorsNeeded) {
          sectorNumber = runStart;
          this.setOffset(x, z, sectorNumber << 8 | sectorsNeeded);
          for (var i = 0; i < sectorsNeeded; ++i) {
            this.sectorFree.set(sectorNumber + i, false);
          }
          this.write(sectorNumber, data, length);
        } else {
          this.file.seek(this.file.length());
          sectorNumber = this.sectorFree.size();
          for (var i = 0; i < sectorsNeeded; ++i) {
            this.file.write(AnvilRegion.emptySector);
            this.sectorFree.add(false);
          }
          this.write(sectorNumber, data, length);
          this.setOffset(x, z, sectorNumber << 8 | sectorsNeeded);
        }
      }
      this.setTimestamp(x, z, (int) (System.currentTimeMillis() / 1000L));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private int getOffset(final int x, final int z) {
    return this.offsets[x + (z << 5)];
  }

  private boolean outOfBounds(final int x, final int z) {
    return x < 0 || x >= 32 || z < 0 || z >= 32;
  }

  private void setOffset(final int x, final int z, final int offset) throws IOException {
    this.offsets[x + (z << 5)] = offset;
    this.file.seek((long) x + ((long) z << 5) << 2);
    this.file.writeInt(offset);
  }

  private void setTimestamp(final int x, final int z, final int value) throws IOException {
    this.file.seek(AnvilRegion.SECTOR_BYTES + ((long) x + ((long) z << 5) << 2));
    this.file.writeInt(value);
  }

  private void write(final int sectorNumber, final byte[] data, final int length) throws IOException {
    this.file.seek((long) sectorNumber << 12);
    this.file.writeInt(length + 1);
    this.file.writeByte(AnvilRegion.VERSION_DEFLATE);
    this.file.write(data, 0, length);
  }

  final class ChunkBuffer extends ByteArrayOutputStream {

    private final int x;

    private final int z;

    public ChunkBuffer(final int x, final int z) {
      super(8096);
      this.x = x;
      this.z = z;
    }

    @Override
    public void close() {
      AnvilRegion.this.write(this.x, this.z, this.buf, this.count);
    }
  }
}
