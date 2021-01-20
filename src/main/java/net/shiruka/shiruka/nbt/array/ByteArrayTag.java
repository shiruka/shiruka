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

package net.shiruka.shiruka.nbt.array;

import java.util.Arrays;
import java.util.stream.IntStream;
import net.shiruka.shiruka.nbt.ArrayTag;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents byte arrays.
 */
public final class ByteArrayTag implements ArrayTag<Byte> {

  /**
   * the original.
   */
  @NotNull
  private final Byte[] original;

  /**
   * the primitive original.
   */
  private final byte @NotNull [] primitiveOriginal;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ByteArrayTag(final byte... original) {
    this.primitiveOriginal = original.clone();
    this.original = IntStream.range(0, original.length)
      .mapToObj(i -> original[i])
      .toArray(Byte[]::new);
  }

  @NotNull
  @Override
  public ByteArrayTag asByteArray() {
    return this;
  }

  @Override
  public byte id() {
    return 7;
  }

  @Override
  public boolean isByteArray() {
    return true;
  }

  @NotNull
  @Override
  public Byte get(final int index) {
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
  public byte @NotNull [] primitiveValue() {
    return this.primitiveOriginal.clone();
  }

  @Override
  public String toString() {
    return Arrays.toString(this.original);
  }

  @NotNull
  @Override
  public Byte @NotNull [] value() {
    return this.original.clone();
  }
}
