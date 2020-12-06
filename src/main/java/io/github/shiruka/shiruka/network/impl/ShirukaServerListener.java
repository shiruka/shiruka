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

import io.github.shiruka.api.Server;
import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.server.ServerConnectionHandler;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ServerListener}.
 */
public final class ShirukaServerListener implements ServerListener {

  /**
   * the server instance.
   */
  @NotNull
  private final Server server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaServerListener(@NotNull final Server server) {
    this.server = server;
  }

  @Override
  public boolean onConnect(@NotNull final InetSocketAddress address) {
    return true;
  }

  @Override
  public void onConnectionCreation(@NotNull final Connection<ServerSocket, ServerConnectionHandler> connection) {
    connection.setConnectionListener(new ShirukaConnectionListener(connection));
    Loggers.debug("onConnectionCreation");
  }

  @Override
  public byte[] onRequestServerData(@NotNull final ServerSocket server, @NotNull final InetSocketAddress requester) {
    return ServerListener.createOne(server, this.server.getServerDescription(), this.server.getPlayerCount(),
      this.server.getMaxPlayerCount());
  }

  @Override
  public void onUnhandledDatagram(@NotNull final ServerSocket server, @NotNull final ChannelHandlerContext ctx,
                                  @NotNull final DatagramPacket packet) {
    final var buffer = packet.content();
    try {
      if (!buffer.isReadable(3)) {
        return;
      }
      final var prefix = new byte[2];
      buffer.readBytes(prefix);
      if (!Arrays.equals(prefix, new byte[]{(byte) 0xfe, (byte) 0xfd})) {
        return;
      }
      final var packetId = buffer.readUnsignedByte();
      final var sessionId = buffer.readInt();
      System.out.println("onUnhandledDatagram -> " + packetId);
    } catch (final Exception e) {
      Loggers.error("Error whilst handling packet ", e);
    }
  }
}
