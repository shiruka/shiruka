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

package net.shiruka.shiruka.network.packets;

import io.netty.buffer.ByteBuf;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;

/**
 * sends by the client at the start of the game.
 * it is sent to let the server know if it supports the client-side blob cache.
 * clients such as Nintendo Switch do not support the cache, and attempting to use it anyway will fail.
 */
public final class ClientCacheStatusPacket extends ShirukaPacket {

  /**
   * the blob cache support.
   */
  private boolean blobCacheSupport;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ClientCacheStatusPacket(@NotNull final ByteBuf original) {
    super(ShirukaPacket.ID_CLIENT_CACHE_STATUS, original);
  }

  /**
   * ctor.
   */
  public ClientCacheStatusPacket() {
    super(ShirukaPacket.ID_CLIENT_CACHE_STATUS);
  }

  @Override
  public void decode() {
    this.blobCacheSupport = this.readBoolean();
  }

  @Override
  public void handle(@NotNull final PacketHandler handler) {
    handler.clientCacheStatusPacket(this);
  }

  /**
   * obtains the block cache support.
   *
   * @return blob cache support.
   */
  public boolean isBlobCacheSupport() {
    return this.blobCacheSupport;
  }
}
