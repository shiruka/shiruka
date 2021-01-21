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

import io.netty.buffer.ByteBuf;
import net.shiruka.shiruka.network.Connection;
import net.shiruka.shiruka.network.ConnectionListener;
import net.shiruka.shiruka.network.ConnectionState;
import net.shiruka.shiruka.network.DisconnectReason;
import net.shiruka.shiruka.network.objects.EncapsulatedPacket;
import net.shiruka.shiruka.network.protocol.Protocol;
import net.shiruka.shiruka.network.server.ServerSocket;
import net.shiruka.shiruka.network.util.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ConnectionListener}.
 */
public final class ShirukaConnectionListener implements ConnectionListener {

  /**
   * the connection.
   */
  @NotNull
  private final Connection<ServerSocket> connection;

  /**
   * the player.
   */
  @NotNull
  private final PlayerConnection playerConnection;

  /**
   * ctor.
   *
   * @param playerConnection the connection.
   */
  ShirukaConnectionListener(@NotNull final PlayerConnection playerConnection) {
    this.playerConnection = playerConnection;
    this.connection = this.playerConnection.getConnection();
  }

  @Override
  public void onDirect(@NotNull final ByteBuf packet) {
  }

  @Override
  public void onDisconnect(@NotNull final DisconnectReason reason) {
  }

  @Override
  public void onEncapsulated(@NotNull final EncapsulatedPacket packet) {
    if (this.connection.getState() != ConnectionState.CONNECTED) {
      return;
    }
    final var buffer = packet.getBuffer();
    final var packetId = buffer.readUnsignedByte();
    if (packetId == Constants.BATCH_MAGIC) {
      this.onWrappedPacket(buffer);
    }
  }

  @Override
  public void onStateChanged(@NotNull final ConnectionState old, @NotNull final ConnectionState state) {
  }

  /**
   * handles wrapped packets.
   *
   * @param buf the buf to handle.
   */
  private void onWrappedPacket(@NotNull final ByteBuf buf) {
    buf.markReaderIndex();
    Protocol.deserialize(buf, this.playerConnection);
  }
}
