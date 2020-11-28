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

package io.github.shiruka.shiruka.misc;

import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for arrays.
 */
public final class ArrayUtils {

  /**
   * an empty object byte array.
   */
  private static final Byte[] EMPTY_BYTE_ARRAY_OBJECT = new Byte[0];

  /**
   * an empty primitive byte array.
   */
  private static final byte[] EMPTY_BYTE_ARRAY_PRIMITIVE = new byte[0];

  /**
   * ctor.
   */
  private ArrayUtils() {
  }

  /**
   * <p>Converts an array of primitive bytes to objects.
   *
   * <p>This method returns {@code null} for a {@code null} input array.
   *
   * @param array a {@code byte} array
   *
   * @return a {@code Byte} array, {@code null} if null array input
   */
  @NotNull
  public static Byte[] toObject(final byte[] array) {
    if (array.length == 0) {
      return ArrayUtils.EMPTY_BYTE_ARRAY_OBJECT;
    }
    final var result = new Byte[array.length];
    for (var i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  /**
   * <p>Converts an array of object Bytes to primitives.
   *
   * <p>This method returns {@code null} for a {@code null} input array.
   *
   * @param array a {@code Byte} array, may be {@code null}
   *
   * @return a {@code byte} array, {@code null} if null array input
   *
   * @throws NullPointerException if array content is {@code null}
   */
  public static byte[] toPrimitive(@NotNull final Byte[] array) {
    if (array.length == 0) {
      return ArrayUtils.EMPTY_BYTE_ARRAY_PRIMITIVE;
    }
    final var result = new byte[array.length];
    for (var i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }
}
