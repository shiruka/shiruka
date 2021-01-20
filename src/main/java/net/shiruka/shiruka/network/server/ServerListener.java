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
package net.shiruka.shiruka.network.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import net.shiruka.shiruka.network.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * a socket listener to handle custom events.
 */
public interface ServerListener {

  /**
   * runs before when a client connects to the server.
   *
   * @param address the address to connect.
   *
   * @return if it's true the client will be connect into the server,
   *   if not the client can't connect into the server and,
   *   the server will send a packet, that says the connection is baned, to the client.
   */
  boolean onConnect(@NotNull InetSocketAddress address);

  /**
   * runs when a connection created between server and client.
   *
   * @param connection the connection.
   */
  void onConnectionCreation(@NotNull Connection<ServerSocket> connection);

  /**
   * runs before sending a ping packet to the client.
   *
   * @param server the server.
   * @param requester the requester .
   *
   * @return a server data.
   */
  @NotNull
  CompletableFuture<byte[]> onRequestServerData(@NotNull ServerSocket server, @NotNull InetSocketAddress requester);

  /**
   * runs when a raw datagram packet received.
   *
   * @param server the server to receive.
   * @param ctx the context to receive.
   * @param packet the packet to receive.
   */
  void onUnhandledDatagram(@NotNull ServerSocket server, @NotNull ChannelHandlerContext ctx,
                           @NotNull DatagramPacket packet);
}
