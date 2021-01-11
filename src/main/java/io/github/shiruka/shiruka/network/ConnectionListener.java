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

package io.github.shiruka.shiruka.network;

import io.github.shiruka.shiruka.network.objects.EncapsulatedPacket;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * a connection listener to handle custom events.
 */
public interface ConnectionListener {

  /**
   * runs when a packet directly received.
   *
   * @param packet the packet to receive.
   */
  void onDirect(@NotNull ByteBuf packet);

  /**
   * runs when the connection disconnected.
   *
   * @param reason the reason to disconnect.
   */
  void onDisconnect(@NotNull DisconnectReason reason);

  /**
   * runs when an encapsulated packet received.
   *
   * @param packet the packet to receive.
   */
  void onEncapsulated(@NotNull EncapsulatedPacket packet);

  /**
   * runs when the connection's state changes.
   *
   * @param old the old statement of the connection.
   * @param state the state to change.
   */
  void onStateChanged(@NotNull ConnectionState old, @NotNull ConnectionState state);
}
