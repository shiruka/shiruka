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

import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.DataFormatException;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.packet.PacketOut;
import net.shiruka.shiruka.network.packet.PacketRegistry;
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
   * @param connection the connection to deserialize.
   */
  public static void deserialize(@NotNull final PacketHandler handler, @NotNull final RakNetPacket packet,
                                 @NotNull final RakNetClientPeer connection) {
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
        shirukaPacket.handle(handler, connection);
      }
    } catch (final DataFormatException e) {
      JiraExceptionCatcher.serverException(e);
    } finally {
      packet.release();
    }
  }

  /**
   * serializes the given {@code buf}.
   *
   * @param buffer the buf to serialize.
   * @param packets the packets to serialize.
   * @param level the level to serialize.
   */
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
