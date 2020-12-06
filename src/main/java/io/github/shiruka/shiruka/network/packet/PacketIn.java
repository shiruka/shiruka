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

package io.github.shiruka.shiruka.network.packet;

import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.netty.channel.socket.DatagramPacket;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Packet} that determines incoming packets.
 */
public abstract class PacketIn extends Packet {

  /**
   * ctor.
   *
   * @param id the id.
   */
  protected PacketIn(final int id) {
    super(id);
  }

  /**
   * reads the packet that was sent by the injected connection.
   *
   * @param packet the packet to read.
   * @param connection the connection to read.
   */
  public abstract void read(@NotNull DatagramPacket packet, @NotNull Connection<ServerSocket> connection);
}
