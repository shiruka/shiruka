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

package net.shiruka.shiruka.network.objects;

import net.shiruka.shiruka.network.util.Misc;

/**
 * implementation of a queue that compresses multiple single bits (booleans) into single bytes.
 */
public final class BitQueue {

  /**
   * the head.
   */
  private int head;

  /**
   * the queue.
   */
  private byte[] queue;

  /**
   * the tail.
   */
  private int tail;

  /**
   * ctor.
   *
   * @param cap the capacity.
   */
  public BitQueue(final int cap) {
    var capacity = Misc.powerOfTwoCeiling(cap);
    if (capacity <= 0) {
      capacity = 8;
    }
    this.queue = new byte[(capacity + 7 >> 3)];
    this.head = 0;
    this.tail = 0;
  }

  /**
   * adds the specified bit to the queue.
   *
   * @param bit the bit to add to the queue.
   */
  public void add(final boolean bit) {
    if ((this.head + 1 & (this.queue.length << 3) - 1) == this.tail) {
      this.resize(this.queue.length << 4);
    }
    final var by = this.head >> 3;
    final var bi = (byte) (1 << (this.head & 7));
    this.queue[by] ^= (byte) (((bit ? 0xFF : 0x00) ^ this.queue[by]) & bi);
    this.head = this.head + 1 & (this.queue.length << 3) - 1;
  }

  /**
   * gets the value of the n-th bit contained in the queue.
   *
   * @param n The index of the bit to get the value of.
   *
   * @return The value of the n-th bit inside the queue.
   */
  public boolean get(final int n) {
    if (n >= this.size() || n < 0) {
      return false;
    }
    final var idx = this.tail + n & (this.queue.length << 3) - 1;
    final var arrIdx = idx >> 3;
    final var mask = (byte) (1 << (idx & 7));
    return (this.queue[arrIdx] & mask) != 0;
  }

  /**
   * tests whether or not the queue is currently empty.
   *
   * @return Whether or not the queue is currently empty.
   */
  public boolean isEmpty() {
    return this.head == this.tail;
  }

  /**
   * peeks at the next bit to be returned from the queue without actually removing it.
   *
   * @return The value of the element that would be returned next.
   */
  public boolean peek() {
    if (this.head == this.tail) {
      return false;
    }
    final var arrIdx = this.tail >> 3;
    final var mask = (byte) (1 << (this.tail & 7));
    return (this.queue[arrIdx] & mask) != 0;
  }

  /**
   * gets the value of the next bit to be returned from the queue and removes it entirely.
   *
   * @return The value of the next element in the queue.
   */
  public boolean poll() {
    if (this.head == this.tail) {
      return false;
    }
    final var arrIdx = this.tail >> 3;
    final var mask = (byte) (1 << (this.tail & 7));
    this.tail = this.tail + 1 & (this.queue.length << 3) - 1;
    return (this.queue[arrIdx] & mask) != 0;
  }

  /**
   * sets the n-th bit contained in the queue to the specified value.
   *
   * @param n The index of the bit to change.
   * @param bit The value to assign to the bit.
   */
  public void set(final int n, final boolean bit) {
    if (n >= this.size() || n < 0) {
      return;
    }
    final var idx = this.tail + n & (this.queue.length << 3) - 1;
    final var arrIdx = idx >> 3;
    final var mask = (byte) (1 << (idx & 7));
    this.queue[arrIdx] ^= (byte) (((bit ? 0xFF : 0x00) ^ this.queue[arrIdx]) & mask);
  }

  /**
   * returns the number of elements remaining inside the queue.
   *
   * @return The queue's size.
   */
  public int size() {
    if (this.head > this.tail) {
      return this.head - this.tail;
    }
    if (this.head < this.tail) {
      return (this.queue.length << 3) - (this.tail - this.head);
    }
    return 0;
  }

  /**
   * resizes the queue with the given capacity.
   *
   * @param capacity the capacity to resize.
   */
  private void resize(final int capacity) {
    final var newQueue = new byte[capacity + 7 >> 3];
    final var size = this.size();
    if ((this.tail & 7) == 0) {
      if (this.head > this.tail) {
        final var srcPos = this.tail >> 3;
        final var length = this.head - this.tail + 7 >> 3;
        System.arraycopy(this.queue, srcPos, newQueue, 0, length);
      } else if (this.head < this.tail) {
        int length = this.tail >> 3;
        final var adjustedPos = (this.queue.length << 3) - this.tail + 7 >> 3;
        System.arraycopy(this.queue, length, newQueue, 0, adjustedPos);
        length = this.head + 7 >> 3;
        System.arraycopy(this.queue, 0, newQueue, adjustedPos, length);
      }
    } else {
      final var tailBits = this.tail & 7;
      int tailIdx = this.tail >> 3;
      int by2 = tailIdx + 1 & this.queue.length - 1;
      int mask;
      int bit1;
      int bit2;
      int cursor = 0;
      while (cursor < size) {
        mask = (1 << tailBits) - 1 & 0xFF;
        bit1 = (this.queue[tailIdx] & ~mask & 0xFF) >>> tailBits;
        bit2 = this.queue[by2] << 8 - tailBits;
        newQueue[cursor >> 3] = (byte) (bit1 | bit2);
        cursor += 8;
        tailIdx = tailIdx + 1 & this.queue.length - 1;
        by2 = by2 + 1 & this.queue.length - 1;
      }
    }
    this.tail = 0;
    this.head = size;
    this.queue = newQueue;
  }
}
