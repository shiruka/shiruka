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

package net.shiruka.shiruka.network;

import com.nukkitx.natives.util.Natives;
import com.nukkitx.natives.zlib.Deflater;
import com.nukkitx.natives.zlib.Inflater;
import com.whirvis.jraknet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.zip.DataFormatException;
import org.jetbrains.annotations.NotNull;

/**
 * a class that implementation of Zlib natives.
 */
public final class Zlib {

  /**
   * the raw.
   */
  static final Zlib RAW = new Zlib(true);

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

  /**
   * deflates the given {@code uncompressed} byte byf.
   *
   * @param uncompressed the uncompressed to deflate.
   * @param compressed the compressed to deflate.
   * @param level the level to deflate.
   */
  void deflate(@NotNull final Packet uncompressed, @NotNull final Packet compressed, final int level) {
    ByteBuf destination = null;
    ByteBuf source = null;
    try {
      if (!uncompressed.buffer().isDirect()) {
        source = ByteBufAllocator.DEFAULT.ioBuffer();
        source.writeBytes(uncompressed.buffer());
      } else {
        source = uncompressed.buffer();
      }
      if (!compressed.buffer().isDirect()) {
        destination = ByteBufAllocator.DEFAULT.ioBuffer();
      } else {
        destination = compressed.buffer();
      }
      final var deflated = this.deflaterLocal.get();
      deflated.reset();
      deflated.setLevel(level);
      deflated.setInput(source.internalNioBuffer(source.readerIndex(), source.readableBytes()));
      while (!deflated.finished()) {
        final var index = destination.writerIndex();
        destination.ensureWritable(Zlib.CHUNK);
        final var written = deflated.deflate(destination.internalNioBuffer(index, Zlib.CHUNK));
        destination.writerIndex(index + written);
      }
      if (destination != compressed.buffer()) {
        compressed.buffer().writeBytes(destination);
      }
    } finally {
      if (source != null && source != uncompressed.buffer()) {
        source.release();
      }
      if (destination != null && destination != compressed.buffer()) {
        destination.release();
      }
    }
  }

  /**
   * inflates the given {@code packet} byte byf.
   *
   * @param packet the packet to inflate.
   * @param maxSize the maximum size to inflate.
   *
   * @return inflated byte buf instance.
   *
   * @throws DataFormatException if inflated data exceeds maximum size.
   */
  @NotNull
  Packet inflate(@NotNull final Packet packet, final int maxSize) throws DataFormatException {
    ByteBuf source = null;
    final var decompressed = new Packet(ByteBufAllocator.DEFAULT.ioBuffer());
    final var buffer = packet.buffer();
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
        decompressed.buffer().ensureWritable(Zlib.CHUNK);
        final var index = decompressed.buffer().writerIndex();
        final var written = inflater.inflate(decompressed.buffer().internalNioBuffer(index, Zlib.CHUNK));
        decompressed.buffer().writerIndex(index + written);
        if (maxSize > 0 && decompressed.buffer().writerIndex() >= maxSize) {
          throw new DataFormatException("Inflated data exceeds maximum size!");
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
