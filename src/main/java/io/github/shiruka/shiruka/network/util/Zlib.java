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

package io.github.shiruka.shiruka.network.util;

import com.nukkitx.natives.util.Natives;
import com.nukkitx.natives.zlib.Deflater;
import com.nukkitx.natives.zlib.Inflater;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.zip.DataFormatException;
import org.jetbrains.annotations.NotNull;

/**
 * a class that implementation of Zlib natives.
 */
public final class Zlib {

  /**
   * the default.
   */
  public static final Zlib DEFAULT = new Zlib(false);

  /**
   * the raw.
   */
  public static final Zlib RAW = new Zlib(true);

  /**
   * the chunk.
   */
  private static final int CHUNK = 8192;

  /**
   * the deflated.
   */
  @NotNull
  private final ThreadLocal<Deflater> deflaterLocal;

  /**
   * the inflated.
   */
  @NotNull
  private final ThreadLocal<Inflater> inflaterLocal;

  /**
   * ctor.
   *
   * @param raw the raw.
   */
  private Zlib(final boolean raw) {
    this.inflaterLocal = ThreadLocal.withInitial(() -> Natives.ZLIB.get().create(raw));
    this.deflaterLocal = ThreadLocal.withInitial(() -> Natives.ZLIB.get().create(7, raw));
  }

  public void deflate(@NotNull final ByteBuf uncompressed, @NotNull final ByteBuf compressed, final int level) {
    ByteBuf destination = null;
    ByteBuf source = null;
    try {
      if (!uncompressed.isDirect()) {
        source = ByteBufAllocator.DEFAULT.ioBuffer();
        source.writeBytes(uncompressed);
      } else {
        source = uncompressed;
      }
      if (!compressed.isDirect()) {
        destination = ByteBufAllocator.DEFAULT.ioBuffer();
      } else {
        destination = compressed;
      }
      final var deflater = this.deflaterLocal.get();
      deflater.reset();
      deflater.setLevel(level);
      deflater.setInput(source.internalNioBuffer(source.readerIndex(), source.readableBytes()));
      while (!deflater.finished()) {
        final var index = destination.writerIndex();
        destination.ensureWritable(Zlib.CHUNK);
        final var written = deflater.deflate(destination.internalNioBuffer(index, Zlib.CHUNK));
        destination.writerIndex(index + written);
      }
      if (destination != compressed) {
        compressed.writeBytes(destination);
      }
    } finally {
      if (source != null && source != uncompressed) {
        source.release();
      }
      if (destination != null && destination != compressed) {
        destination.release();
      }
    }
  }

  @NotNull
  public ByteBuf inflate(@NotNull final ByteBuf buffer, final int maxSize) throws DataFormatException {
    ByteBuf source = null;
    final var decompressed = ByteBufAllocator.DEFAULT.ioBuffer();
    try {
      if (!buffer.isDirect()) {
        final ByteBuf temporary = ByteBufAllocator.DEFAULT.ioBuffer();
        temporary.writeBytes(buffer);
        source = temporary;
      } else {
        source = buffer;
      }
      final var inflater = this.inflaterLocal.get();
      inflater.reset();
      inflater.setInput(source.internalNioBuffer(source.readerIndex(), source.readableBytes()));
      inflater.finished();
      while (!inflater.finished()) {
        decompressed.ensureWritable(Zlib.CHUNK);
        final var index = decompressed.writerIndex();
        final var written = inflater.inflate(decompressed.internalNioBuffer(index, Zlib.CHUNK));
        decompressed.writerIndex(index + written);
        if (maxSize > 0 && decompressed.writerIndex() >= maxSize) {
          throw new DataFormatException("Inflated data exceeds maximum size");
        }
      }
      return decompressed;
    } catch (final DataFormatException e) {
      decompressed.release();
      throw e;
    } finally {
      if (source != null && source != buffer) {
        source.release();
      }
    }
  }
}
