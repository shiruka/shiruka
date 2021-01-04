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

package io.github.shiruka.shiruka.network.impl;

import io.github.shiruka.shiruka.ShirukaServer;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.entity.ShirukaPlayerConnection;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
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
    final var playerConnection = new ShirukaPlayerConnection(connection, this.server);
    final var player = new ShirukaPlayer(playerConnection);
    final var connectionListener = new ShirukaConnectionListener(player);
    connection.setConnectionListener(connectionListener);
  }

  @Override
  public byte[] onRequestServerData(@NotNull final ServerSocket server, @NotNull final InetSocketAddress requester) {
    return ServerListener.createOne(server, this.server.getServerDescription(), this.server.getPlayerCount(),
      this.server.getMaxPlayerCount());
  }

  @Override
  public void onUnhandledDatagram(@NotNull final ServerSocket server, @NotNull final ChannelHandlerContext ctx,
                                  @NotNull final DatagramPacket packet) {
  }
}
