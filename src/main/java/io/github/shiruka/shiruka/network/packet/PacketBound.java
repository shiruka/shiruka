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

import org.jetbrains.annotations.NotNull;

/**
 * the direction which a packet is sent towards.
 */
public enum PacketBound {
  /**
   * client-bound, out packets.
   */
  CLIENT,
  /**
   * server-bound, in packets.
   */
  SERVER;

  /**
   * obtain the bound of the packet represented by the given class.
   *
   * @param cls the class to determine the bound.
   *
   * @return the bound of the packet.
   */
  @NotNull
  public PacketBound from(@NotNull final Class<? extends Packet> cls) {
    if (cls.getSuperclass() == PacketIn.class) {
      return PacketBound.SERVER;
    }
    return PacketBound.CLIENT;
  }
}