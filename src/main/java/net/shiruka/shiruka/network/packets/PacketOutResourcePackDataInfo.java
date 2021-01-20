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
import net.shiruka.api.pack.Pack;
import net.shiruka.shiruka.network.packet.PacketOut;
import net.shiruka.shiruka.network.util.VarInts;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents resource pack data info packets.
 */
public final class PacketOutResourcePackDataInfo extends PacketOut {

  /**
   * the maximum chunk size.
   */
  private static final int MAX_CHUNK_SIZE = 1048576;

  @NotNull
  private final Pack pack;

  /**
   * ctor.
   *
   * @param pack the pack.
   */
  public PacketOutResourcePackDataInfo(@NotNull final Pack pack) {
    super(PacketOutResourcePackDataInfo.class);
    this.pack = pack;
  }

  @Override
  public void write(@NotNull final ByteBuf buf) {
    VarInts.writeString(buf, this.pack.getId().toString() + '_' + this.pack.getVersion());
    buf.writeIntLE(PacketOutResourcePackDataInfo.MAX_CHUNK_SIZE);
    buf.writeIntLE((int) (this.pack.getSize() / PacketOutResourcePackDataInfo.MAX_CHUNK_SIZE));
    buf.writeLongLE(this.pack.getSize());
    final var hash = this.pack.getHash();
    VarInts.writeUnsignedInt(buf, hash.length);
    buf.writeBytes(hash);
    buf.writeBoolean(false);
    buf.writeByte(this.pack.getType().getId());
  }
}
