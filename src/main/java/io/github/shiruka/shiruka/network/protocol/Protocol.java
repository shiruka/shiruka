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

import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.entity.Player;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.exceptions.PacketSerializeException;
import io.github.shiruka.shiruka.network.packet.PacketBound;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.github.shiruka.shiruka.network.packet.PacketRegistry;
import io.github.shiruka.shiruka.network.util.Zlib;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.util.zip.DataFormatException;
import org.jetbrains.annotations.NotNull;

/**
 * a class that serializes and deserializes packets.
 */
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

  public static void deserialize(@NotNull final ByteBuf buf, @NotNull final Player player) {
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
          final var cls = PacketRegistry.byId(player.getPlayerConnection().getState(),
            PacketBound.SERVER, packetId);
          if (cls == null) {
            throw new RuntimeException(String.format("Packet with %s not found!", packetId));
          }
          final PacketIn packet = PacketRegistry.make(cls);
          packet.setSenderId(header >>> 10 & 3);
          packet.setClientId(header >>> 12 & 3);
          packet.read(packetBuffer, player);
        } catch (final PacketSerializeException e) {
          Loggers.debug("Error occurred whilst decoding packet", e);
          Loggers.debug("Packet contents\n{}", ByteBufUtil.prettyHexDump(packetBuffer.readerIndex(0)));
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
}
