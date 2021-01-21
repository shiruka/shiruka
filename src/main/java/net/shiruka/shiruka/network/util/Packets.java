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

package net.shiruka.shiruka.network.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AsciiString;
import java.net.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.shiruka.shiruka.network.Socket;
import net.shiruka.shiruka.network.*;
import net.shiruka.shiruka.network.packets.PacketOutPackInfo;
import net.shiruka.shiruka.network.packets.PacketOutPackStack;
import net.shiruka.shiruka.network.server.ServerSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains packet constants and utility methods.
 *
 * @todo #1:5m Add more JavaDocs packet id constants and others.
 */
public final class Packets {

  public static final byte CONNECTED_PING = 0x00;

  public static final byte CONNECTED_PONG = 0x03;

  public static final byte CONNECTION_REQUEST = 0x09;

  public static final byte DETECT_LOST_CONNECTION = 0x04;

  public static final byte DISCONNECTION_NOTIFICATION = 0x15;

  public static final byte NEW_INCOMING_CONNECTION = 0x13;

  public static final byte OPEN_CONNECTION_REQUEST_2 = 0x07;

  public static final byte UNCONNECTED_PING_OPEN_CONNECTION = 0x02;

  public static final short USER_PACKET_ENUM = 0x80;

  private static final byte AF_INET6 = 23;

  private static final byte ALREADY_CONNECTED = 0x12;

  private static final byte CONNECTION_BANNED = 0x17;

  private static final byte CONNECTION_REQUEST_ACCEPTED = 0x10;

  private static final byte CONNECTION_REQUEST_FAILED = 0x11;

  private static final byte INCOMPATIBLE_PROTOCOL_VERSION = 0x19;

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Packets");

  private static final byte MAXIMUM_CONNECTION = 0x14;

  private static final byte OPEN_CONNECTION_REPLY_1 = 0x06;

  private static final byte OPEN_CONNECTION_REPLY_2 = 0x08;

  private static final byte OPEN_CONNECTION_REQUEST_1 = 0x05;

  private static final byte UNCONNECTED_PING = 0x01;

  private static final byte UNCONNECTED_PONG = 0x1C;

  /**
   * ctor.
   */
  private Packets() {
  }

  /**
   * handles packets that are not using the connection a.k.a. session.
   *
   * @param ctx the context to handle.
   * @param server the server to handle.
   * @param packet the packet to handle.
   *
   * @return true if the packet id is {@link Packets#UNCONNECTED_PING} or {@link Packets#OPEN_CONNECTION_REQUEST_1}
   */
  public static boolean handleNoConnectionPackets(@NotNull final ChannelHandlerContext ctx,
                                                  @NotNull final ServerSocket server,
                                                  @NotNull final DatagramPacket packet) {
    final var content = packet.content();
    final var packetId = content.readByte();
    if (packetId == Packets.UNCONNECTED_PING) {
      Packets.handleUnconnectedPing(ctx, server, packet);
      return true;
    }
    if (packetId == Packets.OPEN_CONNECTION_REQUEST_1) {
      Packets.handleOpenConnectionRequest1(ctx, server, packet);
      return true;
    }
    return false;
  }

  /**
   * reads the given buffer to parse as {@link InetSocketAddress}.
   *
   * @param buffer the buffer to parse.
   *
   * @return an address from the given buffer instance.
   */
  @NotNull
  public static InetSocketAddress readAddress(@NotNull final ByteBuf buffer) {
    final var type = buffer.readByte();
    final InetAddress address;
    final int port;
    try {
      if (type == 4) {
        final var addressBytes = new byte[4];
        buffer.readBytes(addressBytes);
        Misc.flip(addressBytes);
        address = InetAddress.getByAddress(addressBytes);
        port = buffer.readUnsignedShort();
      } else if (type == 6) {
        buffer.readShortLE();
        port = buffer.readUnsignedShort();
        buffer.readInt();
        final var addressBytes = new byte[16];
        buffer.readBytes(addressBytes);
        final var scopeId = buffer.readInt();
        address = Inet6Address.getByAddress(null, addressBytes, scopeId);
      } else {
        throw new UnsupportedOperationException("Unknown Internet Protocol version.");
      }
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException(e);
    }
    return new InetSocketAddress(address, port);
  }

  /**
   * reads the given buffer and returns {@link AsciiString}.
   *
   * @param buffer the buffer to read.
   *
   * @return an {@link AsciiString} instance.
   */
  @NotNull
  public static AsciiString readLEAsciiString(@NotNull final ByteBuf buffer) {
    final var length = buffer.readIntLE();
    final var bytes = new byte[length];
    buffer.readBytes(bytes);
    return new AsciiString(bytes);
  }

  /**
   * sends connected ping packet to the connection.
   *
   * @param connection the connection to send.
   * @param pingTime the pingTime to send as ping.
   */
  public static void sendConnectedPing(@NotNull final Connection<?> connection, final long pingTime) {
    Packets.createPacket(connection, 9, packet -> {
      packet.writeByte(Packets.CONNECTED_PING);
      packet.writeLong(pingTime);
      connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
      connection.getCurrentPingTime().set(pingTime);
    });
  }

  /**
   * send a connected pong packet to the connection.
   *
   * @param connection the connection to send.
   * @param pingTime the pint time to send.
   */
  public static void sendConnectedPong(@NotNull final Connection<?> connection, final long pingTime) {
    Packets.createPacket(connection, 17, packet -> {
      packet.writeByte(Packets.CONNECTED_PONG);
      packet.writeLong(pingTime);
      packet.writeLong(System.currentTimeMillis());
      connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
    });
  }

  /**
   * sends connection reply 1 packet to the connection's address.
   *
   * @param connection the connection to send.
   */
  public static void sendConnectionReply1(@NotNull final Connection<?> connection) {
    Packets.createPacket(connection, 28, newPacket -> {
      newPacket.writeByte(Packets.OPEN_CONNECTION_REPLY_1);
      Packets.writeUnconnectedMagic(newPacket);
      newPacket.writeLong(connection.getSocket().getUniqueId());
      newPacket.writeBoolean(false);
      newPacket.writeShort(connection.getMtu());
      connection.sendDirect(newPacket);
    });
  }

  /**
   * send the connection request accepted packet to the connection.
   *
   * @param connection the connection to send.
   * @param time the time to send.
   */
  public static void sendConnectionRequestAccepted(@NotNull final Connection<?> connection, final long time) {
    final var address = connection.getAddress();
    final var ipv6 = address.getAddress() instanceof Inet6Address;
    Packets.createPacket(connection, ipv6 ? 628 : 166, packet -> {
      packet.writeByte(Packets.CONNECTION_REQUEST_ACCEPTED);
      Packets.writeAddress(packet, address);
      packet.writeShort(0);
      Arrays.stream(ipv6 ? Misc.LOCAL_IP_ADDRESSES_V6 : Misc.LOCAL_IP_ADDRESSES_V4)
        .forEach(socketAddress -> Packets.writeAddress(packet, socketAddress));
      packet.writeLong(time);
      packet.writeLong(System.currentTimeMillis());
      connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
    });
  }

  /**
   * sends the connection request failed packet to the connection.
   *
   * @param connection the connection to send.
   */
  public static void sendConnectionRequestFailed(@NotNull final Connection<?> connection) {
    Packets.createPacket(connection, 21, packet -> {
      packet.writeByte(Packets.CONNECTION_REQUEST_FAILED);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(connection.getSocket().getUniqueId());
      connection.sendDirect(packet);
    });
  }

  /**
   * sends disconnection packet to the connection.
   *
   * @param connection the connection to send.
   */
  public static void sendDisconnectionNotification(@NotNull final Connection<?> connection) {
    Packets.createPacket(connection, 1, packet -> {
      packet.writeByte(Packets.DISCONNECTION_NOTIFICATION);
      connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE_ORDERED);
    });
  }

  /**
   * sends the open connection reply 2 packet to the connection.
   *
   * @param connection the connection to send.
   */
  public static void sendOpenConnectionReply2(@NotNull final Connection<?> connection) {
    Packets.createPacket(connection, 31, packet -> {
      packet.writeByte(Packets.OPEN_CONNECTION_REPLY_2);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(connection.getSocket().getUniqueId());
      Packets.writeAddress(packet, connection.getAddress());
      packet.writeShort(connection.getMtu());
      packet.writeBoolean(false);
      connection.sendDirect(packet);
    });
  }

  /**
   * verifies the magic numbers from the byte array.
   *
   * @param buffer the buffer to check.
   *
   * @return returns true if the given buffer does not equal {@link Constants#UNCONNECTED_MAGIC}.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean verifyUnconnectedMagic(@NotNull final ByteBuf buffer) {
    final var bytes = new byte[Constants.UNCONNECTED_MAGIC.length];
    buffer.readBytes(bytes);
    return Arrays.equals(bytes, Constants.UNCONNECTED_MAGIC);
  }

  /**
   * writes the give {@code buffer} from the give {@code array}.
   *
   * @param buffer the buffer to write.
   * @param array the array to write.
   * @param biConsumer the bi consumer to write.
   * @param <T> type of the value.
   */
  public static <T> void writeArray(@NotNull final ByteBuf buffer, @NotNull final Collection<T> array,
                                    @NotNull final BiConsumer<ByteBuf, T> biConsumer) {
    VarInts.writeUnsignedInt(buffer, array.size());
    array.forEach(val -> biConsumer.accept(buffer, val));
  }

  /**
   * writes the given array to the buffer.
   *
   * @param buffer the buffer to write.
   * @param array the array to write.
   * @param consumer the consumer to write.
   * @param <T> the type of the array.
   */
  public static <T> void writeArrayShortLE(@NotNull final ByteBuf buffer, @NotNull final Collection<T> array,
                                           @NotNull final BiConsumer<ByteBuf, T> consumer) {
    buffer.writeShortLE(array.size());
    array.forEach(val -> consumer.accept(buffer, val));
  }

  /**
   * writes the given entry into the buffer.
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeEntry(@NotNull final ByteBuf buffer, @NotNull final PacketOutPackStack.Entry entry) {
    VarInts.writeString(buffer, entry.getPackId());
    VarInts.writeString(buffer, entry.getPackVersion());
    VarInts.writeString(buffer, entry.getSubPackName());
  }

  /**
   * writes the given entry to the buffer.
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeEntry(@NotNull final ByteBuf buffer, @NotNull final PacketOutPackInfo.Entry entry) {
    VarInts.writeString(buffer, entry.getPackId());
    VarInts.writeString(buffer, entry.getPackVersion());
    buffer.writeLongLE(entry.getPackSize());
    VarInts.writeString(buffer, entry.getContentKey());
    VarInts.writeString(buffer, entry.getSubPackName());
    VarInts.writeString(buffer, entry.getContentId());
    buffer.writeBoolean(entry.isScripting());
  }

  /**
   * writes the given experiments into the buffer.
   *
   * @param buffer the buffer to write.
   * @param experiments the experiment to write.
   */
  public static void writeExperiments(@NotNull final ByteBuf buffer,
                                      @NotNull final List<PacketOutPackStack.ExperimentData> experiments) {
    buffer.writeIntLE(experiments.size());
    experiments.forEach(experiment -> {
      VarInts.writeString(buffer, experiment.getName());
      buffer.writeBoolean(experiment.isEnabled());
    });
  }

  /**
   * writes the given resource pack entry to
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeResourcePackEntry(@NotNull final ByteBuf buffer,
                                            @NotNull final PacketOutPackInfo.Entry entry) {
    Packets.writeEntry(buffer, entry);
    buffer.writeBoolean(entry.isRaytracingCapable());
  }

  /**
   * creates a packet from the given size and runs the given packet consumer.
   *
   * @param initialSize the initial size to create.
   * @param packet the packet to run.
   */
  private static void createPacket(@NotNull final ChannelHandlerContext ctx, final int initialSize,
                                   @NotNull final Consumer<ByteBuf> packet) {
    packet.accept(ctx.alloc().ioBuffer(initialSize));
  }

  /**
   * creates a packet from the given size and runs the given packet consumer.
   *
   * @param initialSize the initial size to create.
   * @param maxCapacity the maximum capacity size to create.
   * @param packet the packet to run.
   */
  private static void createPacket(@NotNull final ChannelHandlerContext ctx, final int initialSize,
                                   final int maxCapacity, @NotNull final Consumer<ByteBuf> packet) {
    packet.accept(ctx.alloc().ioBuffer(initialSize, maxCapacity));
  }

  /**
   * creates a packet from the given size and runs the given packet consumer.
   *
   * @param size the size to create.
   * @param packet the packet to run.
   */
  private static void createPacket(@NotNull final Connection<?> connection, final int size,
                                   @NotNull final Consumer<ByteBuf> packet) {
    packet.accept(connection.allocateBuffer(size));
  }

  /**
   * handles open connection request 1 packet.
   *
   * @param ctx the context to handle.
   * @param server the server to handle.
   * @param packet the packet to handle.
   */
  private static void handleOpenConnectionRequest1(@NotNull final ChannelHandlerContext ctx,
                                                   @NotNull final ServerSocket server,
                                                   @NotNull final DatagramPacket packet) {
    final var content = packet.content();
    if (!content.isReadable(16)) {
      Packets.LOGGER.error("Invalid packet content for unconnected ping.");
      return;
    }
    if (!Packets.verifyUnconnectedMagic(content)) {
      Packets.LOGGER.error("Invalid magic number for unconnected ping.");
      return;
    }
    final var protocolVersion = content.readUnsignedByte();
    final var mtu = content.readableBytes() + 1 + 16 + 1 +
      Misc.getIpHeader(packet.sender()) + Constants.UDP_HEADER_SIZE;
    final var recipient = packet.sender();
    final var connection = server.getConnectionsByAddress().get(recipient);
    if (connection != null && connection.getState() == ConnectionState.CONNECTED) {
      Packets.LOGGER.error("{} is already connected!", recipient);
      Packets.LOGGER.debug("Sending already connected packet.");
      Packets.sendAlreadyConnected(ctx, server, recipient);
      return;
    }
    if (Constants.MOJANG_PROTOCOL_VERSION != protocolVersion) {
      Packets.LOGGER.error("Incompatible protocol version from {}!", recipient);
      Packets.LOGGER.debug("Sending incompatible protocol version packet.");
      Packets.sendIncompatibleProtocolVersion(ctx, server, recipient);
      return;
    }
    if (server.getMaxConnections() >= 0 && server.getMaxConnections() <= server.getConnectionsByAddress().size()) {
      Packets.LOGGER.error("Reached Maximum connection size!");
      Packets.LOGGER.debug("Sending maximum connection packet.");
      Packets.sendMaximumConnection(ctx, server, recipient);
      return;
    }
    if (!server.getServerListener().onConnect(recipient)) {
      Packets.LOGGER.error("{} can't connect to the server!", recipient);
      Packets.LOGGER.debug("Sending connection banned packet.");
      Packets.sendConnectedBanned(ctx, server, recipient);
      return;
    }
    if (connection != null) {
      Packets.sendConnectionReply1(connection);
      return;
    }
    server.createNewConnection(recipient, ctx, mtu, protocolVersion);
  }

  /**
   * handles unconnected ping packet.
   *
   * @param ctx the context to handle.
   * @param server the server to handle.
   * @param packet the packet to handle.
   */
  private static void handleUnconnectedPing(@NotNull final ChannelHandlerContext ctx,
                                            @NotNull final ServerSocket server,
                                            @NotNull final DatagramPacket packet) {
    final var content = packet.content();
    if (!content.isReadable(24)) {
      Packets.LOGGER.error("Invalid packet content for unconnected ping.");
      return;
    }
    final var pingTime = content.readLong();
    if (!Packets.verifyUnconnectedMagic(content)) {
      Packets.LOGGER.error("Invalid magic number for unconnected ping.");
      return;
    }
    Packets.sendUnconnectedPongPacket(ctx, server, packet.sender(), pingTime);
  }

  /**
   * sends already connected packet to the recipient.
   *
   * @param ctx the context to send.
   * @param server the server to send unique id.
   * @param recipient the recipient to send.
   */
  private static void sendAlreadyConnected(@NotNull final ChannelHandlerContext ctx, @NotNull final ServerSocket server,
                                           @NotNull final InetSocketAddress recipient) {
    Packets.createPacket(ctx, 25, 25, packet -> {
      packet.writeByte(Packets.ALREADY_CONNECTED);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(server.getUniqueId());
      Socket.sendWithPromise(ctx, packet, recipient);
    });
  }

  /**
   * sends connection banned to the recipient.
   *
   * @param ctx the context to send.
   * @param server the server to send unique id.
   * @param recipient the recipient to send.
   */
  private static void sendConnectedBanned(@NotNull final ChannelHandlerContext ctx, @NotNull final ServerSocket server,
                                          @NotNull final InetSocketAddress recipient) {
    Packets.createPacket(ctx, 25, 25, packet -> {
      packet.writeByte(Packets.CONNECTION_BANNED);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(server.getUniqueId());
      Socket.send(ctx, packet, recipient);
    });
  }

  /**
   * sends incompatible protocol version packet to the recipient.
   *
   * @param ctx the context to send.
   * @param server the server to send unique id.
   * @param recipient the recipient to send.
   */
  private static void sendIncompatibleProtocolVersion(@NotNull final ChannelHandlerContext ctx,
                                                      @NotNull final ServerSocket server,
                                                      @NotNull final InetSocketAddress recipient) {
    Packets.createPacket(ctx, 26, 26, packet -> {
      packet.writeByte(Packets.INCOMPATIBLE_PROTOCOL_VERSION);
      packet.writeByte(Constants.MOJANG_PROTOCOL_VERSION);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(server.getUniqueId());
      Socket.sendWithPromise(ctx, packet, recipient);
    });
  }

  /**
   * sends maximum connection packet to the recipient.
   *
   * @param ctx the context to send.
   * @param server the server to send unique id.
   * @param recipient the recipient to send.
   */
  private static void sendMaximumConnection(@NotNull final ChannelHandlerContext ctx,
                                            @NotNull final ServerSocket server,
                                            @NotNull final InetSocketAddress recipient) {
    Packets.createPacket(ctx, 25, 25, packet -> {
      packet.writeByte(Packets.MAXIMUM_CONNECTION);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(server.getUniqueId());
      Socket.send(ctx, packet, recipient);
    });
  }

  /**
   * sends unconnected pong packet to the given recipient.
   *
   * @param ctx the context to send.
   * @param server the server to send unique id.
   * @param recipient the recipient to send.
   * @param pingTime the ping time to send.
   */
  private static void sendUnconnectedPongPacket(@NotNull final ChannelHandlerContext ctx,
                                                @NotNull final ServerSocket server,
                                                @NotNull final InetSocketAddress recipient,
                                                final long pingTime) {
    server.getServerListener().onRequestServerData(server, recipient).thenAccept(serverData -> {
      final var serverDataLength = serverData.length;
      final var packetLength = 35 + serverDataLength;
      Packets.createPacket(ctx, packetLength, packet -> {
        packet.writeByte(Packets.UNCONNECTED_PONG);
        packet.writeLong(pingTime);
        packet.writeLong(server.getUniqueId());
        Packets.writeUnconnectedMagic(packet);
        packet.writeShort(serverDataLength);
        packet.writeBytes(serverData);
        Socket.sendWithPromise(ctx, packet, recipient);
      });
    });
  }

  /**
   * writes the given address into the given buffer.
   *
   * @param buffer the buffer to write.
   * @param address the address to write.
   */
  private static void writeAddress(@NotNull final ByteBuf buffer, @NotNull final InetSocketAddress address) {
    final var addressBytes = address.getAddress().getAddress();
    if (address.getAddress() instanceof Inet4Address) {
      buffer.writeByte(4);
      Misc.flip(addressBytes);
      buffer.writeBytes(addressBytes);
      buffer.writeShort(address.getPort());
    } else if (address.getAddress() instanceof Inet6Address) {
      buffer.writeByte(6);
      buffer.writeShortLE(Packets.AF_INET6);
      buffer.writeShort(address.getPort());
      buffer.writeInt(0);
      buffer.writeBytes(addressBytes);
      buffer.writeInt(((Inet6Address) address.getAddress()).getScopeId());
    } else {
      throw new UnsupportedOperationException("Unknown InetAddress instance");
    }
  }

  /**
   * writes {@link Constants#UNCONNECTED_MAGIC} into the given array.
   *
   * @param buffer the buffer to write.
   */
  private static void writeUnconnectedMagic(@NotNull final ByteBuf buffer) {
    buffer.writeBytes(Constants.UNCONNECTED_MAGIC);
  }
}
