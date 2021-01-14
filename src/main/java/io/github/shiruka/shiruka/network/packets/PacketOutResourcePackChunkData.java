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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.util.VarInts;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack chunk data packets.
 */
public final class PacketOutResourcePackChunkData extends PacketOut {

  /**
   * the chunk index.
   */
  private final int chunkIndex;

  /**
   * the data.
   */
  private final byte[] data;

  /**
   * the pack id.
   */
  @NotNull
  private final UUID packId;

  /**
   * the pack version.
   */
  @Nullable
  private final String packVersion;

  /**
   * the progress.
   */
  private final long progress;

  /**
   * ctor.
   *
   * @param chunkIndex the chunk index.
   * @param data the data.
   * @param packId the pack id.
   * @param packVersion the pack version.
   * @param progress the progress.
   */
  public PacketOutResourcePackChunkData(final int chunkIndex, final byte[] data, @NotNull final UUID packId,
                                        @Nullable final String packVersion, final long progress) {
    super(PacketOutResourcePackChunkData.class);
    this.chunkIndex = chunkIndex;
    this.data = data.clone();
    this.packId = packId;
    this.packVersion = packVersion;
    this.progress = progress;
  }

  @Override
  public void write(@NotNull final ByteBuf buf) {
    final var packInfo = this.packId.toString() + (this.packVersion == null ? "" : '_' + this.packVersion);
    VarInts.writeString(buf, packInfo);
    buf.writeIntLE(this.chunkIndex);
    buf.writeLongLE(this.progress);
    VarInts.writeByteArray(buf, this.data);
  }
}
