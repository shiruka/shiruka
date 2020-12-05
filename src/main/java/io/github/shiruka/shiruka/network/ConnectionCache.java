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

package io.github.shiruka.shiruka.network;

import io.github.shiruka.shiruka.network.misc.*;
import io.github.shiruka.shiruka.network.util.Constants;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains the connection's cache values.
 */
public final class ConnectionCache {

  /**
   * the connection instance.
   */
  @NotNull
  private final Connection<?, ?> connection;

  /**
   * incoming ACK packets.
   */
  @Nullable
  private Queue<IntRange> incomingACKs;

  /**
   * incoming NACK packets.
   */
  @Nullable
  private Queue<IntRange> incomingNACKs;

  /**
   * the order read index.
   */
  private int[] orderReadIndex;

  /**
   * the order write index.
   */
  @Nullable
  private AtomicIntegerArray orderWriteIndex;

  /**
   * the ordering heaps.
   */
  @Nullable
  private FastBinaryMinHeap<EncapsulatedPacket>[] orderingHeaps;

  /**
   * the ordering lock.
   */
  @Nullable
  private Lock orderingLock;

  /**
   * outgoing ACK packets.
   */
  @Nullable
  private Queue<IntRange> outgoingACKs;

  /**
   * the outgoing lock.
   */
  @Nullable
  private Lock outgoingLock;

  /**
   * outgoing NACK packets.
   */
  @Nullable
  private Queue<IntRange> outgoingNACKs;

  /**
   * the outgoing packet next weights.
   */
  private long[] outgoingPacketNextWeights;

  /**
   * the outgoing packets.
   */
  @Nullable
  private FastBinaryMinHeap<EncapsulatedPacket> outgoingPackets;

  /**
   * the reliability read lock.
   */
  @Nullable
  private Lock reliabilityReadLock;

  /**
   * the reliable datagram queue.
   */
  @Nullable
  private BitQueue reliableDatagramQueue;

  /**
   * the sent datagram packets.
   */
  @Nullable
  private ConcurrentMap<Integer, NetDatagramPacket> sentDatagrams;

  /**
   * ACK and NACK processor.
   */
  @Nullable
  private NetSlidingWindow slidingWindow;

  /**
   * the split packets.
   */
  @Nullable
  private RoundRobinArray<SplitPacketHelper> splitPackets;

  /**
   * ctor.
   *
   * @param connection the connection instance.
   */
  ConnectionCache(@NotNull final Connection<?, ?> connection) {
    this.connection = connection;
  }

  /**
   * obtains order read index.
   *
   * @param index the index to get.
   *
   * @return the order read index.
   */
  public int getOrderReadIndex(final int index) {
    return this.orderReadIndex[index];
  }

  /**
   * obtains ordering heap.
   *
   * @param index the index to get
   *
   * @return a binary heap {@link FastBinaryMinHeap}.
   */
  @NotNull
  public FastBinaryMinHeap<EncapsulatedPacket> getOrderingHeap(final int index) {
    return Objects.requireNonNull(this.orderingHeaps[index], "ordering heaps");
  }

  /**
   * the outgoing ACK packets.
   *
   * @return the outgoing ACKs.
   */
  @NotNull
  public Queue<IntRange> getOutgoingACKs() {
    return Objects.requireNonNull(this.outgoingACKs, "outgoing ACKs");
  }

  /**
   * the outgoing NACK packets.
   *
   * @return the outgoing NACKs.
   */
  @NotNull
  public Queue<IntRange> getOutgoingNACKs() {
    return Objects.requireNonNull(this.outgoingNACKs, "outgoing NACKs");
  }

  /**
   * obtains the reliable datagram queue.
   *
   * @return the reliable datagram queue.
   */
  @NotNull
  public BitQueue getReliableDatagramQueue() {
    return Objects.requireNonNull(this.reliableDatagramQueue, "reliable datagram queue");
  }

  /**
   * obtains the sliding window.
   *
   * @return the sliding window {@link NetSlidingWindow}.
   */
  @NotNull
  public NetSlidingWindow getSlidingWindow() {
    return Objects.requireNonNull(this.slidingWindow, "sliding window");
  }

  /**
   * obtains the split packets.
   *
   * @return the split packets.
   */
  @NotNull
  public RoundRobinArray<SplitPacketHelper> getSplitPackets() {
    return Objects.requireNonNull(this.splitPackets, "split packets");
  }

  /**
   * increase the order read index with the given index
   *
   * @param index the index to increase.
   */
  public void increaseOrderReadIndex(final int index) {
    this.orderReadIndex[index]++;
  }

  /**
   * locks the ordering lock.
   */
  public void lockOrderingLock() {
    Objects.requireNonNull(this.orderingLock, "ordering lock").lock();
  }

  /**
   * locks the reliability read lock.
   */
  public void lockReliabilityReadLock() {
    Objects.requireNonNull(this.reliabilityReadLock, "reliability read lock").lock();
  }

  /**
   * offers the given int range instance.
   *
   * @param intRange the int range to offer.
   */
  public void offerIncomingACKs(@NotNull final IntRange intRange) {
    Objects.requireNonNull(this.incomingACKs, "incoming ACKs").offer(intRange);
  }

  /**
   * offers the given int range instance.
   *
   * @param intRange the int range to offer.
   */
  public void offerIncomingNACKs(@NotNull final IntRange intRange) {
    Objects.requireNonNull(this.incomingNACKs, "incoming NACKs").offer(intRange);
  }

  /**
   * unlocks the ordering lock.
   */
  public void unlockOrderingLock() {
    Objects.requireNonNull(this.orderingLock, "ordering lock").unlock();
  }

  /**
   * unlocks the reliability read lock.
   */
  public void unlockReliabilityReadLock() {
    Objects.requireNonNull(this.reliabilityReadLock, "reliability read lock").unlock();
  }

  /**
   * the incoming ACK packets.
   *
   * @return the incoming ACKs.
   */
  @NotNull
  Queue<IntRange> getIncomingACKs() {
    return Objects.requireNonNull(this.incomingACKs, "incoming ACKs");
  }

  /**
   * the incoming NACK packets.
   *
   * @return the incoming NACKs.
   */
  @NotNull
  Queue<IntRange> getIncomingNACKs() {
    return Objects.requireNonNull(this.incomingNACKs, "incoming NACKs");
  }

  /**
   * obtains the order write index.
   *
   * @return the order write index as {@link AtomicIntegerArray}.
   */
  @NotNull
  AtomicIntegerArray getOrderWriteIndex() {
    return Objects.requireNonNull(this.orderWriteIndex, "order write index");
  }

  /**
   * obtains the outgoing packet next weight from the given priority.
   *
   * @param priority the priority to get.
   *
   * @return the next weight of the outgoing packet.
   */
  long getOutgoingPacketNextWeight(final int priority) {
    return this.outgoingPacketNextWeights[priority];
  }

  /**
   * obtains the outgoing packets.
   *
   * @return the outgoing packets.
   */
  @NotNull
  FastBinaryMinHeap<EncapsulatedPacket> getOutgoingPackets() {
    return Objects.requireNonNull(this.outgoingPackets, "outgoing packets");
  }

  /**
   * obtains the sent datagram packets..
   *
   * @return the sent datagrams.
   */
  @NotNull
  ConcurrentMap<Integer, NetDatagramPacket> getSentDatagrams() {
    return Objects.requireNonNull(this.sentDatagrams, "sent datagrams");
  }

  /**
   * initiates heap weights.
   */
  void initHeapWeights() {
    for (int priority = 0; priority < 4; priority++) {
      this.outgoingPacketNextWeights[priority] = (1L << priority) * priority + priority;
    }
  }

  /**
   * initializes caches.
   */
  void initialize() {
    if (this.connection.getState() != ConnectionState.INITIALIZING) {
      throw new IllegalStateException("Connection's state must be initializing!");
    }
    this.slidingWindow = new NetSlidingWindow(this.connection.getMtu());
    this.reliableDatagramQueue = new BitQueue(512);
    this.reliabilityReadLock = new ReentrantLock(true);
    this.orderReadIndex = new int[Constants.MAXIMUM_ORDERING_CHANNELS];
    this.orderWriteIndex = new AtomicIntegerArray(Constants.MAXIMUM_ORDERING_CHANNELS);
    //noinspection unchecked
    this.orderingHeaps = new FastBinaryMinHeap[Constants.MAXIMUM_ORDERING_CHANNELS];
    this.orderingLock = new ReentrantLock(true);
    this.splitPackets = new RoundRobinArray<>(256);
    this.sentDatagrams = new ConcurrentSkipListMap<>();
    for (int i = 0; i < Constants.MAXIMUM_ORDERING_CHANNELS; i++) {
      this.orderingHeaps[i] = new FastBinaryMinHeap<>(64);
    }
    this.outgoingLock = new ReentrantLock(true);
    this.outgoingPackets = new FastBinaryMinHeap<>(8);
    this.incomingACKs = PlatformDependent.newMpscQueue();
    this.incomingNACKs = PlatformDependent.newMpscQueue();
    this.outgoingACKs = PlatformDependent.newMpscQueue();
    this.outgoingNACKs = PlatformDependent.newMpscQueue();
    this.outgoingPacketNextWeights = new long[4];
    this.initHeapWeights();
  }

  /**
   * inserts the given packet into the outgoing packets.
   *
   * @param weight the weight to insert.
   * @param packet the packet to insert.
   */
  void insertOutgoingPackets(final long weight, @NotNull final EncapsulatedPacket packet) {
    this.getOutgoingPackets().insert(weight, packet);
  }

  /**
   * insert series the given packets into the outgoing packets.
   *
   * @param weight the weight to insert.
   * @param packets the packets to insert.
   */
  void insertSeriesOutgoingPackets(final long weight, @NotNull final EncapsulatedPacket[] packets) {
    this.getOutgoingPackets().insertSeries(weight, packets);
  }

  /**
   * locks the {@link ConnectionCache#outgoingLock}.
   */
  void lockOutgoingLock() {
    Objects.requireNonNull(this.outgoingLock, "outgoing lock").lock();
  }

  /**
   * puts the sent datagram packet to {@link ConnectionCache#sentDatagrams}.
   *
   * @param oldIndex the old index to put.
   * @param packet the packet to put.
   */
  void putSentDatagrams(final int oldIndex, @NotNull final NetDatagramPacket packet) {
    this.getSentDatagrams().put(oldIndex, packet);
  }

  /**
   * removes the sent datagram packet from {@link ConnectionCache#sentDatagrams}.
   *
   * @param oldIndex the old index to remove
   * @param packet the packet to remove.
   */
  void removeSentDatagrams(final int oldIndex, @NotNull final NetDatagramPacket packet) {
    this.getSentDatagrams().remove(oldIndex, packet);
  }

  /**
   * removes the sent datagram packet from {@link ConnectionCache#sentDatagrams}.
   *
   * @param oldIndex the old index to remove.
   *
   * @return the removed value.
   */
  @Nullable
  NetDatagramPacket removeSentDatagrams(final int oldIndex) {
    return this.getSentDatagrams().remove(oldIndex);
  }

  /**
   * resets caches.
   */
  void reset() {
    Optional.ofNullable(this.splitPackets).ifPresent(split ->
      split.forEach(ReferenceCountUtil::release));
    Optional.ofNullable(this.sentDatagrams).ifPresent(sent ->
      sent.values().forEach(ReferenceCountUtil::release));
    Optional.ofNullable(this.orderingLock).ifPresent(lock -> {
      lock.lock();
      try {
        final var heaps = this.orderingHeaps;
        this.orderingHeaps = null;
        if (heaps != null) {
          for (final var orderingHeap : heaps) {
            EncapsulatedPacket packet;
            while ((packet = orderingHeap.poll()) != null) {
              packet.release();
            }
          }
        }
      } finally {
        lock.unlock();
      }
    });
    Optional.ofNullable(this.outgoingLock).ifPresent(lock -> {
      lock.lock();
      try {
        final var packets = this.outgoingPackets;
        this.outgoingPackets = null;
        if (packets != null) {
          EncapsulatedPacket packet;
          while ((packet = packets.poll()) != null) {
            packet.release();
          }
        }
        this.initHeapWeights();
      } finally {
        lock.unlock();
      }
    });
  }

  /**
   * sets the outgoing packet nex weights to the given value.
   *
   * @param priority the priority to set.
   * @param value the value tos et.
   */
  void setOutgoingPacketNextWeights(final int priority, final long value) {
    this.outgoingPacketNextWeights[priority] = value;
  }

  /**
   * unlocks the {@link ConnectionCache#outgoingLock}.
   */
  void unlockOutgoingLock() {
    Objects.requireNonNull(this.outgoingLock, "outgoing lock").unlock();
  }
}
