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
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.ConnectionHandler;
import io.github.shiruka.shiruka.network.ConnectionState;
import io.github.shiruka.shiruka.network.DisconnectReason;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.github.shiruka.shiruka.network.misc.IntRange;
import io.github.shiruka.shiruka.network.misc.NetDatagramPacket;
import io.github.shiruka.shiruka.network.misc.SplitPacketHelper;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Packets;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server connection handler implementation class.
 */
public final class NetServerConnectionHandler implements ConnectionHandler {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("NetServerConnectionHandler");

  /**
   * the server connection instance.
   */
  @NotNull
  private final Connection<ServerSocket> connection;

  /**
   * ctor.
   *
   * @param connection the server connection.
   */
  NetServerConnectionHandler(@NotNull final Connection<ServerSocket> connection) {
    this.connection = connection;
  }

  @Override
  public void onClose() {
    this.connection.getSocket().removeConnection(this.connection.getAddress(), this.connection);
  }

  @Override
  public void onRawDatagram(@NotNull final ByteBuf packet) {
    if (this.connection.isClosed()) {
      return;
    }
    this.connection.touch();
    final var flags = packet.readByte();
    final var isRakNetPacket = (flags & Constants.FLAG_VALID) != 0;
    if (!isRakNetPacket) {
      packet.readerIndex(0);
      final var packetId = packet.getUnsignedByte(packet.readerIndex());
      packet.readerIndex(0);
      if (packetId >= Packets.USER_PACKET_ENUM) {
        this.connection.getConnectionListener().ifPresent(listener ->
          listener.onDirect(packet));
        return;
      }
      this.onPacket(packet);
      return;
    }
    if (this.connection.getState().ordinal() < ConnectionState.INITIALIZED.ordinal()) {
      return;
    }
    if ((flags & Constants.FLAG_ACK) != 0) {
      this.onACKnowledge(packet, intRange -> this.connection.getCache().offerIncomingACKs(intRange));
    } else if ((flags & Constants.FLAG_NACK) != 0) {
      this.onACKnowledge(packet, intRange -> this.connection.getCache().offerIncomingNACKs(intRange));
    } else {
      packet.readerIndex(0);
      this.onDatagram(packet);
    }
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

  /**
   * handles incoming ACK and NACK packets.
   *
   * @param packet the packet to handle.
   * @param consumer the offer to offer.
   */
  private void onACKnowledge(@NotNull final ByteBuf packet, @NotNull final Consumer<IntRange> consumer) {
    this.connection.checkForClosed();
    final var size = packet.readUnsignedShort();
    IntStream.range(0, size).forEach(i -> {
      final var singleton = packet.readBoolean();
      final var start = packet.readUnsignedMediumLE();
      final var end = singleton ? start : packet.readMediumLE();
      if (start <= end) {
        consumer.accept(new IntRange(start, end));
        return;
      }
      NetServerConnectionHandler.LOGGER.error("{} sent an IntRange with a start value {} greater than an end value of {}",
        this.connection.getAddress(), start, end);
      this.connection.disconnect(DisconnectReason.BAD_PACKET);
    });
  }

  /**
   * handles the packet as a connected ping packet.
   *
   * @param packet the packet to handle.
   */
  private void onConnectedPing(@NotNull final ByteBuf packet) {
    final var pingTime = packet.readLong();
    Packets.sendConnectedPong(this.connection, pingTime);
  }

  /**
   * handles the packet as a connected pong packet.
   *
   * @param packet the packet to handle.
   */
  private void onConnectedPong(@NotNull final ByteBuf packet) {
    final var pingTime = packet.readLong();
    final var currentPingTime = this.connection.getCurrentPingTime().get();
    if (currentPingTime != pingTime) {
      return;
    }
    this.connection.getLastPingTime().set(currentPingTime);
    this.connection.getLastPongTime().set(System.currentTimeMillis());
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
      Packets.sendConnectionRequestFailed(this.connection);
      this.connection.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
      return;
    }
    this.connection.setState(ConnectionState.CONNECTING);
    Packets.sendConnectionRequestAccepted(this.connection, time);
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
   * runs when a disconnection notified.
   */
  private void onDisconnectionNotification() {
    this.connection.close(DisconnectReason.CLOSED_BY_REMOTE_PEER);
  }

  /**
   * runs when an internal encapsulated packet comes.
   *
   * @param packet the packet to receive.
   */
  private void onEncapsulatedInternal(@NotNull final EncapsulatedPacket packet) {
    final var buffer = packet.getBuffer();
    final var packetId = buffer.readUnsignedByte();
    if (packetId == Packets.CONNECTED_PING) {
      this.onConnectedPing(buffer);
    } else if (packetId == Packets.CONNECTED_PONG) {
      this.onConnectedPong(buffer);
    } else if (packetId == Packets.DISCONNECTION_NOTIFICATION) {
      this.onDisconnectionNotification();
    } else {
      buffer.readerIndex(0);
      if (packetId >= Packets.USER_PACKET_ENUM) {
        this.connection.getConnectionListener().ifPresent(listener ->
          listener.onEncapsulated(packet));
      } else {
        this.onPacket(buffer);
      }
    }
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
      Packets.sendOpenConnectionReply2(this.connection);
      this.connection.setState(ConnectionState.INITIALIZED);
    });
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
   * handles the simple connection request packets.
   *
   * @param packet the packet to handle.
   */
  private void onPacket(@NotNull final ByteBuf packet) {
    final var packetId = packet.readUnsignedByte();
    if (packetId == Packets.OPEN_CONNECTION_REQUEST_2) {
      this.onOpenConnectionRequest2(packet);
    } else if (packetId == Packets.CONNECTION_REQUEST) {
      this.onConnectionRequest(packet);
    } else if (packetId == Packets.NEW_INCOMING_CONNECTION) {
      this.onNewIncomingConnection();
    }
  }
}
