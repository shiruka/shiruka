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

import net.shiruka.api.pack.Pack;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents resource pack data info packets.
 */
public final class ResourcePackDataInfoPacket extends ShirukaPacket {

  /**
   * the maximum chunk size.
   */
  private static final int MAX_CHUNK_SIZE = 1048576;

  /**
   * the pack.
   */
  @NotNull
  private final Pack pack;

  /**
   * ctor.
   *
   * @param pack the pack.
   */
  public ResourcePackDataInfoPacket(@NotNull final Pack pack) {
    super(ShirukaPacket.ID_RESOURCE_PACK_DATA_INFO);
    this.pack = pack;
  }

  @Override
  public void encode() {
    this.writeString(this.pack.getId().toString() + '_' + this.pack.getVersion());
    this.writeIntLE(ResourcePackDataInfoPacket.MAX_CHUNK_SIZE);
    this.writeIntLE((int) (this.pack.getSize() / ResourcePackDataInfoPacket.MAX_CHUNK_SIZE));
    this.writeLongLE(this.pack.getSize());
    final var hash = this.pack.getHash();
    this.writeUnsignedInt(hash.length);
    this.buffer().writeBytes(hash);
    this.writeBoolean(false);
    this.writeByte(this.pack.getType().getId());
  }

  /**
   * obtains the pack.
   *
   * @return pack.
   */
  @NotNull
  public Pack getPack() {
    return this.pack;
  }
}
