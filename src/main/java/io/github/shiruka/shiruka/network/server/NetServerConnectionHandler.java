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

package io.github.shiruka.shiruka.network.server;

import io.github.shiruka.api.misc.Optionals;
import io.github.shiruka.shiruka.misc.Loggers;
import io.github.shiruka.shiruka.network.*;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.github.shiruka.shiruka.network.misc.IntRange;
import io.github.shiruka.shiruka.network.misc.NetDatagramPacket;
import io.github.shiruka.shiruka.network.misc.SplitPacketHelper;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.network.util.Packets;
import io.netty.buffer.ByteBuf;
import java.net.Inet6Address;
import java.util.Arrays;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * server connection handler implementation class.
 */
public final class NetServerConnectionHandler implements ServerConnectionHandler {

  /**
   * the server connection instance.
   */
  @NotNull
  private final Connection<ServerSocket, ServerConnectionHandler> connection;

  /**
   * ctor.
   *
   * @param connection the server connection.
   */
  NetServerConnectionHandler(@NotNull final Connection<ServerSocket, ServerConnectionHandler> connection) {
    this.connection = connection;
  }

  @Override
  public void sendConnectionReply1() {
    this.createPacket(28, packet -> {
      packet.writeByte(Packets.OPEN_CONNECTION_REPLY_1);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(this.connection.getSocket().getUniqueId());
      packet.writeBoolean(false);
      packet.writeShort(this.connection.getMtu());
      this.connection.sendDirect(packet);
    });
  }

  @Override
  public void onClose() {
    this.connection.getSocket().removeConnection(this.connection.getAddress(), this.connection);
  }

  @Override
  public void sendDisconnectionNotification() {
    this.createPacket(1, packet -> {
      packet.writeByte(Packets.DISCONNECTION_NOTIFICATION);
      this.connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE_ORDERED);
    });
  }

  @Override
  public void sendConnectedPing(final long pingTime) {
    this.createPacket(9, packet -> {
      packet.writeByte(Packets.CONNECTED_PING);
      packet.writeLong(pingTime);
      this.connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
      this.connection.getCurrentPingTime().set(pingTime);
    });
  }

  @Override
  public void onRawDatagram(@NotNull final ByteBuf packet) {
    if (this.connection.isClosed()) {
      return;
    }
    this.connection.touch();
    final var flags = packet.readByte();
    final var isRakNetPacket = (flags & Constants.FLAG_VALID) != 0;
    if (isRakNetPacket) {
      if (this.connection.getState().ordinal() >= ConnectionState.INITIALIZED.ordinal()) {
        if ((flags & Constants.FLAG_ACK) != 0) {
          this.onACKnowledge(packet, intRange -> this.connection.getCache().offerIncomingACKs(intRange));
        } else if ((flags & Constants.FLAG_NACK) != 0) {
          this.onACKnowledge(packet, intRange -> this.connection.getCache().offerIncomingNACKs(intRange));
        } else {
          packet.readerIndex(0);
          this.onDatagram(packet);
        }
      }
      return;
    }
    packet.readerIndex(0);
    final var packetId = packet.getUnsignedByte(packet.readerIndex());
    packet.readerIndex(0);
    if (packetId >= Packets.USER_PACKET_ENUM) {
      this.connection.getSocket().getSocketListener().onDirect(packet);
      return;
    }
    this.onPacket(packet);
  }

  /**
   * handles the simple connection request packets.
   *
   * @param packet the packet to handle.
   */
  private void onPacket(@NotNull final ByteBuf packet) {
    Optionals.useAndGet(packet.readUnsignedByte(), packetId -> {
      if (packetId == Packets.OPEN_CONNECTION_REQUEST_2) {
        this.onOpenConnectionRequest2(packet);
      } else if (packetId == Packets.CONNECTION_REQUEST) {
        this.onConnectionRequest(packet);
      } else if (packetId == Packets.NEW_INCOMING_CONNECTION) {
        this.onNewIncomingConnection();
      }
    });
  }

  /**
   * handles incoming ACK and NACK packets.
   *
   * @param packet the packet to handle.
   * @param consumer the offer to offer.
   */
  private void onACKnowledge(@NotNull final ByteBuf packet, @NotNull final Consumer<IntRange> consumer) {
    this.connection.checkForClosed();
    final var size = packet.readUnsignedShort();
    for (int i = 0; i < size; i++) {
      final var singleton = packet.readBoolean();
      final var start = packet.readUnsignedMediumLE();
      final var end = singleton ? start : packet.readMediumLE();
      if (start > end) {
        Loggers.useLogger(
          logger -> logger.error("{} sent an IntRange with a start value {} greater than an end value of {}",
            this.connection.getAddress(), start, end));
        this.connection.disconnect(DisconnectReason.BAD_PACKET);
        return;
      }
      consumer.accept(new IntRange(start, end));
    }
  }

  /**
   * handles datagram packets.
   *
   * @param packet the packet to handle.
   */
  private void onDatagram(@NotNull final ByteBuf packet) {
    if (ConnectionState.INITIALIZED.compareTo(this.connection.getState()) > 0) {
      return;
    }
    final var datagram = new NetDatagramPacket(System.currentTimeMillis());
    datagram.decode(packet);
    final var cache = this.connection.getCache();
    cache.getSlidingWindow().onPacketReceived(datagram.getTime());
    final var prevSequenceIndex = this.connection.getDatagramReadIndex().getAndAccumulate(datagram.getSequenceIndex(),
      (prev, newIndex) -> prev <= newIndex ? newIndex + 1 : prev);
    final var missedDatagrams = datagram.getSequenceIndex() - prevSequenceIndex;
    if (missedDatagrams > 0) {
      cache.getOutgoingNACKs().offer(new IntRange(datagram.getSequenceIndex() - missedDatagrams, datagram.getSequenceIndex()));
    }
    cache.getOutgoingACKs().offer(new IntRange(datagram.getSequenceIndex()));
    for (final var encapsulated : datagram.getPackets()) {
      if (encapsulated.getReliability().isReliable()) {
        cache.lockReliabilityReadLock();
        try {
          final var missed = encapsulated.reliabilityIndex - this.connection.getReliabilityReadIndex().get();
          final var reliableDatagramQueue = cache.getReliableDatagramQueue();
          if (missed > 0) {
            if (missed < reliableDatagramQueue.size()) {
              if (reliableDatagramQueue.get(missed)) {
                reliableDatagramQueue.set(missed, false);
              } else {
                continue;
              }
            } else {
              final var count = missed - reliableDatagramQueue.size();
              for (var i = 0; i < count; i++) {
                reliableDatagramQueue.add(true);
              }
              reliableDatagramQueue.add(false);
            }
          } else if (missed == 0) {
            this.connection.getReliabilityReadIndex().incrementAndGet();
            if (!reliableDatagramQueue.isEmpty()) {
              reliableDatagramQueue.poll();
            }
          } else {
            continue;
          }
          while (!reliableDatagramQueue.isEmpty() && !reliableDatagramQueue.peek()) {
            reliableDatagramQueue.poll();
            this.connection.getReliabilityReadIndex().incrementAndGet();
          }
        } finally {
          cache.unlockReliabilityReadLock();
        }
      }
      if (!encapsulated.split) {
        this.checkForOrdered(encapsulated);
        continue;
      }
      final var reassembled = this.getReassembledPacket(encapsulated);
      if (reassembled == null) {
        continue;
      }
      try {
        this.checkForOrdered(reassembled);
      } finally {
        reassembled.release();
      }
    }
  }

  /**
   * runs when a connection want to open second connection request.
   *
   * @param packet the packet receive.
   */
  private void onOpenConnectionRequest2(@NotNull final ByteBuf packet) {
    if (this.connection.getState() != ConnectionState.INITIALIZING) {
      return;
    }
    if (!Packets.verifyUnconnectedMagic(packet)) {
      return;
    }
    Packets.readAddress(packet);
    Optionals.useAndGet(packet.readUnsignedShort(), mtu -> {
      this.connection.setMtu(mtu);
      this.connection.setUniqueId(packet.readLong());
      this.connection.initialize();
      this.sendOpenConnectionReply2();
      this.connection.setState(ConnectionState.INITIALIZED);
    });
  }

  /**
   * runs when a connection want to last connection request.
   *
   * @param packet the packet receive.
   */
  private void onConnectionRequest(@NotNull final ByteBuf packet) {
    final var uniqueId = packet.readLong();
    final var time = packet.readLong();
    final var security = packet.readBoolean();
    if (this.connection.getUniqueId() != uniqueId || security) {
      this.sendConnectionRequestFailed();
      this.connection.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
      return;
    }
    this.connection.setState(ConnectionState.CONNECTING);
    this.sendConnectionRequestAccepted(time);
  }

  /**
   * send the connection request accepted packet to the connection.
   *
   * @param time the time to send.
   */
  private void sendConnectionRequestAccepted(final long time) {
    final var address = this.connection.getAddress();
    final var ipv6 = address.getAddress() instanceof Inet6Address;
    this.createPacket(ipv6 ? 628 : 166, packet -> {
      packet.writeByte(Packets.CONNECTION_REQUEST_ACCEPTED);
      Packets.writeAddress(packet, address);
      packet.writeShort(0);
      Arrays.stream(ipv6 ? Misc.LOCAL_IP_ADDRESSES_V6 : Misc.LOCAL_IP_ADDRESSES_V4)
        .forEach(socketAddress -> Packets.writeAddress(packet, socketAddress));
      packet.writeLong(time);
      packet.writeLong(System.currentTimeMillis());
      this.connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
    });
  }

  /**
   * sends the connection request failed packet to the connection.
   */
  private void sendConnectionRequestFailed() {
    this.createPacket(21, packet -> {
      packet.writeByte(Packets.CONNECTION_REQUEST_FAILED);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(this.connection.getSocket().getUniqueId());
      this.connection.sendDirect(packet);
    });
  }

  /**
   * runs when a connection want to second open connection request.
   */
  private void onNewIncomingConnection() {
    if (this.connection.getState() == ConnectionState.CONNECTING) {
      this.connection.setState(ConnectionState.CONNECTED);
    }
  }

  /**
   * sends the open connection reply 2 packet to the connection.
   */
  private void sendOpenConnectionReply2() {
    this.createPacket(31, packet -> {
      packet.writeByte(Packets.OPEN_CONNECTION_REPLY_2);
      Packets.writeUnconnectedMagic(packet);
      packet.writeLong(this.connection.getSocket().getUniqueId());
      Packets.writeAddress(packet, this.connection.getAddress());
      packet.writeShort(this.connection.getMtu());
      packet.writeBoolean(false);
      this.connection.sendDirect(packet);
    });
  }

  /**
   * runs when a disconnection notified.
   */
  private void onDisconnectionNotification() {
    this.connection.close(DisconnectReason.CLOSED_BY_REMOTE_PEER);
  }

  /**
   * handles the packet as a connected pong packet.
   *
   * @param packet the packet to handle.
   */
  private void onConnectedPong(@NotNull final ByteBuf packet) {
    final var pingTime = packet.readLong();
    final var currentPingTime = this.connection.getCurrentPingTime().get();
    if (currentPingTime == pingTime) {
      this.connection.getLastPingTime().set(currentPingTime);
      this.connection.getLastPongTime().set(System.currentTimeMillis());
    }
  }

  /**
   * handles the packet as a connected ping packet.
   *
   * @param packet the packet to handle.
   */
  private void onConnectedPing(@NotNull final ByteBuf packet) {
    final var pingTime = packet.readLong();
    this.sendConnectedPong(pingTime);
  }

  /**
   * send a connected pong packet to the connection.
   *
   * @param pingTime the pint time to send.
   */
  private void sendConnectedPong(final long pingTime) {
    this.createPacket(17, packet -> {
      packet.writeByte(Packets.CONNECTED_PONG);
      packet.writeLong(pingTime);
      packet.writeLong(System.currentTimeMillis());
      this.connection.sendDecent(packet, PacketPriority.IMMEDIATE, PacketReliability.RELIABLE);
    });
  }

  /**
   * creates a packet from the given size and runs the given packet consumer.
   *
   * @param size the size to create.
   * @param packet the packet to run.
   */
  private void createPacket(final int size, @NotNull final Consumer<ByteBuf> packet) {
    Optionals.useAndGet(this.connection.allocateBuffer(size), packet);
  }

  /**
   * checks for ordered the packet
   *
   * @param packet the packet to check.
   */
  private void checkForOrdered(@NotNull final EncapsulatedPacket packet) {
    if (packet.getReliability().isOrdered()) {
      this.onOrderedReceived(packet);
    } else {
      this.onEncapsulatedInternal(packet);
    }
  }

  /**
   * runs when an ordered packet received.
   *
   * @param packet the packet to receive.
   */
  private void onOrderedReceived(@NotNull final EncapsulatedPacket packet) {
    final var cache = this.connection.getCache();
    cache.lockOrderingLock();
    try {
      final var binaryHeap = cache.getOrderingHeap(packet.orderingChannel);
      if (cache.getOrderReadIndex(packet.orderingChannel) < packet.orderingIndex) {
        binaryHeap.insert(packet.orderingIndex, packet.retain());
        return;
      } else if (cache.getOrderReadIndex(packet.orderingChannel) > packet.orderingIndex) {
        return;
      }
      cache.increaseOrderReadIndex(packet.orderingChannel);
      this.onEncapsulatedInternal(packet);
      EncapsulatedPacket queuedPacket;
      while ((queuedPacket = binaryHeap.peek()) != null) {
        if (queuedPacket.orderingIndex == cache.getOrderReadIndex(packet.orderingChannel)) {
          try {
            binaryHeap.remove();
            cache.increaseOrderReadIndex(packet.orderingChannel);
            this.onEncapsulatedInternal(queuedPacket);
          } finally {
            queuedPacket.release();
          }
        } else {
          break;
        }
      }
    } finally {
      cache.unlockOrderingLock();
    }
  }

  /**
   * runs when an internal encapsulated packet comes.
   *
   * @param packet the packet to receive.
   */
  private void onEncapsulatedInternal(@NotNull final EncapsulatedPacket packet) {
    final var buffer = packet.getBuffer();
    Optionals.useAndGet(buffer.readUnsignedByte(), packetId -> {
      if (packetId == Packets.CONNECTED_PING) {
        this.onConnectedPing(buffer);
      } else if (packetId == Packets.CONNECTED_PONG) {
        this.onConnectedPong(buffer);
      } else if (packetId == Packets.DISCONNECTION_NOTIFICATION) {
        this.onDisconnectionNotification();
      } else {
        buffer.readerIndex(0);
        if (packetId >= Packets.USER_PACKET_ENUM) {
          this.connection.getSocket().getSocketListener().onEncapsulated(packet);
        } else {
          this.onPacket(buffer);
        }
      }
    });
  }

  /**
   * obtains reassembled packet.
   *
   * @param splitPacket the split packet to create one.
   *
   * @return a reassembled packet.
   */
  @Nullable
  private EncapsulatedPacket getReassembledPacket(@NotNull final EncapsulatedPacket splitPacket) {
    this.connection.checkForClosed();
    final var cache = this.connection.getCache();
    var helper = cache.getSplitPackets().get(splitPacket.partId);
    if (helper == null) {
      cache.getSplitPackets().set(splitPacket.partId, helper = new SplitPacketHelper(splitPacket.partCount));
    }
    final var result = helper.add(splitPacket, this.connection);
    if (result != null &&
      cache.getSplitPackets().remove(splitPacket.partId, helper)) {
      helper.release();
    }
    return result;
  }
}
