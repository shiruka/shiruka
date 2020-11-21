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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * a class that covers socket's methods.
 */
public interface Socket extends AutoCloseable {

  /**
   * sends the packet to the given recipient.
   *
   * @param ctx the context.
   * @param packet the packet to send.
   * @param recipient the recipient to send.
   *
   * @see ChannelHandlerContext#writeAndFlush(Object)
   */
  static void send(@NotNull final ChannelHandlerContext ctx, @NotNull final ByteBuf packet,
                   @NotNull final InetSocketAddress recipient) {
    ctx.writeAndFlush(new DatagramPacket(packet, recipient));
  }

  /**
   * sends the packet to the given recipient with {@link ChannelHandlerContext#voidPromise()}.
   *
   * @param ctx the context.
   * @param packet the packet to send.
   * @param recipient the recipient to send.
   *
   * @see ChannelHandlerContext#writeAndFlush(Object, ChannelPromise)
   */
  static void sendWithPromise(@NotNull final ChannelHandlerContext ctx, @NotNull final ByteBuf packet,
                              @NotNull final InetSocketAddress recipient) {
    ctx.writeAndFlush(new DatagramPacket(packet, recipient), ctx.voidPromise());
  }

  /**
   * runs every tick.
   */
  void onTick();

  /**
   * socket's the unique id.
   *
   * @return the unique id to determine the socket.
   */
  long getUniqueId();

  /**
   * socket's address.
   *
   * @return the address to determine the socket's ip and port.
   */
  @NotNull
  InetSocketAddress getAddress();

  /**
   * socket's listener.
   *
   * @return the listener to handler socket's events.
   */
  @NotNull
  SocketListener getSocketListener();

  /**
   * socket's bootstrap.
   *
   * @return the bootstrap to bind the socket.
   */
  @NotNull
  Bootstrap getBootstrap();

  /**
   * socket's status in terms of closing.
   *
   * @return if the socket is closed returns true otherwise, false.
   */
  boolean isClosed();

  /**
   * binds the socket.
   *
   * @return a {@link CompletableFuture} to listen or what you do want.
   */
  @NotNull
  CompletableFuture<Void> bind();

  /**
   * binds the server.
   *
   * @return a {@link CompletableFuture} to listen or what you do want.
   */
  @NotNull
  CompletableFuture<Void> exec();
}
