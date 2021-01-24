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

package net.shiruka.shiruka.network.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import net.shiruka.api.server.ServerDescription;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.network.Connection;
import net.shiruka.shiruka.network.server.ServerListener;
import net.shiruka.shiruka.network.server.ServerSocket;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ServerListener}.
 */
public final class ShirukaServerListener implements ServerListener {

  /**
   * the server instance.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaServerListener(@NotNull final ShirukaServer server) {
    this.server = server;
  }

  @Override
  public boolean onConnect(@NotNull final InetSocketAddress address) {
    return true;
  }

  @Override
  public void onConnectionCreation(@NotNull final Connection<ServerSocket> connection) {
    final var playerConnection = new PlayerConnection(connection, this.server);
    final var connectionListener = new ShirukaConnectionListener(playerConnection);
    connection.setConnectionListener(connectionListener);
  }

  @Override
  public byte[] onRequestServerData(@NotNull final ServerSocket server,
                                                       @NotNull final InetSocketAddress requester) {
    return this.server.getServerDescription().toPacket();
  }

  @Override
  public void onUnhandledDatagram(@NotNull final ServerSocket server, @NotNull final ChannelHandlerContext ctx,
                                  @NotNull final DatagramPacket packet) {
  }
}
