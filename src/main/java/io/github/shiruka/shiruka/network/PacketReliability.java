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

package io.github.shiruka.shiruka.network;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * reliability of the packets.
 */
public enum PacketReliability {
  /**
   * the unreliable.
   */
  UNRELIABLE(false, false, false, false),
  /**
   * the unreliable sequenced.
   */
  UNRELIABLE_SEQUENCED(false, false, true, false),
  /**
   * the unreliable with ack receipt.
   */
  UNRELIABLE_WITH_ACK_RECEIPT(false, false, false, true),
  /**
   * the reliable.
   */
  RELIABLE(true, false, false, false),
  /**
   * the reliable ordered.
   */
  RELIABLE_ORDERED(true, true, false, false),
  /**
   * the reliable sequenced.
   */
  RELIABLE_SEQUENCED(true, false, true, false),
  /**
   * the reliable with ack receipt.
   */
  RELIABLE_WITH_ACK_RECEIPT(true, false, false, true),
  /**
   * the reliable ordered with ack receipt.
   */
  RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false, true);

  /**
   * cache of the values.
   */
  private static final PacketReliability[] VALUES = PacketReliability.values();

  /**
   * is ordered or not.
   */
  private final boolean ordered;

  /**
   * is reliable or not.
   */
  private final boolean reliable;

  /**
   * is sequenced or not
   */
  private final boolean sequenced;

  /**
   * the size.
   */
  private final int size;

  /**
   * include ack receipt or not.
   */
  private final boolean withAckReceipt;

  /**
   * ctor.
   *
   * @param reliable the reliable.
   * @param ordered the ordered.
   * @param sequenced the sequenced.
   * @param withAckReceipt the withAckReceipt.
   */
  PacketReliability(final boolean reliable, final boolean ordered, final boolean sequenced,
                    final boolean withAckReceipt) {
    this.reliable = reliable;
    this.ordered = ordered;
    this.sequenced = sequenced;
    this.withAckReceipt = withAckReceipt;
    var size = 0;
    if (this.reliable) {
      size += 3;
    }
    if (this.sequenced) {
      size += 3;
    }
    if (this.ordered) {
      size += 4;
    }
    this.size = size;
  }

  /**
   * obtains packet reliability from the id.
   *
   * @param id the id to get.
   *
   * @return returns the reliability if the id is between 0 to 7 or {@link Optional#empty()}.
   */
  @NotNull
  public static Optional<PacketReliability> fromId(final int id) {
    if (id < 0 || id > 7) {
      return Optional.empty();
    }
    return Optional.ofNullable(PacketReliability.VALUES[id]);
  }

  /**
   * size of the reliability.
   *
   * @return the size as a number.
   */
  public int getSize() {
    return this.size;
  }

  /**
   * obtains {@link PacketReliability#ordered}.
   *
   * @return returns true if it's ordered.
   */
  public boolean isOrdered() {
    return this.ordered;
  }

  /**
   * obtains {@link PacketReliability#reliable}.
   *
   * @return returns true if it's reliable.
   */
  public boolean isReliable() {
    return this.reliable;
  }

  /**
   * obtains {@link PacketReliability#sequenced}.
   *
   * @return returns true if it's sequenced.
   */
  public boolean isSequenced() {
    return this.sequenced;
  }
}
