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

package io.github.shiruka.shiruka.network.objects;

import io.github.shiruka.shiruka.network.util.Misc;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an array implementation.
 *
 * @param <E> the value.
 */
public final class RoundRobinArray<E> {

  /**
   * the elements.
   */
  @NotNull
  private final AtomicReferenceArray<E> elements;

  /**
   * the mask.
   */
  private final int mask;

  /**
   * ctor.
   *
   * @param cap the fixed capacity.
   */
  public RoundRobinArray(final int cap) {
    var capacity = cap;
    capacity = Misc.powerOfTwoCeiling(cap);
    this.elements = new AtomicReferenceArray<>(capacity);
    this.mask = cap - 1;
  }

  /**
   * runs the consumer for each element in the array.
   *
   * @param consumer the consumer to run.
   */
  public void forEach(@NotNull final Consumer<E> consumer) {
    for (int i = 0, len = this.mask + 1; i < len; i++) {
      consumer.accept(this.elements.get(i));
    }
  }

  /**
   * get the element from the index.
   *
   * @param index the index to get.
   *
   * @return an element {@link E}.
   */
  @Nullable
  public E get(final int index) {
    return this.elements.get(index & this.mask);
  }

  /**
   * removes the element with the given index.
   *
   * @param index the index to remove.
   * @param expected the expected to remove.
   *
   * @return returns true if the expected element removed successfully.
   */
  public boolean remove(final int index, @NotNull final E expected) {
    return this.elements.compareAndSet(index & this.mask, expected, null);
  }

  /**
   * sets the element with the given index.
   *
   * @param index the index to set.
   * @param value the value to set.
   */
  public void set(final int index, @NotNull final E value) {
    ReferenceCountUtil.release(this.elements.getAndSet(index & this.mask, value));
  }
}
