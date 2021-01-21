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
import java.net.InetSocketAddress;
import java.util.function.Function;
import net.shiruka.shiruka.network.Connection;
import net.shiruka.shiruka.network.ConnectionHandler;
import net.shiruka.shiruka.network.NetConnection;
import org.jetbrains.annotations.NotNull;

/**
 * a class that provides you to manage the connection.
 */
public final class NetServerConnection extends NetConnection<ServerSocket> {

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
  NetServerConnection(@NotNull final ServerSocket socket,
                      @NotNull final Function<Connection<ServerSocket>, ConnectionHandler> handler,
                      @NotNull final InetSocketAddress address, @NotNull final ChannelHandlerContext ctx, final int mtu,
                      final short protocolVersion) {
    super(socket, handler, address, ctx, mtu, protocolVersion);
  }
}
