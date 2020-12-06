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

package io.github.shiruka.shiruka.network.server;

import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple server datagram handler.
 */
@ChannelHandler.Sharable
final class NetServerOutboundHandler extends MessageToMessageEncoder<PacketOut> {

  /**
   * the server socket instance.
   */
  @NotNull
  private final ServerSocket server;

  /**
   * the latest decrypted buffer.
   */
  @Nullable
  private ByteBuf lastDecrypted;

  /**
   * ctor.
   *
   * @param server the server socket.
   */
  NetServerOutboundHandler(@NotNull final ServerSocket server) {
    this.server = server;
  }

  @Override
  public void handlerAdded(final ChannelHandlerContext ctx) {
    this.server.addChannel(ctx.channel());
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    this.server.getExceptionHandlers().values().forEach(cons ->
      cons.accept(cause));
  }

  @Override
  protected void encode(final ChannelHandlerContext ctx, final PacketOut packet, final List<Object> out) {
    final var payload = ctx.alloc().buffer();
    try {
      VarInts.writeVarInt(payload, packet.id());
      packet.write(payload);
      final var buf = ctx.alloc().buffer();
      try {
        out.add(buf);
      } finally {
        buf.release();
      }
    } finally {
      payload.release();
    }
  }
}
