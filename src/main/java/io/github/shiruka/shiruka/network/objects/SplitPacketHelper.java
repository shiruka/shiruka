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

package io.github.shiruka.shiruka.network.objects;

import com.google.common.base.Preconditions;
import io.github.shiruka.shiruka.network.Connection;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that helps to manage split packets.
 */
public final class SplitPacketHelper extends AbstractReferenceCounted {

  /**
   * the created time.
   */
  private final long created = System.currentTimeMillis();

  /**
   * the packets.
   */
  @Nullable
  private final EncapsulatedPacket[] packets;

  /**
   * ctor.
   *
   * @param expectedLength the expected length.
   */
  public SplitPacketHelper(final long expectedLength) {
    Preconditions.checkState(expectedLength >= 1, "expectedLength is less than 1 (%s)", expectedLength);
    this.packets = new EncapsulatedPacket[(int) expectedLength];
  }

  /**
   * adds the given encapsulated packet into {@link #packets}.
   *
   * @param packet the packet to add.
   * @param connection the connection to add.
   *
   * @return splited packet.
   */
  @Nullable
  public EncapsulatedPacket add(@NotNull final EncapsulatedPacket packet, @NotNull final Connection<?> connection) {
    Preconditions.checkState(packet.split, "packet is not split");
    Preconditions.checkState(this.refCnt() > 0, "packet has been released");
    Objects.checkIndex(packet.partIndex, this.packets.length);
    final var partIndex = packet.partIndex;
    if (this.packets[partIndex] != null) {
      return null;
    }
    this.packets[partIndex] = packet;
    packet.retain();
    int sz = 0;
    for (final var netPacket : this.packets) {
      if (netPacket == null) {
        return null;
      }
      sz += netPacket.getBuffer().readableBytes();
    }
    final var reassembled = connection.allocateBuffer(sz);
    Arrays.stream(this.packets)
      .filter(Objects::nonNull)
      .map(EncapsulatedPacket::getBuffer)
      .forEach(buf -> reassembled.writeBytes(buf, buf.readerIndex(), buf.readableBytes()));
    return packet.fromSplit(reassembled);
  }

  /**
   * checks if the packet is expired.
   *
   * @return {@code true} if the packet is expired.
   */
  public boolean expired() {
    Preconditions.checkState(this.refCnt() > 0, "packet has been released");
    return System.currentTimeMillis() - this.created >= 30000;
  }

  @Override
  public ReferenceCounted touch(final Object hint) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void deallocate() {
    Arrays.stream(this.packets)
      .forEach(ReferenceCountUtil::release);
  }
}
