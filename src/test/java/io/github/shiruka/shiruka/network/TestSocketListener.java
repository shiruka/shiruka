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

import io.github.shiruka.shiruka.misc.Loggers;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * a class that implements {@link SocketListener}.
 */
public final class TestSocketListener implements SocketListener {

  /**
   * the singleton instance.
   */
  static final TestSocketListener INSTANCE = new TestSocketListener();

  /**
   * ctor.
   */
  private TestSocketListener() {
  }

  @Override
  public boolean onConnect(@NotNull final InetSocketAddress address) {
    return true;
  }

  @Override
  public void onConnectionCreation(@NotNull final Connection<ServerSocket, ServerConnectionHandler> connection) {
    Loggers.useLogger(logger ->
      logger.debug("onConnectionCreation"));
  }

  @Override
  public void onConnectionStateChanged(@NotNull final ConnectionState state) {
    Loggers.useLogger(logger ->
      logger.debug("onConnectionStateChanged"));
  }

  @Override
  public void onDirect(@NotNull final ByteBuf packet) {
    Loggers.useLogger(logger ->
      logger.debug("onDirect"));
  }

  @Override
  public void onDisconnect(@NotNull final DisconnectReason reason) {
    Loggers.useLogger(logger ->
      logger.debug("onDisconnect"));
  }

  @Override
  public void onEncapsulated(@NotNull final EncapsulatedPacket packet) {
    Loggers.useLogger(logger ->
      logger.debug("onEncapsulated"));
  }

  @Override
  public byte[] onRequestServerData(@NotNull final ServerSocket server, @NotNull final InetSocketAddress requester) {
    return SocketListener.createOne(server, "Test server description.", 0, 10);
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
      System.out.println("*********************");
    } catch (final Exception e) {
      Loggers.useLogger(logger ->
        logger.error("Error whilst handling packet " + e));
    }
  }
}
