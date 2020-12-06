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

package io.github.shiruka.shiruka.network;

import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.github.shiruka.shiruka.network.misc.IntRange;
import io.github.shiruka.shiruka.network.misc.NetDatagramPacket;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.network.util.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that provides you to manage the connection.
 */
public abstract class NetConnection<S extends Socket> implements Connection<S> {

  /**
   * connection's address.
   */
  @NotNull
  private final InetSocketAddress address;

  /**
   * cache values.
   */
  @NotNull
  private final ConnectionCache cache;

  /**
   * the channel.
   */
  @NotNull
  private final Channel channel;

  /**
   * close status for connection.
   */
  private final AtomicInteger closed = new AtomicInteger(0);

  /**
   * the connection handler instance.
   */
  @NotNull
  private final ConnectionHandler connectionHandler;

  /**
   * connection's channel handler context.
   */
  @NotNull
  private final ChannelHandlerContext ctx;

  /**
   * the current ping time.
   */
  private final AtomicLong currentPingTime = new AtomicLong(-1);

  /**
   * the datagram read index.
   */
  private final AtomicInteger datagramReadIndex = new AtomicInteger();

  /**
   * the datagram write index.
   */
  private final AtomicInteger datagramWriteIndex = new AtomicInteger();

  /**
   * the event loop.
   */
  @NotNull
  private final EventLoop eventLoop;

  /**
   * last minimum weight.
   */
  private final AtomicLong lastMinWeight = new AtomicLong();

  /**
   * the latest ping time.
   */
  private final AtomicLong lastPingTime = new AtomicLong(-1);

  /**
   * the latest pong time.
   */
  private final AtomicLong lastPongTime = new AtomicLong(-1);

  /**
   * last received packet.
   */
  private final AtomicLong lastTouched = new AtomicLong(System.currentTimeMillis());

  /**
   * connection's protocol version.
   */
  private final short protocolVersion;

  /**
   * the reliability read index.
   */
  private final AtomicInteger reliabilityReadIndex = new AtomicInteger();

  /**
   * the reliability write index.
   */
  private final AtomicInteger reliabilityWriteIndex = new AtomicInteger();

  /**
   * the socket instance.
   */
  @NotNull
  private final S socket;

  /**
   * the split index number.
   */
  private final AtomicInteger splitIndex = new AtomicInteger();

  /**
   * the state of the connection.
   */
  private final AtomicReference<ConnectionState> state = new AtomicReference<>(ConnectionState.UNCONNECTED);

  /**
   * un-ACK bytes.
   */
  private final AtomicInteger unACKedBytes = new AtomicInteger();

  /**
   * connection's adjusted mtu size.
   */
  private int adjustedMtu;

  /**
   * the connection listener instance.
   */
  @Nullable
  private ConnectionListener connectionListener;

  /**
   * the connection's timeout.
   */
  private long connectionTimeout = Constants.CONNECTION_TIMEOUT_MS;

  /**
   * connection's mtu size.
   */
  private int mtu;

  /**
   * connection's unique id a.k.a. guid.
   */
  private long uniqueId;

  /**
   * ctor.
   *
   * @param socket the socket.
   * @param handler the handler.
   * @param address the address.
   * @param ctx the context.
   * @param mtu the mtu size.
   * @param protocolVersion the protocol version.
   */
  protected NetConnection(@NotNull final S socket, @NotNull final Function<Connection<S>, ConnectionHandler> handler,
                          @NotNull final InetSocketAddress address, @NotNull final ChannelHandlerContext ctx,
                          final int mtu, final short protocolVersion) {
    this.socket = socket;
    this.connectionHandler = handler.apply(this);
    this.address = address;
    this.ctx = ctx;
    this.setMtu(mtu);
    this.protocolVersion = protocolVersion;
    this.channel = ctx.channel();
    this.eventLoop = this.channel.eventLoop();
    this.cache = new ConnectionCache(this);
  }

  @Override
  public final void checkForClosed() {
    if (this.isClosed()) {
      throw new IllegalStateException("The connection already closed!");
    }
  }

  @Override
  public final void close(@NotNull final DisconnectReason reason) {
    if (!this.closed.compareAndSet(0, 1)) {
      return;
    }
    this.eventLoop.execute(() -> {
      this.setState(ConnectionState.UNCONNECTED);
      this.connectionHandler.onClose();
      Loggers.debug("Connection (%s => %s) closed: %s", this.socket.getAddress(), this.address, reason);
      this.reset();
      this.getConnectionListener().ifPresent(listener ->
        listener.onDisconnect(reason));
    });
  }

  @Override
  public final void disconnect(@NotNull final DisconnectReason reason) {
    if (this.isClosed()) {
      return;
    }
    Packets.sendDisconnectionNotification(this);
    this.close(reason);
  }

  @NotNull
  @Override
  public final InetSocketAddress getAddress() {
    return this.address;
  }

  @Override
  public final int getAdjustedMtu() {
    return this.adjustedMtu;
  }

  @NotNull
  @Override
  public final ConnectionCache getCache() {
    return this.cache;
  }

  @NotNull
  @Override
  public final Channel getChannel() {
    return this.channel;
  }

  @NotNull
  @Override
  public final ConnectionHandler getConnectionHandler() {
    return this.connectionHandler;
  }

  @NotNull
  @Override
  public final Optional<ConnectionListener> getConnectionListener() {
    return Optional.ofNullable(this.connectionListener);
  }

  @Override
  public final void setConnectionListener(@NotNull final ConnectionListener connectionListener) {
    this.connectionListener = connectionListener;
  }

  @Override
  public final long getConnectionTimeout() {
    return this.connectionTimeout;
  }

  @Override
  public final void setConnectionTimeout(final long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  @NotNull
  @Override
  public final ChannelHandlerContext getContext() {
    return this.ctx;
  }

  @NotNull
  @Override
  public final AtomicLong getCurrentPingTime() {
    return this.currentPingTime;
  }

  @NotNull
  @Override
  public final AtomicInteger getDatagramReadIndex() {
    return this.datagramReadIndex;
  }

  @NotNull
  @Override
  public final EventLoop getEventLoop() {
    return this.eventLoop;
  }

  @NotNull
  @Override
  public final AtomicLong getLastPingTime() {
    return this.lastPingTime;
  }

  @NotNull
  @Override
  public final AtomicLong getLastPongTime() {
    return this.lastPongTime;
  }

  @Override
  public final int getMtu() {
    return this.mtu;
  }

  @Override
  public final void setMtu(final int mtu) {
    if (mtu < Constants.MINIMUM_MTU_SIZE) {
      this.mtu = Constants.MINIMUM_MTU_SIZE;
    } else {
      this.mtu = Math.max(mtu, Constants.MAXIMUM_MTU_SIZE);
    }
    this.adjustedMtu = this.mtu - Constants.UDP_HEADER_SIZE - Misc.getIpHeader(this.address);
  }

  @Override
  public final long getPing() {
    return this.lastPongTime.get() - this.lastPingTime.get();
  }

  @Override
  public final short getProtocolVersion() {
    return this.protocolVersion;
  }

  @NotNull
  @Override
  public final AtomicInteger getReliabilityReadIndex() {
    return this.reliabilityReadIndex;
  }

  @NotNull
  @Override
  public final S getSocket() {
    return this.socket;
  }

  @NotNull
  @Override
  public final ConnectionState getState() {
    return this.state.get();
  }

  @Override
  public final void setState(@NotNull final ConnectionState state) {
    final var old = this.getState();
    if (this.getState() != state) {
      this.state.set(state);
      this.getConnectionListener().ifPresent(listener ->
        listener.onStateChanged(old, state));
    }
  }

  @Override
  public final long getUniqueId() {
    return this.uniqueId;
  }

  @Override
  public final void setUniqueId(final long uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public final void initialize() {
    this.cache.initialize();
  }

  @Override
  public final boolean isClosed() {
    return this.closed.get() == 1;
  }

  @Override
  public final void onTick(final long now) {
    if (this.isClosed()) {
      return;
    }
    this.tick(now);
  }

  @Override
  public void onWrappedPacket(@NotNull final ByteBuf packet) {
    // TODO Continue to development here.
  }

  @Override
  public final void reset() {
    this.cache.reset();
  }

  @Override
  public final void sendDecent(@NotNull final ByteBuf packet, @NotNull final PacketPriority priority,
                               @NotNull final PacketReliability reliability, final int orderingChannel) {
    if (this.isClosed() || this.getState().ordinal() < ConnectionState.INITIALIZED.ordinal()) {
      return;
    }
    final var packets = this.createEncapsulated(packet, priority, reliability, orderingChannel);
    if (priority == PacketPriority.IMMEDIATE) {
      this.eventLoop.execute(() -> this.sendImmediate(packets));
      return;
    }
    try {
      this.getCache().lockOutgoingLock();
      try {
        final long weight = this.getNextWeight(priority);
        if (packets.length == 1) {
          this.getCache().insertOutgoingPackets(weight, packets[0]);
        } else {
          this.getCache().insertSeriesOutgoingPackets(weight, packets);
        }
      } finally {
        this.getCache().unlockOutgoingLock();
      }
    } finally {
      packet.release();
    }
  }

  @Override
  public final void touch() {
    this.checkForClosed();
    this.updateLastTouch();
  }

  @Override
  public final void close() {
    this.close(DisconnectReason.DISCONNECTED);
  }

  /**
   * creates and returns an encapsulated packet from the given packet.
   *
   * @param packet the packet to create.
   * @param priority the priority to create.
   * @param reliability the reliability to create.
   * @param orderingChannel the ordering channel to create.
   *
   * @return an encapsulated packet as {@link EncapsulatedPacket}
   */
  @NotNull
  private EncapsulatedPacket[] createEncapsulated(@NotNull final ByteBuf packet, @NotNull final PacketPriority priority,
                                                  @NotNull PacketReliability reliability, final int orderingChannel) {
    final var maxLength = this.adjustedMtu -
      Constants.MAXIMUM_ENCAPSULATED_HEADER_SIZE - Constants.DATAGRAM_HEADER_SIZE;
    final ByteBuf[] buffers;
    int splitId = 0;
    if (packet.readableBytes() > maxLength) {
      switch (reliability) {
        case UNRELIABLE:
          reliability = PacketReliability.RELIABLE;
          break;
        case UNRELIABLE_SEQUENCED:
          reliability = PacketReliability.RELIABLE_SEQUENCED;
          break;
        case UNRELIABLE_WITH_ACK_RECEIPT:
          reliability = PacketReliability.RELIABLE_WITH_ACK_RECEIPT;
          break;
        case RELIABLE:
        case RELIABLE_ORDERED:
        case RELIABLE_SEQUENCED:
        case RELIABLE_WITH_ACK_RECEIPT:
        case RELIABLE_ORDERED_WITH_ACK_RECEIPT:
        default:
      }
      final var split = (packet.readableBytes() - 1) / maxLength + 1;
      packet.retain(split);
      buffers = new ByteBuf[split];
      IntStream.range(0, split)
        .forEach(i -> buffers[i] = packet.readSlice(Math.min(maxLength, packet.readableBytes())));
      if (packet.isReadable()) {
        throw new IllegalStateException("Buffer still has bytes to read!");
      }
      splitId = this.splitIndex.getAndIncrement();
    } else {
      buffers = new ByteBuf[]{packet.readRetainedSlice(packet.readableBytes())};
    }
    int orderingIndex = 0;
    if (reliability.isOrdered()) {
      orderingIndex = this.getCache().getOrderWriteIndex().getAndIncrement(orderingChannel);
    }
    final var packets = new EncapsulatedPacket[buffers.length];
    for (int i = 0, parts = buffers.length; i < parts; i++) {
      final var encapsulatedPacket = new EncapsulatedPacket();
      encapsulatedPacket.setBuffer(buffers[i]);
      encapsulatedPacket.orderingChannel = (short) orderingChannel;
      encapsulatedPacket.orderingIndex = orderingIndex;
      encapsulatedPacket.setReliability(reliability);
      encapsulatedPacket.setPriority(priority);
      if (reliability.isReliable()) {
        encapsulatedPacket.reliabilityIndex = this.reliabilityWriteIndex.getAndIncrement();
      }
      if (parts > 1) {
        encapsulatedPacket.split = true;
        encapsulatedPacket.partIndex = i;
        encapsulatedPacket.partCount = parts;
        encapsulatedPacket.partId = splitId;
      }
      packets[i] = encapsulatedPacket;
    }
    return packets;
  }

  /**
   * gets the next weight.
   *
   * @param priority the priority to get.
   *
   * @return the next weight.
   */
  private long getNextWeight(@NotNull final PacketPriority priority) {
    final var ordinal = priority.ordinal();
    var next = this.getCache().getOutgoingPacketNextWeight(ordinal);
    if (!this.getCache().getOutgoingPackets().isEmpty()) {
      if (next >= this.lastMinWeight.longValue()) {
        next = this.lastMinWeight.longValue() + (1L << ordinal) * ordinal + ordinal;
        this.getCache().setOutgoingPacketNextWeights(ordinal, next + (1L << ordinal) * (ordinal + 1) + ordinal);
      }
    } else {
      this.getCache().initHeapWeights();
    }
    this.lastMinWeight.set(next - (1L << ordinal) * ordinal + ordinal);
    return next;
  }

  /**
   * checks and returns if the connection is timed out.
   *
   * @param now the now to check
   *
   * @return true if the connection is timed out.
   */
  private boolean isTimedOut(final long now) {
    return now - this.lastTouched.get() >= this.connectionTimeout;
  }

  /**
   * sends the net datagram packet to the connection.
   *
   * @param packet the packet to send.
   * @param time the packet to send.
   */
  private void sendDatagram(@NotNull final NetDatagramPacket packet, final long time) {
    if (packet.getPackets().isEmpty()) {
      throw new IllegalStateException("NetDatagramPacket has not any packet!");
    }
    try {
      final var oldIndex = packet.getSequenceIndex();
      packet.setSequenceIndex(this.datagramWriteIndex.getAndIncrement());
      for (final var encapsulatedPacket : packet.getPackets()) {
        if (encapsulatedPacket.getReliability() != PacketReliability.UNRELIABLE &&
          encapsulatedPacket.getReliability() != PacketReliability.UNRELIABLE_SEQUENCED) {
          packet.setNextSend(time + this.getCache().getSlidingWindow().getRtoForRetransmission());
          if (oldIndex == -1) {
            this.unACKedBytes.addAndGet(packet.getSize());
          } else {
            this.getCache().removeSentDatagrams(oldIndex, packet);
          }
          this.getCache().putSentDatagrams(packet.getSequenceIndex(), packet.retain());
          break;
        }
      }
      final var buf = this.allocateBuffer(packet.getSize());
      if (buf.writerIndex() >= this.adjustedMtu) {
        throw new IllegalStateException("Packet length was " + buf.writerIndex() + " but expected " + this.adjustedMtu);
      }
      packet.encode(buf);
      this.channel.write(new DatagramPacket(buf, this.address));
    } finally {
      packet.release();
    }
  }

  /**
   * sends the given packets immediately.
   *
   * @param packets the packets to send.
   */
  private void sendImmediate(@NotNull final EncapsulatedPacket[] packets) {
    final var now = System.currentTimeMillis();
    for (final var packet : packets) {
      final var datagram = new NetDatagramPacket(now);
      if (!datagram.tryAddPacket(packet, this.adjustedMtu)) {
        throw new IllegalArgumentException("Packet too large to fit in MTU (size: " + packet.getSize() +
          ", MTU: " + this.adjustedMtu + ")");
      }
      this.sendDatagram(datagram, now);
    }
    this.channel.flush();
  }

  /**
   * runs every tick.
   *
   * @param now the time of now to tick.
   */
  private void tick(final long now) {
    if (this.isTimedOut(now)) {
      this.close(DisconnectReason.TIMED_OUT);
      return;
    }
    if (this.getState().ordinal() < ConnectionState.INITIALIZED.ordinal()) {
      return;
    }
    if (this.currentPingTime.get() + 2000L < now) {
      Packets.sendConnectedPing(this, now);
    }
    final var temp = this.getCache();
    final var slidingWindow = temp.getSlidingWindow();
    final var incomingACKs = temp.getIncomingACKs();
    if (!incomingACKs.isEmpty()) {
      IntRange range;
      while ((range = incomingACKs.poll()) != null) {
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
          final var datagram = temp.removeSentDatagrams(i);
          if (datagram == null) {
            continue;
          }
          datagram.release();
          this.unACKedBytes.addAndGet(-datagram.getSize());
          slidingWindow.onACK(now - datagram.getTime(), datagram.getSequenceIndex(), this.datagramReadIndex.get());
        }
      }
    }
    final var incomingNACKs = temp.getIncomingNACKs();
    if (!incomingNACKs.isEmpty()) {
      slidingWindow.onNACK();
      IntRange range;
      while ((range = incomingNACKs.poll()) != null) {
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
          final var datagram = temp.removeSentDatagrams(i);
          if (datagram == null) {
            continue;
          }
          Loggers.error("NACKed datagram %s from %s", datagram.getSequenceIndex(), this.address);
          this.sendDatagram(datagram, now);
        }
      }
    }
    final var mtuSize = this.adjustedMtu - Constants.DATAGRAM_HEADER_SIZE;
    final var outgoingNACKs = temp.getOutgoingNACKs();
    while (!outgoingNACKs.isEmpty()) {
      final var buffer = this.allocateBuffer(mtuSize);
      buffer.writeByte(Constants.FLAG_VALID | Constants.FLAG_NACK);
      Misc.writeIntRanges(buffer, outgoingNACKs, mtuSize - 1);
      this.sendDirect(buffer);
    }
    if (slidingWindow.shouldSendACKs(now)) {
      final var outgoingACKs = temp.getOutgoingACKs();
      while (!outgoingACKs.isEmpty()) {
        final var buffer = this.allocateBuffer(mtuSize);
        buffer.writeByte(Constants.FLAG_VALID | Constants.FLAG_ACK);
        Misc.writeIntRanges(buffer, outgoingACKs, mtuSize - 1);
        this.sendDirect(buffer);
        slidingWindow.onSendACK();
      }
    }
    int transmissionBandwidth;
    final var sentDatagrams = temp.getSentDatagrams();
    if (!sentDatagrams.isEmpty()) {
      transmissionBandwidth = this.unACKedBytes.get();
      boolean hasResent = false;
      for (final var datagram : sentDatagrams.values()) {
        if (datagram.getNextSend() > now) {
          continue;
        }
        final int size = datagram.getSize();
        if (transmissionBandwidth < size) {
          break;
        }
        transmissionBandwidth -= size;
        if (!hasResent) {
          hasResent = true;
        }
        Loggers.error("Stale datagram %s from %s", datagram.getSequenceIndex(), this.address);
        this.sendDatagram(datagram, now);
      }
      if (hasResent) {
        slidingWindow.onResend(now);
      }
    }
    temp.lockOutgoingLock();
    try {
      final var outgoingPackets = temp.getOutgoingPackets();
      if (!outgoingPackets.isEmpty()) {
        transmissionBandwidth = slidingWindow.getTransmissionBandwidth(this.unACKedBytes.get());
        var datagram = new NetDatagramPacket(now);
        EncapsulatedPacket packet;
        while ((packet = outgoingPackets.peek()) != null) {
          final int size = packet.getSize();
          if (transmissionBandwidth < size) {
            break;
          }
          transmissionBandwidth -= size;
          outgoingPackets.remove();
          if (datagram.tryAddPacket(packet, this.adjustedMtu)) {
            continue;
          }
          this.sendDatagram(datagram, now);
          datagram = new NetDatagramPacket(now);
          if (!datagram.tryAddPacket(packet, this.adjustedMtu)) {
            throw new IllegalArgumentException("Packet too large to fit in MTU (size: " + packet.getSize() + ", MTU: " + this.adjustedMtu + ")");
          }
        }
        if (!datagram.getPackets().isEmpty()) {
          this.sendDatagram(datagram, now);
        }
      }
    } finally {
      temp.unlockOutgoingLock();
    }
    this.channel.flush();
  }

  /**
   * updates last touch to the current time.
   */
  private void updateLastTouch() {
    this.lastTouched.set(System.currentTimeMillis());
  }
}
