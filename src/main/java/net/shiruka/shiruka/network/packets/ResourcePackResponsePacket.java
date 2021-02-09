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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;
import java.util.stream.IntStream;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import net.shiruka.shiruka.network.VarInts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack response packets.
 */
public final class ResourcePackResponsePacket extends ShirukaPacket {

  /**
   * the packs.
   */
  @Nullable
  private List<Entry> packs;

  /**
   * the status.
   */
  @Nullable
  private Status status;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ResourcePackResponsePacket(final @NotNull ByteBuf original) {
    super(ShirukaPacket.ID_RESOURCE_PACK_RESPONSE, original);
  }

  @Override
  public void decode() {
    this.status = Status.valueOf(this.readByte());
    final var length = this.readShortLE();
    final var packs = new ObjectArrayList<Entry>();
    IntStream.range(0, length)
      .mapToObj(i -> VarInts.readString(this.buffer()).split("_"))
      .forEach(uniqueIdVersion -> {
        try {
          packs.add(new Entry(UUID.fromString(uniqueIdVersion[0]), uniqueIdVersion[1]));
        } catch (final Exception ignored) {
        }
      });
    this.packs = packs;
  }

  @Override
  public void handle(@NotNull final PacketHandler handler) {
    handler.resourcePackResponsePacket(this);
  }

  @NotNull
  public List<Entry> getPacks() {
    return Collections.unmodifiableList(Objects.requireNonNull(this.packs));
  }

  /**
   * obtains the status.
   *
   * @return status.
   */
  @NotNull
  public Status getStatus() {
    return Objects.requireNonNull(this.status);
  }

  /**
   * an enum class that represents resource pack response status.
   */
  public enum Status {
    /**
     * the refused.
     */
    REFUSED(1),
    /**
     * the send packs.
     */
    SEND_PACKS(2),
    /**
     * the have all packs.
     */
    HAVE_ALL_PACKS(3),
    /**
     * the completed.
     */
    COMPLETED(4);

    /**
     * the value cache.
     */
    private static final Status[] VALUES = Status.values();

    /**
     * the id.
     */
    private final byte id;

    /**
     * ctor.
     *
     * @param id the id.
     */
    Status(final int id) {
      this.id = (byte) id;
    }

    /**
     * obtains the status from the status id.
     *
     * @param statusId the status id to get.
     *
     * @return status value.
     */
    @Nullable
    public static Status valueOf(final byte statusId) {
      return Arrays.stream(Status.VALUES)
        .filter(status -> status.id == statusId)
        .findFirst()
        .orElse(null);
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
