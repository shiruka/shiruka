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

import io.github.shiruka.shiruka.network.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains encapsulated packets to encode and decode them.
 */
public final class NetDatagramPacket extends AbstractReferenceCounted {

  /**
   * list of the encapsulated packets that added before.
   */
  private final List<EncapsulatedPacket> packets = new ArrayList<>();

  /**
   * the send time of the packet.
   */
  private final long time;

  /**
   * the rak net flag.
   */
  private short flags = Constants.FLAG_VALID;

  /**
   * the next send time.
   */
  private long nextSend;

  /**
   * the sequence index.
   */
  private int sequenceIndex = -1;

  /**
   * ctor.
   *
   * @param time the send time.
   */
  public NetDatagramPacket(final long time) {
    this.time = time;
  }

  /**
   * decodes the gives packet.
   *
   * @param packet the packet to decode.
   */
  public void decode(@NotNull final ByteBuf packet) {
    this.flags = packet.readByte();
    this.sequenceIndex = packet.readUnsignedMediumLE();
    while (packet.isReadable()) {
      final var encapsulatedPacket = new EncapsulatedPacket();
      encapsulatedPacket.decode(packet);
      this.packets.add(encapsulatedPacket);
    }
  }

  /**
   * encodes all encapsulated packets.
   *
   * @param packet the packet to encode.
   */
  public void encode(@NotNull final ByteBuf packet) {
    packet.writeByte(this.flags);
    packet.writeMediumLE(this.sequenceIndex);
    this.packets.forEach(encapsulatedPacket -> encapsulatedPacket.encode(packet));
  }

  /**
   * obtains the next send time.
   *
   * @return the next send time.
   */
  public long getNextSend() {
    return this.nextSend;
  }

  /**
   * sets the next send time.
   *
   * @param nextSend the next send to set.
   */
  public void setNextSend(final long nextSend) {
    this.nextSend = nextSend;
  }

  /**
   * obtains the encapsulated packet list.
   *
   * @return the packet list as {@link EncapsulatedPacket}.
   */
  @NotNull
  public List<EncapsulatedPacket> getPackets() {
    return Collections.unmodifiableList(this.packets);
  }

  /**
   * obtains the sequence index of the packet.
   *
   * @return the sequence index number.
   */
  public int getSequenceIndex() {
    return this.sequenceIndex;
  }

  /**
   * sets the sequence index.
   *
   * @param sequenceIndex the sequence index to set.
   */
  public void setSequenceIndex(final int sequenceIndex) {
    this.sequenceIndex = sequenceIndex;
  }

  /**
   * obtains size of the packets.
   *
   * @return the size of the packets.
   */
  public int getSize() {
    return Constants.DATAGRAM_HEADER_SIZE +
      this.packets.stream()
        .mapToInt(EncapsulatedPacket::getSize)
        .sum();
  }

  /**
   * obtains the send time.
   *
   * @return the send time of the packet.
   */
  public long getTime() {
    return this.time;
  }

  @NotNull
  @Override
  public NetDatagramPacket retain() {
    super.retain();
    return this;
  }

  @Override
  public NetDatagramPacket retain(final int increment) {
    super.retain(increment);
    return this;
  }

  @Override
  protected void deallocate() {
    this.packets.forEach(ReferenceCounted::release);
  }

  @Override
  public NetDatagramPacket touch(final Object hint) {
    this.packets.forEach(packet -> packet.touch(hint));
    return this;
  }

  /**
   * tries to add the given packet into the {@link NetDatagramPacket#packets}.
   *
   * @param packet the packet to add.
   * @param mtu the mtu.
   *
   * @return return true if the adding packet is succeed.
   */
  public boolean tryAddPacket(@NotNull final EncapsulatedPacket packet, final int mtu) {
    if (this.getSize() + packet.getSize() > mtu - Constants.DATAGRAM_HEADER_SIZE) {
      return false;
    }
    this.packets.add(packet);
    if (packet.split) {
      this.flags |= Constants.FLAG_CONTINUOUS_SEND;
    }
    return true;
  }
}
