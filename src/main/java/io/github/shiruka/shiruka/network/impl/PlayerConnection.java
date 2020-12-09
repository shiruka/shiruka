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
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.PacketPriority;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine player's connection.
 */
public interface PlayerConnection {

  /**
   * obtains the original connection.
   *
   * @return the original connection.
   */
  @NotNull
  Connection<ServerSocket> getConnection();

  /**
   * obtains the server.
   *
   * @return the server.
   */
  @NotNull
  Server getServer();

  /**
   * obtains the state.
   *
   * @return the state.
   */
  @NotNull
  State getState();

  /**
   * sets the state.
   *
   * @param state the state to set.
   */
  void setState(@NotNull State state);

  /**
   * sends the given packet to the player's connection.
   *
   * @param packet the packet to send.
   */
  default void sendPacket(@NotNull final PacketOut packet) {
    this.sendPacket(packet, PacketPriority.MEDIUM);
  }

  /**
   * sends the given packet to the player's connection.
   *
   * @param packet the packet to send.
   * @param priority the priority to send.
   */
  void sendPacket(@NotNull PacketOut packet, @NotNull PacketPriority priority);

  /**
   * represents the current connection state that the client is in whilst connecting to the server.
   */
  enum State {
    /**
     * handshake, attempting to connect to server.
     */
    HANDSHAKE,
    /**
     * obtain server status via ping.
     */
    STATUS,
    /**
     * login, authenticate and complete connection formalities before joining.
     */
    LOGIN,
    /**
     * normal gameplay.
     */
    PLAY
  }
}
