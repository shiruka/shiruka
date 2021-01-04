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

package io.github.shiruka.shiruka.network.misc;

import com.google.common.base.Preconditions;
import io.github.shiruka.shiruka.network.util.Misc;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that allows you to manage binaries as fast as possible.
 *
 * @param <E> the value in the binary
 */
public final class FastBinaryMinHeap<E> {

  /**
   * the heap
   */
  @Nullable
  private Object[] heap;

  /**
   * the size
   */
  private int size;

  /**
   * the weights.
   */
  @NotNull
  private long[] weights;

  /**
   * ctor.
   */
  public FastBinaryMinHeap() {
    this(8);
  }

  /**
   * ctor.
   *
   * @param initialCapacity the initial capacity.
   */
  public FastBinaryMinHeap(int initialCapacity) {
    this.heap = new Object[++initialCapacity];
    this.weights = new long[initialCapacity];
    Arrays.fill(this.weights, Long.MAX_VALUE);
    this.weights[0] = Long.MIN_VALUE;
  }

  /**
   * inserts the given element.
   *
   * @param weight the weight to insert.
   * @param element the element to insert.
   */
  public void insert(final long weight, @NotNull final E element) {
    this.ensureCapacity(this.size + 1);
    this.insert0(weight, element);
  }

  /**
   * insert series the given elements.
   *
   * @param weight the weight to insert series.
   * @param elements the elements to insert series.
   */
  public void insertSeries(final long weight, @NotNull final E[] elements) {
    if (elements.length == 0) {
      return;
    }
    this.ensureCapacity(this.size + elements.length);
    var optimized = this.size == 0;
    if (!optimized) {
      optimized = true;
      for (int parentIdx = 0, currentIdx = this.size; parentIdx < currentIdx; parentIdx++) {
        if (weight < this.weights[parentIdx]) {
          optimized = false;
          break;
        }
      }
    }
    if (optimized) {
      Arrays.stream(elements).forEach(element -> {
        Objects.requireNonNull(element, "element");
        this.heap[++this.size] = element;
        this.weights[this.size] = weight;
      });
    } else {
      Arrays.stream(elements).forEach(element -> {
        Objects.requireNonNull(element, "element");
        this.insert0(weight, element);
      });
    }
  }

  /**
   * checks if binary empty.
   *
   * @return true if binary empty.
   */
  public boolean isEmpty() {
    return this.size == 0;
  }

  /**
   * checks if binary not empty.
   *
   * @return true if binary not empty.
   */
  public boolean isNotEmpty() {
    return !this.isEmpty();
  }

  /**
   * gets but not removes.
   *
   * @return the next object.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public E peek() {
    return (E) this.heap[1];
  }

  /**
   * gets and removes the object.
   *
   * @return the next object.
   */
  @Nullable
  public E poll() {
    if (this.size > 0) {
      final var e = this.peek();
      this.remove();
      return e;
    }
    return null;
  }

  /**
   * removes the next object.
   */
  public void remove() {
    Preconditions.checkState(this.size != 0, "Heap is empty");
    var hole = 1;
    var success = 2;
    final var sz = this.size;
    while (success < sz) {
      final var weight1 = this.weights[success];
      final var weight2 = this.weights[success + 1];
      if (weight1 > weight2) {
        this.weights[hole] = weight2;
        this.heap[hole] = this.heap[++success];
      } else {
        this.weights[hole] = weight1;
        this.heap[hole] = this.heap[success];
      }
      hole = success;
      success <<= 1;
    }
    final var bubble = this.weights[sz];
    var pred = hole >> 1;
    while (this.weights[pred] > bubble) {
      this.weights[hole] = this.weights[pred];
      this.heap[hole] = this.heap[pred];
      hole = pred;
      pred >>= 1;
    }
    this.weights[hole] = bubble;
    this.heap[hole] = this.heap[sz];
    this.heap[sz] = null;
    this.weights[sz] = Long.MAX_VALUE;
    this.size--;
    if (this.size << 2 < this.heap.length && this.size > 4) {
      this.resize(this.size << 1);
    }
  }

  /**
   * obtains size of the binary.
   *
   * @return size of the binary.
   */
  public int size() {
    return this.size;
  }

  /**
   * ensures and resizes the capacity with the given size.
   *
   * @param size the size to ensure.
   */
  private void ensureCapacity(final int size) {
    if (size + 1 >= this.heap.length) {
      this.resize(Misc.powerOfTwoCeiling(size + 1));
    }
  }

  /**
   * inserts the given element into the binary.
   *
   * @param weight the weight to insert.
   * @param element the element to insert.
   */
  private void insert0(final long weight, @NotNull final E element) {
    var hole = ++this.size;
    var pred = hole >> 1;
    var predWeight = this.weights[pred];
    while (predWeight > weight) {
      this.weights[hole] = predWeight;
      this.heap[hole] = this.heap[pred];
      hole = pred;
      pred >>= 1;
      predWeight = this.weights[pred];
    }
    this.weights[hole] = weight;
    this.heap[hole] = element;
  }

  /**
   * resizes the binary with the given capacity.
   *
   * @param capacity the capacity to resize.
   */
  private void resize(final int capacity) {
    final var adjustedSize = this.size + 1;
    final var copyLength = Math.min(this.heap.length, adjustedSize);
    final var newHeap = new Object[capacity];
    final var newWeights = new long[capacity];
    System.arraycopy(this.heap, 0, newHeap, 0, copyLength);
    System.arraycopy(this.weights, 0, newWeights, 0, copyLength);
    if (capacity > adjustedSize) {
      Arrays.fill(newWeights, adjustedSize, capacity, Long.MAX_VALUE);
    }
    this.heap = newHeap;
    this.weights = newWeights;
  }
}
