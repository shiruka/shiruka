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

package io.github.shiruka.shiruka.nbt;

import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine array tags.
 *
 * @param <T> type of arrays.
 */
public interface ArrayTag<T> extends Tag {

  @Override
  @NotNull
  default ArrayTag<?> asArray() {
    return this;
  }

  @Override
  default boolean isArray() {
    return true;
  }

  /**
   * gets the value at {@code index} in this tag.
   *
   * @param index the index in the array.
   *
   * @return the byte at the index in the array.
   *
   * @throws IndexOutOfBoundsException if index is &lt; 0 or &ge; {@link #size()}.
   */
  @NotNull
  T get(final int index);

  /**
   * gets the size of the array.
   *
   * @return array size.
   */
  int size();

  /**
   * gets the value.
   *
   * @return the value.
   */
  @NotNull
  T @NotNull [] value();
}
