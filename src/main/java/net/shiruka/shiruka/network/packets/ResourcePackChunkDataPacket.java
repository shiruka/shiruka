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

import java.util.UUID;
import net.shiruka.shiruka.network.ShirukaPacket;
import net.shiruka.shiruka.network.VarInts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack chunk data packets.
 */
public final class ResourcePackChunkDataPacket extends ShirukaPacket {

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
  public ResourcePackChunkDataPacket(final int chunkIndex, final byte[] data, @NotNull final UUID packId,
                                     @Nullable final String packVersion, final long progress) {
    super(ShirukaPacket.ID_RESOURCE_PACK_CHUNK_DATA);
    this.chunkIndex = chunkIndex;
    this.data = data.clone();
    this.packId = packId;
    this.packVersion = packVersion;
    this.progress = progress;
  }

  @Override
  public void encode() {
    final var packInfo = this.getPackId().toString() + (this.getPackVersion() == null
      ? ""
      : '_' + this.getPackVersion());
    VarInts.writeString(this.buffer(), packInfo);
    this.writeIntLE(this.getChunkIndex());
    this.writeLongLE(this.getProgress());
    this.writeByteArray(this.getData());
  }

  /**
   * obtains the chunk index.
   *
   * @return chunk index.
   */
  public int getChunkIndex() {
    return this.chunkIndex;
  }

  /**
   * obtains the data.
   *
   * @return data.
   */
  public byte[] getData() {
    return this.data.clone();
  }

  /**
   * obtains tha pack id.
   *
   * @return pack id.
   */
  @NotNull
  public UUID getPackId() {
    return this.packId;
  }

  /**
   * obtains the pack version.
   *
   * @return pack version.
   */
  @Nullable
  public String getPackVersion() {
    return this.packVersion;
  }

  /**
   * obtains the progress.
   *
   * @return progress.
   */
  public long getProgress() {
    return this.progress;
  }
}
