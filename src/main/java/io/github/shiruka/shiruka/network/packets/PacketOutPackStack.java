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
 * pack stack packet.
 *
 * @todo #1:1m Add experimental field.
 */
public final class PacketOutPackStack extends PacketOut {

  /**
   * the entries.
   */
  @NotNull
  private final List<Entry> entries;

  /**
   * the game version.
   */
  @NotNull
  private final String gameVersion;

  /**
   * the must accept.
   */
  private final boolean mustAccept;

  /**
   * ctor.
   *
   * @param mustAccept the must accept.
   * @param gameVersion the game version.
   * @param entries the entries.
   */
  public PacketOutPackStack(@NotNull final List<Entry> entries, @NotNull final String gameVersion,
                            final boolean mustAccept) {
    super(PacketOutPackStack.class);
    this.entries = Collections.unmodifiableList(entries);
    this.gameVersion = gameVersion;
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
     * the pack id.
     */
    @NotNull
    private final String packId;

    /**
     * the pack version.
     */
    @NotNull
    private final String packVersion;

    /**
     * the sub pack name.
     */
    @NotNull
    private final String subPackName;

    /**
     * ctor.
     *
     * @param packId the pack id.
     * @param packVersion the pack version.
     * @param subPackName the sub pack name.
     */
    public Entry(@NotNull final String packId, @NotNull final String packVersion, @NotNull final String subPackName) {
      this.packId = packId;
      this.packVersion = packVersion;
      this.subPackName = subPackName;
    }
  }
}
