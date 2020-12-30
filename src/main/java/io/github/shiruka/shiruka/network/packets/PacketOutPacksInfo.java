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

import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * packs info packet.
 */
public final class PacketOutPacksInfo extends PacketOut {

  /**
   * the entries.
   */
  @NotNull
  private final List<Entry> entries;

  /**
   * the must accept.
   */
  private final boolean mustAccept;

  /**
   * ctor.
   *
   * @param mustAccept the must accept.
   * @param entries the info entries
   */
  public PacketOutPacksInfo(@NotNull final List<Entry> entries, final boolean mustAccept) {
    super(PacketOutPacksInfo.class);
    this.entries = Collections.unmodifiableList(entries);
    this.mustAccept = mustAccept;
  }

  @Override
  public void write(@NotNull final ByteBuf buf) {
  }

  /**
   * a class that represents entries of {@code this} packet.
   */
  public static final class Entry {

    /**
     * the the content id.
     */
    @NotNull
    private final String contentId;

    /**
     * the content key.
     */
    @NotNull
    private final String contentKey;

    /**
     * the pack id.
     */
    @NotNull
    private final String packId;

    /**
     * the pack size.
     */
    private final long packSize;

    /**
     * the pack version.
     */
    @NotNull
    private final String packVersion;

    /**
     * the raytracing capable.
     */
    private final boolean raytracingCapable;

    /**
     * the scripting.
     */
    private final boolean scripting;

    /**
     * the sub pack name.
     */
    @NotNull
    private final String subPackName;

    /**
     * ctor.
     *
     * @param contentId the content id.
     * @param contentKey the content key.
     * @param packId the pack id.
     * @param packSize the pack size.
     * @param packVersion the pack version.
     * @param raytracingCapable the raytracing capable
     * @param scripting the scripting.
     * @param subPackName the sub pack name.
     */
    public Entry(@NotNull final String contentId, @NotNull final String contentKey, @NotNull final String packId,
                 final long packSize, @NotNull final String packVersion, final boolean raytracingCapable,
                 final boolean scripting, @NotNull final String subPackName) {
      this.contentId = contentId;
      this.contentKey = contentKey;
      this.packId = packId;
      this.packSize = packSize;
      this.packVersion = packVersion;
      this.raytracingCapable = raytracingCapable;
      this.scripting = scripting;
      this.subPackName = subPackName;
    }
  }
}
