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

import io.github.shiruka.shiruka.network.PacketPriority;
import io.github.shiruka.shiruka.network.PacketReliability;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that encodes and decodes the given packets.
 */
@ToString
@EqualsAndHashCode
public final class EncapsulatedPacket implements ReferenceCounted {

  /**
   * the ordering channel.
   */
  public short orderingChannel;

  /**
   * the ordering index.
   */
  public int orderingIndex;

  /**
   * the part count.
   */
  public int partCount;

  /**
   * the part id.
   */
  public int partId;

  /**
   * the part index.
   */
  public int partIndex;

  /**
   * the reliability index.
   */
  public int reliabilityIndex;

  /**
   * the sequence index.
   */
  public int sequenceIndex;

  /**
   * the split.
   */
  public boolean split;

  /**
   * the packet itself.
   */
  @Nullable
  private ByteBuf buffer;

  /**
   * the packet's sending priority.
   */
  @Nullable
  private PacketPriority priority;

  /**
   * the packet's reliability.
   */
  @Nullable
  private PacketReliability reliability;

  /**
   * obtains the buffer.
   *
   * @return the buffer.
   */
  @NotNull
  public ByteBuf getBuffer() {
    return Objects.requireNonNull(this.buffer, "buffer");
  }

  /**
   * sets the {@link EncapsulatedPacket#buffer} to the given buffer.
   *
   * @param buffer the buffer to set.
   */
  public void setBuffer(@NotNull final ByteBuf buffer) {
    this.buffer = buffer;
  }

  /**
   * obtains the reliability of the packet.
   *
   * @return the reliability of the packet.
   */
  @NotNull
  public PacketReliability getReliability() {
    return Objects.requireNonNull(this.reliability, "reliability");
  }

  /**
   * sets the {@link EncapsulatedPacket#reliability} to the given reliability.
   *
   * @param reliability the reliability to set.
   */
  public void setReliability(@NotNull final PacketReliability reliability) {
    this.reliability = reliability;
  }

  /**
   * obtains size of the packet.
   *
   * @return size of the packet.
   */
  public int getSize() {
    return 3 + this.getReliability().getSize() + (this.split ? 10 : 0) + this.getBuffer().readableBytes();
  }

  @Override
  public int refCnt() {
    return this.getBuffer().refCnt();
  }

  @NotNull
  @Override
  public EncapsulatedPacket retain() {
    this.getBuffer().retain();
    return this;
  }

  @NotNull
  @Override
  public EncapsulatedPacket retain(final int increment) {
    this.getBuffer().retain(increment);
    return this;
  }

  @NotNull
  @Override
  public EncapsulatedPacket touch() {
    this.getBuffer().touch();
    return this;
  }

  @NotNull
  @Override
  public EncapsulatedPacket touch(final Object hint) {
    this.getBuffer().touch(hint);
    return this;
  }

  @Override
  public boolean release() {
    return this.getBuffer().release();
  }

  @Override
  public boolean release(final int decrement) {
    return this.getBuffer().release(decrement);
  }

  /**
   * sets the {@link EncapsulatedPacket#priority} to the given priority.
   *
   * @param priority the priority to set.
   */
  public void setPriority(@NotNull final PacketPriority priority) {
    this.priority = priority;
  }

  /**
   * decodes the given packet.
   *
   * @param packet the packet to decode.
   */
  void decode(@NotNull final ByteBuf packet) {
    final var flags = packet.readByte();
    final var optional = PacketReliability.fromId((flags & 0b11100000) >> 5);
    if (optional.isEmpty()) {
      return;
    }
    this.reliability = optional.get();
    this.split = (flags & 0b00010000) != 0;
    final var size = packet.readUnsignedShort() + 7 >> 3;
    if (this.reliability.isReliable()) {
      this.reliabilityIndex = packet.readUnsignedMediumLE();
    }
    if (this.reliability.isSequenced()) {
      this.sequenceIndex = packet.readUnsignedMediumLE();
    }
    if (this.reliability.isOrdered() ||
      this.reliability.isSequenced()) {
      this.orderingIndex = packet.readUnsignedMediumLE();
      this.orderingChannel = packet.readUnsignedByte();
    }
    if (this.split) {
      this.partCount = packet.readInt();
      this.partId = packet.readUnsignedShort();
      this.partIndex = packet.readInt();
    }
    this.buffer = packet.readSlice(size);
  }

  /**
   * encodes the given packet.
   *
   * @param packet the packet to encode.
   */
  void encode(@NotNull final ByteBuf packet) {
    var flags = Objects.requireNonNull(this.reliability, "reliability").ordinal() << 5;
    if (this.split) {
      flags |= 0b00010000;
    }
    packet.writeByte(flags);
    packet.writeShort(Objects.requireNonNull(this.buffer, "buffer").readableBytes() << 3);
    if (this.reliability.isReliable()) {
      packet.writeMediumLE(this.reliabilityIndex);
    }
    if (this.reliability.isSequenced()) {
      packet.writeMediumLE(this.sequenceIndex);
    }
    if (this.reliability.isOrdered() || this.reliability.isSequenced()) {
      packet.writeMediumLE(this.orderingIndex);
      packet.writeByte(this.orderingChannel);
    }
    if (this.split) {
      packet.writeInt(this.partCount);
      packet.writeShort(this.partId);
      packet.writeInt(this.partIndex);
    }
    packet.writeBytes(this.buffer, this.buffer.readerIndex(), this.buffer.readableBytes());
  }

  /**
   * creates a packet from the given reassembled buffer
   *
   * @param reassembled the reassembled to create.
   *
   * @return an encapsulated packet.
   */
  @NotNull
  EncapsulatedPacket fromSplit(@NotNull final ByteBuf reassembled) {
    final var packet = new EncapsulatedPacket();
    packet.reliability = this.reliability;
    packet.reliabilityIndex = this.reliabilityIndex;
    packet.sequenceIndex = this.sequenceIndex;
    packet.orderingIndex = this.orderingIndex;
    packet.orderingChannel = this.orderingChannel;
    packet.buffer = reassembled;
    return packet;
  }
}
