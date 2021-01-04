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

import com.google.common.base.Preconditions;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NetSocket implements Socket {

  /**
   * the datagram channel class.
   */
  @NotNull
  private static final Class<? extends Channel> CHANNEL;

  /**
   * the datagram event loop group.
   */
  @NotNull
  private static final EventLoopGroup GROUP;

  /**
   * socket's address.
   */
  @NotNull
  private final InetSocketAddress address;

  /**
   * the bootstrap to bind the socket.
   */
  private final Bootstrap bootstrap = new Bootstrap();

  /**
   * if the socket is closed or not.
   */
  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * if the socket is running or not.
   */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * server's unique id a.k.a. guid.
   */
  private final long uniqueId = ThreadLocalRandom.current().nextLong();

  /**
   * the tick future to run {@link Socket#tick()} method.
   */
  @Nullable
  private ScheduledFuture<?> tickFuture;

  static {
    final var disableNative = System.getProperties().contains("disableNativeEventLoop");
    if (!disableNative && Epoll.isAvailable()) {
      GROUP = new EpollEventLoopGroup(PoolSpec.UNCAUGHT_FACTORY);
      CHANNEL = EpollDatagramChannel.class;
    } else if (!disableNative && KQueue.isAvailable()) {
      GROUP = new KQueueEventLoopGroup(PoolSpec.UNCAUGHT_FACTORY);
      CHANNEL = KQueueDatagramChannel.class;
    } else {
      GROUP = new NioEventLoopGroup(PoolSpec.UNCAUGHT_FACTORY);
      CHANNEL = NioDatagramChannel.class;
    }
  }

  /**
   * ctor.
   *
   * @param address the address.
   */
  protected NetSocket(@NotNull final InetSocketAddress address) {
    this.address = address;
    this.bootstrap
      .option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
      .group(NetSocket.GROUP)
      .channel(NetSocket.CHANNEL);
  }

  @NotNull
  @Override
  public final CompletableFuture<Void> bind() {
    Preconditions.checkState(this.running.compareAndSet(false, true), "NetSocket has already been started");
    return this.exec()
      .whenComplete((unused, throwable) -> {
        if (throwable != null) {
          this.running.compareAndSet(true, false);
          return;
        }
        this.closed.set(false);
        this.tickFuture = NetSocket.GROUP.next().scheduleAtFixedRate(this::tick, 0, 10,
          TimeUnit.MILLISECONDS);
      });
  }

  @NotNull
  @Override
  public final InetSocketAddress getAddress() {
    return this.address;
  }

  @NotNull
  @Override
  public final Bootstrap getBootstrap() {
    return this.bootstrap;
  }

  @Override
  public final long getUniqueId() {
    return this.uniqueId;
  }

  @Override
  public final boolean isClosed() {
    return this.closed.get();
  }

  @SuppressWarnings("DesignForExtension")
  @Override
  public void close() {
    this.closed.set(true);
    Optional.ofNullable(this.tickFuture).ifPresent(future ->
      future.cancel(false));
  }
}
