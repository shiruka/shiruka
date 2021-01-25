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
import java.util.Objects;
import java.util.UUID;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack chunk request packets.
 */
public final class ResourcePackChunkRequestPacket extends ShirukaPacket {

  /**
   * the chunk size.
   */
  private int chunkSize;

  /**
   * the pack id.
   */
  @Nullable
  private UUID packId;

  /**
   * the version.
   */
  @Nullable
  private String version;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ResourcePackChunkRequestPacket(@NotNull final ByteBuf original) {
    super(ShirukaPacket.ID_RESOURCE_PACK_CHUNK_REQUEST, original);
  }

  @Override
  public void decode() {
    final var packInfo = this.readString().split("_");
    this.packId = UUID.fromString(packInfo[0]);
    if (packInfo.length > 1) {
      this.version = packInfo[1];
    } else {
      this.version = null;
    }
    this.chunkSize = this.readIntLE();
  }

  @Override
  public void handle(@NotNull final PacketHandler handler) {
    handler.resourcePackChunkRequestPacket(this);
  }

  /**
   * obtains the chunk size.
   *
   * @return chunk size.
   */
  public int getChunkSize() {
    return this.chunkSize;
  }

  /**
   * obtains the pack id.
   *
   * @return pack id.
   */
  @NotNull
  public UUID getPackId() {
    return Objects.requireNonNull(this.packId);
  }

  /**
   * obtains the version.
   *
   * @return version.
   */
  @Nullable
  public String getVersion() {
    return this.version;
  }
}
