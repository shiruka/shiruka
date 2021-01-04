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

import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Packet} that determines incoming packets.
 *
 * @todo #1:30m Add a simple singleton field for packets which extend PacketIn class.
 *   And don't create packets over and over again to improve performance.
 */
public abstract class PacketIn extends Packet {

  /**
   * ctor.
   *
   * @param cls the packet class.
   */
  protected PacketIn(@NotNull final Class<? extends Packet> cls) {
    super(cls);
  }

  /**
   * reads the buf that was sent by the injected player.
   *
   * @param buf the buf to read.
   * @param player the player to read.
   */
  public abstract void read(@NotNull ByteBuf buf, @NotNull ShirukaPlayer player);
}
