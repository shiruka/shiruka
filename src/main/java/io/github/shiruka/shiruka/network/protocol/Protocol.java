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

package io.github.shiruka.shiruka.network.protocol;

import com.google.common.base.Preconditions;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.entity.ShirukaPlayerConnection;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.packet.PacketBound;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.packet.PacketRegistry;
import io.github.shiruka.shiruka.network.util.Zlib;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.zip.DataFormatException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class that serializes and deserializes packets.
 *
 * @todo #1:5m Add JavaDocs.
 */
public final class Protocol {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("Protocol");

  /**
   * the zlib.
   */
  private static final Zlib ZLIB = Zlib.RAW;

  /**
   * ctor.
   */
  private Protocol() {
  }

  public static void deserialize(@NotNull final ByteBuf buf, @NotNull final ShirukaPlayerConnection connection) {
    ByteBuf decompressed = null;
    try {
      decompressed = Protocol.ZLIB.inflate(buf, 12 * 1024 * 1024);
      while (decompressed.isReadable()) {
        final var length = VarInts.readUnsignedVarInt(decompressed);
        final var packetBuffer = decompressed.readSlice(length);
        if (!packetBuffer.isReadable()) {
          throw new DataFormatException("Packet cannot be empty");
        }
        try {
          final var header = VarInts.readUnsignedVarInt(packetBuffer);
          final var packetId = header & 0x3ff;
          Protocol.LOGGER.debug("Incoming packet id -> {}", packetId);
          final var cls = PacketRegistry.byId(connection.getState(),
            PacketBound.SERVER, packetId);
          Preconditions.checkArgument(cls != null, "Packet with %s not found!", packetId);
          final var packet = PacketRegistry.makeIn(cls);
          packet.read(packetBuffer, connection);
        } catch (final InvocationTargetException | InstantiationException |
          IllegalAccessException e) {
          Protocol.LOGGER.debug("Error occurred whilst decoding packet", e);
          Protocol.LOGGER.debug("Packet contents\n{}", ByteBufUtil.prettyHexDump(packetBuffer.readerIndex(0)));
        }
      }
    } catch (final DataFormatException e) {
      throw new RuntimeException("Unable to inflate buffer data", e);
    } finally {
      if (decompressed != null) {
        decompressed.release();
      }
    }
  }

  public static void serialize(@NotNull final ByteBuf buffer, @NotNull final Collection<PacketOut> packets,
                               final int level) {
    final var uncompressed = ByteBufAllocator.DEFAULT.ioBuffer(packets.size() << 3);
    try {
      for (final var packet : packets) {
        final var packetBuffer = ByteBufAllocator.DEFAULT.ioBuffer();
        try {
          final var id = packet.id();
          var header = 0;
          header |= id & 0x3ff;
          VarInts.writeUnsignedInt(packetBuffer, header);
          packet.write(packetBuffer);
          VarInts.writeUnsignedInt(uncompressed, packetBuffer.readableBytes());
          uncompressed.writeBytes(packetBuffer);
        } finally {
          packetBuffer.release();
        }
      }
      Protocol.ZLIB.deflate(uncompressed, buffer, level);
    } finally {
      uncompressed.release();
    }
  }
}
