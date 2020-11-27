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

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * connection's handler interface.
 */
public interface ConnectionHandler {

  /**
   * runs when the connection close.
   */
  void onClose();

  /**
   * runs when a raw packet received.
   *
   * @param packet the packet to receive.
   */
  void onRawDatagram(@NotNull ByteBuf packet);

  /**
   * sends connected ping packet to the connection.
   *
   * @param pingTime the pingTime to send as ping.
   */
  void sendConnectedPing(long pingTime);

  /**
   * sends disconnection packet to the connection.
   */
  void sendDisconnectionNotification();
}
