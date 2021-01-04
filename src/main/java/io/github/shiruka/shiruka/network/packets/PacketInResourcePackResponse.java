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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack response packets.
 */
public final class PacketInResourcePackResponse extends PacketIn {

  /**
   * ctor.
   */
  public PacketInResourcePackResponse() {
    super(PacketInResourcePackResponse.class);
  }

  @Override
  public void read(@NotNull final ByteBuf buf, @NotNull final ShirukaPlayer player) {
    final byte ordinal = buf.readByte();
    final var status = Status.valueOf(ordinal);
    final var length = buf.readShortLE();
    final var packs = new ArrayList<Entry>();
    for (var i = 0; i < length; i++) {
      final var uniqueIdVersion = VarInts.readString(buf).split("_");
      try {
        packs.add(new Entry(UUID.fromString(uniqueIdVersion[0]), uniqueIdVersion[1]));
      } catch (final Exception e) {
      }
    }
    if (status == null) {
      return;
    }
    switch (status) {
      case REFUSED:
        if (ServerConfig.FORCE_RESOURCES.getValue().orElse(false)) {
          player.disconnect("disconnectionScreen.noReason");
        }
        break;
      case COMPLETED:
        final var data = player.getLatestLoginData();
        if (data != null) {
          data.initializePlayer();
        }
        break;
      case SEND_PACKS:
        break;
      case HAVE_ALL_PACKS:
        break;
    }
  }

  /**
   * an enum class that represents resource pack response status.
   */
  public enum Status {
    /**
     * the refused.
     */
    REFUSED,
    /**
     * the send packs.
     */
    SEND_PACKS,
    /**
     * the have all packs.
     */
    HAVE_ALL_PACKS,
    /**
     * the completed.
     */
    COMPLETED;

    @Nullable
    public static Status valueOf(final byte statusId) {
      switch (statusId) {
        case 1:
          return Status.REFUSED;
        case 2:
          return Status.SEND_PACKS;
        case 3:
          return Status.HAVE_ALL_PACKS;
        case 4:
          return Status.COMPLETED;
        default:
          return null;
      }
    }
  }

  /**
   * a class that represents entries of the response packets.
   */
  public static final class Entry {

    /**
     * the unique id.
     */
    @NotNull
    public final UUID uniqueId;

    /**
     * the version.
     */
    @NotNull
    public final String version;

    /**
     * ctor.
     *
     * @param uniqueId the unique id.
     * @param version the version.
     */
    public Entry(@NotNull final UUID uniqueId, @NotNull final String version) {
      this.uniqueId = uniqueId;
      this.version = version;
    }

    /**
     * obtains the unique id.
     *
     * @return unique id.
     */
    @NotNull
    public UUID getUniqueId() {
      return this.uniqueId;
    }

    /**
     * obtains the version.
     *
     * @return version.
     */
    @NotNull
    public String getVersion() {
      return this.version;
    }
  }
}
