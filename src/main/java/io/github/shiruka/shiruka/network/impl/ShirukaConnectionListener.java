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

import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.ConnectionListener;
import io.github.shiruka.shiruka.network.ConnectionState;
import io.github.shiruka.shiruka.network.DisconnectReason;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.github.shiruka.shiruka.network.protocol.Protocol;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.util.Constants;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final ShirukaPlayer player;

  /**
   * ctor.
   *
   * @param player the connection.
   */
  ShirukaConnectionListener(@NotNull final ShirukaPlayer player) {
    this.player = player;
    this.connection = this.player.getPlayerConnection().getConnection();
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
    Protocol.deserialize(buf, this.player);
  }
}
