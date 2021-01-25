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

package net.shiruka.shiruka.network.protocol;

import com.whirvis.jraknet.Packet;
import com.whirvis.jraknet.RakNetPacket;
import io.netty.buffer.ByteBufAllocator;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.DataFormatException;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.packet.PacketRegistry;
import net.shiruka.shiruka.network.packet.ShirukaPacket;
import net.shiruka.shiruka.network.util.VarInts;
import net.shiruka.shiruka.network.util.Zlib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that serializes and deserializes packets.
 */
public final class Protocol {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Protocol");

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
   * deserializes the given {@code packet}.
   *
   * @param handler the handler to handle.
   * @param packet the packet to deserialize.
   */
  public static void deserialize(@NotNull final PacketHandler handler, @NotNull final RakNetPacket packet) {
    try {
      Protocol.ZLIB.inflate(packet, 12 * 1024 * 1024);
      while (packet.buffer().isReadable()) {
        final var length = (int) packet.readUnsignedVarInt();
        packet.setBuffer(packet.buffer().readSlice(length));
        if (!packet.buffer().isReadable()) {
          throw new DataFormatException("Packet cannot be empty!");
        }
        final var header = (int) packet.readUnsignedVarInt();
        final var packetId = header & 0x3ff;
        Protocol.LOGGER.debug("§7Incoming packet id -> {}", packetId);
        final var shirukaPacket = Objects.requireNonNull(PacketRegistry.PACKETS.get(packetId),
          String.format("The packet id %s not found!", packetId)).apply(packet);
        shirukaPacket.decode();
        shirukaPacket.handle(handler);
      }
    } catch (final DataFormatException e) {
      JiraExceptionCatcher.serverException(e);
    } finally {
      packet.release();
    }
  }

  /**
   * serializes the given {@code packet}.
   *
   * @param packets the packets to serialize.
   * @param level the level to serialize.
   *
   * @return serialized packet.
   */
  @NotNull
  public static Packet serialize(@NotNull final Collection<ShirukaPacket> packets, final int level) {
    final var finalPacket = new Packet();
    final var uncompressed = ByteBufAllocator.DEFAULT.ioBuffer(packets.size() << 3);
    try {
      for (final var packet : packets) {
        final var temp = ByteBufAllocator.DEFAULT.ioBuffer();
        try {
          final var id = packet.getId();
          var header = 0;
          header |= id & 0x3ff;
          VarInts.writeUnsignedInt(temp, header);
          packet.setBuffer(temp);
          packet.encode();
          VarInts.writeUnsignedInt(uncompressed, temp.readableBytes());
          uncompressed.writeBytes(temp);
        } finally {
          temp.release();
        }
      }
      Protocol.ZLIB.deflate(uncompressed, finalPacket, level);
    } finally {
      uncompressed.release();
    }
    return finalPacket;
  }
}
