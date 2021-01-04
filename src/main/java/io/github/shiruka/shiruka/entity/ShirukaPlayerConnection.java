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

package io.github.shiruka.shiruka.entity;

import io.github.shiruka.shiruka.ShirukaServer;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link PlayerConnection}.
 */
public final class ShirukaPlayerConnection implements PlayerConnection {

  /**
   * the connection.
   */
  @NotNull
  private final Connection<ServerSocket> connection;

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the state.
   */
  @NotNull
  private PlayerConnection.State state = State.HANDSHAKE;

  /**
   * ctor.
   *
   * @param connection the connection.
   * @param server the server.
   */
  public ShirukaPlayerConnection(@NotNull final Connection<ServerSocket> connection,
                                 @NotNull final ShirukaServer server) {
    this.connection = connection;
    this.server = server;
  }

  @NotNull
  @Override
  public Connection<ServerSocket> getConnection() {
    return this.connection;
  }

  @NotNull
  @Override
  public ShirukaServer getServer() {
    return this.server;
  }

  @NotNull
  @Override
  public PlayerConnection.State getState() {
    return this.state;
  }

  @Override
  public void setState(@NotNull final PlayerConnection.State state) {
    this.state = state;
  }

  @Override
  public void sendPacket(@NotNull final PacketOut packet) {
    this.connection.checkForClosed();
    this.connection.addQueuedPacket(packet);
  }
}
