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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.DataFormatException;
import lombok.extern.log4j.Log4j2;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import org.jetbrains.annotations.NotNull;

/**
 * a class that serializes and deserializes packets.
 */
@Log4j2
public final class Protocol {

  /**
   * the zlib.
   */
  private static final Zlib ZLIB = Zlib.RAW;

  /**
   * ctor.
   */
  private Protocol() {
  }

  /**
   * deserializes the given {@code compressed}.
   *
   * @param handler the handler to handle.
   * @param compressed the compressed to deserialize.
   */
  public static void deserialize(@NotNull final PacketHandler handler, @NotNull final ByteBuf compressed) {
    ByteBuf decompressed = null;
    try {
      decompressed = Protocol.ZLIB.inflate(compressed, 12 * 1024 * 1024);
      while (decompressed.isReadable()) {
        final var length = VarInts.readUnsignedVarInt(decompressed);
        final var buffer = decompressed.readSlice(length);
        if (!buffer.isReadable()) {
          throw new DataFormatException("Packet cannot be empty!");
        }
        final var header = VarInts.readUnsignedVarInt(buffer);
        final var packetId = header & 0x3ff;
        Protocol.log.debug("§7Incoming packet id -> {}", packetId);
        final var shirukaPacket = Objects.requireNonNull(PacketRegistry.PACKETS.get(packetId),
          String.format("The packet id %s not found!", packetId)).apply(buffer);
        shirukaPacket.setSenderId(header >>> 10 & 3);
        shirukaPacket.setClientId(header >>> 12 & 3);
        shirukaPacket.decode();
        shirukaPacket.handle(handler);
      }
    } catch (final Exception e) {
      JiraExceptionCatcher.serverException(e);
    } finally {
      if (decompressed != null) {
        decompressed.release();
      }
    }
  }

  /**
   * serializes the given {@code packet}.
   *
   * @param result the result.
   * @param packets the packets to serialize.
   * @param level the level to serialize.
   */
  public static void serialize(@NotNull final ByteBuf result, @NotNull final Collection<ShirukaPacket> packets,
                               final int level) {
    final var uncompressed = Unpooled.buffer(packets.size() << 3);
    try {
      for (final var packet : packets) {
        final var buffer = Unpooled.buffer();
        try {
          final var packetId = packet.getId();
          var header = 0;
          header |= packetId & 0x3ff;
          header |= (packet.getSenderId() & 3) << 10;
          header |= (packet.getClientId() & 3) << 12;
          VarInts.writeUnsignedInt(buffer, header);
          packet.setBuffer(buffer);
          packet.encode();
          VarInts.writeUnsignedInt(uncompressed, buffer.readableBytes());
          uncompressed.writeBytes(buffer);
        } finally {
          buffer.release();
        }
      }
      Protocol.ZLIB.deflate(uncompressed, result, level);
    } finally {
      uncompressed.release();
    }
  }
}
