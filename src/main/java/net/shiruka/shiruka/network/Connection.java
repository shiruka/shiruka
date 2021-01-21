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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.shiruka.shiruka.network.packet.PacketOut;
import org.jetbrains.annotations.NotNull;

/**
 * a class that provides the connections between server and client.
 */
public interface Connection<S extends Socket> extends AutoCloseable {

  /**
   * adds the given packet to the queued packet list.
   *
   * @param packet the packet to add.
   */
  void addQueuedPacket(@NotNull PacketOut packet);

  /**
   * obtains a byte buffer instance from the given capacity.
   *
   * @param capacity the capacity to create.
   *
   * @return a byte buffer instance.
   *
   * @see ByteBuf
   */
  @NotNull
  default ByteBuf allocateBuffer(final int capacity) {
    return this.getChannel().alloc().ioBuffer(capacity);
  }

  /**
   * checks if the connection is closed.
   *
   * @throws IllegalStateException if the connection is closed.
   */
  void checkForClosed();

  /**
   * closes the connection with the given reason.
   *
   * @param reason the reason to close.
   */
  void close(@NotNull DisconnectReason reason);

  /**
   * disconnects the connection.
   *
   * @param reason the reason to disconnect.
   */
  void disconnect(@NotNull DisconnectReason reason);

  /**
   * obtains connection's address.
   *
   * @return connection's address.
   */
  @NotNull
  InetSocketAddress getAddress();

  /**
   * obtains connection's adjusted mtu size.
   *
   * @return connection's adjusted mtu size.
   */
  int getAdjustedMtu();

  /**
   * obtains the connection's cache instance.
   *
   * @return the connection cache {@link ConnectionCache}
   */
  @NotNull
  ConnectionCache getCache();

  /**
   * obtains connection's channel.
   *
   * @return connection's channel.
   */
  @NotNull
  Channel getChannel();

  /**
   * obtains connection's handler.
   *
   * @return the listener to handle connection's events.
   */
  @NotNull
  ConnectionHandler getConnectionHandler();

  /**
   * obtains the connection's listener.
   *
   * @return the connection's listener.
   */
  @NotNull
  Optional<ConnectionListener> getConnectionListener();

  /**
   * sets the connection's listener.
   *
   * @param connectionListener the connection listener to set.
   */
  void setConnectionListener(@NotNull ConnectionListener connectionListener);

  /**
   * obtains the connection timeout.
   *
   * @return the connection timeout.
   */
  long getConnectionTimeout();

  /**
   * sets the connection connection timeout to the given connection timeout.
   *
   * @param connectionTimeout the connectionTimeout to set.
   */
  void setConnectionTimeout(long connectionTimeout);

  /**
   * obtains connection's channel context.
   *
   * @return connection's channel context.
   */
  @NotNull
  ChannelHandlerContext getContext();

  /**
   * obtains the current ping time of the connection.
   *
   * @return the current ping time.
   */
  @NotNull
  AtomicLong getCurrentPingTime();

  /**
   * obtains the datagram read index.
   *
   * @return the datagram read index.
   */
  @NotNull
  AtomicInteger getDatagramReadIndex();

  /**
   * obtains the event loop.
   *
   * @return the event loop.
   */
  @NotNull
  EventLoop getEventLoop();

  /**
   * obtains the latest ping time of the connection.
   *
   * @return the latest ping time.
   */
  @NotNull
  AtomicLong getLastPingTime();

  /**
   * obtains the latest pong time of the connection.
   *
   * @return the latest pong time.
   */
  @NotNull
  AtomicLong getLastPongTime();

  /**
   * obtains connection's mtu size.
   *
   * @return connection's mtu size.
   */
  int getMtu();

  /**
   * set connection's mtu size.
   *
   * @param mtu the mtu to set.
   */
  void setMtu(int mtu);

  /**
   * calculates the connection's ping.
   *
   * @return the connection ping.
   */
  long getPing();

  /**
   * obtains connection's protocol version.
   *
   * @return connection's protocol version.
   */
  short getProtocolVersion();

  /**
   * obtains the reliability read index.
   *
   * @return the reliability read index.
   */
  @NotNull
  AtomicInteger getReliabilityReadIndex();

  /**
   * obtains connection's socket.
   *
   * @return the socket.
   */
  @NotNull
  S getSocket();

  /**
   * obtains connection's state.
   *
   * @return connection's state.
   */
  @NotNull
  ConnectionState getState();

  /**
   * sets the connection's state to the given state.
   *
   * @param state the state to set.
   */
  void setState(@NotNull ConnectionState state);

  /**
   * connection's the unique id.
   *
   * @return the unique id to determine the connection.
   */
  long getUniqueId();

  /**
   * set connection's the unique id.
   *
   * @param uniqueId the unique id to set.
   */
  void setUniqueId(long uniqueId);

  /**
   * initializes the connection's caches.
   */
  void initialize();

  /**
   * checks and returns {@code true} if the connection is closed.
   *
   * @return true if the connection is closed.
   */
  boolean isClosed();

  /**
   * runs every tick.
   *
   * @param now the current time to handle tick.
   */
  void onTick(long now);

  /**
   * resets the connection's caches.
   */
  void reset();

  /**
   * send the packet with queue.
   *
   * @param packet the packet to send.
   */
  default void sendDecent(@NotNull final ByteBuf packet) {
    this.sendDecent(packet, PacketPriority.MEDIUM);
  }

  /**
   * send the packet with queue.
   *
   * @param packet the packet to send.
   * @param priority the priority to send.
   * @param reliability the reliability to send.
   * @param orderingChannel the ordering channel to send.
   */
  void sendDecent(@NotNull ByteBuf packet, @NotNull PacketPriority priority, @NotNull PacketReliability reliability,
                  int orderingChannel);

  /**
   * send the packet with queue.
   *
   * @param packet the packet to send.
   * @param priority the priority to send.
   * @param reliability the reliability to send.
   */
  default void sendDecent(@NotNull final ByteBuf packet, @NotNull final PacketPriority priority,
                          @NotNull final PacketReliability reliability) {
    this.sendDecent(packet, priority, reliability, 0);
  }

  /**
   * send the packet with queue.
   *
   * @param packet the packet to send.
   * @param priority the priority to send.
   */
  default void sendDecent(@NotNull final ByteBuf packet, @NotNull final PacketPriority priority) {
    this.sendDecent(packet, priority, PacketReliability.RELIABLE_ORDERED);
  }

  /**
   * send the packet with queue.
   *
   * @param packet the packet to send.
   * @param reliability the reliability to send.
   */
  default void sendDecent(@NotNull final ByteBuf packet, @NotNull final PacketReliability reliability) {
    this.sendDecent(packet, PacketPriority.MEDIUM, reliability);
  }

  /**
   * sends the packet to the connection's address.
   *
   * @param packet the packet to send
   */
  default void sendDirect(@NotNull final ByteBuf packet) {
    this.getChannel().writeAndFlush(new DatagramPacket(packet, this.getAddress()));
  }

  /**
   * touches when a packet receive.
   */
  void touch();
}
