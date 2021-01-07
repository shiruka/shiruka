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

package io.github.shiruka.shiruka.nbt.array;

import io.github.shiruka.shiruka.nbt.ArrayTag;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents int arrays.
 */
public final class IntArrayTag implements ArrayTag<Integer> {

  /**
   * the original.
   */
  @NotNull
  private final Integer @NotNull [] original;

  /**
   * the primitive original.
   */
  private final int @NotNull [] primitiveOriginal;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public IntArrayTag(final int... original) {
    this.primitiveOriginal = original.clone();
    this.original = Arrays.stream(original)
      .boxed()
      .toArray(Integer[]::new);
  }

  @NotNull
  @Override
  public IntArrayTag asIntArray() {
    return this;
  }

  @Override
  public byte id() {
    return 11;
  }

  @Override
  public boolean isIntArray() {
    return true;
  }

  @NotNull
  @Override
  public Integer get(final int index) {
    ArrayTag.checkIndex(index, this.original.length);
    return this.original[index];
  }

  @Override
  public int size() {
    return this.original.length;
  }

  /**
   * obtains the primitive original value.
   *
   * @return primitive value.
   */
  public int @NotNull [] primitiveValue() {
    return this.primitiveOriginal.clone();
  }

  @Override
  public String toString() {
    return Arrays.toString(this.original);
  }

  @NotNull
  @Override
  public Integer @NotNull [] value() {
    return this.original.clone();
  }
}
