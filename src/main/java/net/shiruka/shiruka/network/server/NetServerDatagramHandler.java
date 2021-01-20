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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.Optional;
import net.shiruka.shiruka.network.util.Packets;
import org.jetbrains.annotations.NotNull;

/**
 * a simple server datagram handler.
 */
@ChannelHandler.Sharable
final class NetServerDatagramHandler extends MessageToMessageDecoder<DatagramPacket> {

  /**
   * the server socket instance.
   */
  @NotNull
  private final ServerSocket server;

  /**
   * ctor.
   *
   * @param server the server socket.
   */
  NetServerDatagramHandler(@NotNull final ServerSocket server) {
    this.server = server;
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    this.server.getExceptionHandlers().values().forEach(cons ->
      cons.accept(cause));
  }

  @Override
  public void handlerAdded(final ChannelHandlerContext ctx) {
    this.server.addChannel(ctx.channel());
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, final DatagramPacket datagram, final List<Object> out) {
    final var sender = datagram.sender();
    if (this.server.getBlockedAddresses().containsKey(sender.getAddress())) {
      return;
    }
    final var content = datagram.content();
    if (!content.isReadable()) {
      return;
    }
    if (Packets.handleNoConnectionPackets(ctx, this.server, datagram)) {
      return;
    }
    content.readerIndex(0);
    Optional.ofNullable(this.server.getConnectionsByAddress().get(sender))
      .ifPresent(con -> con.getConnectionHandler().onRawDatagram(content));
    content.readerIndex(0);
    this.server.getServerListener().onUnhandledDatagram(this.server, ctx, datagram);
  }
}
