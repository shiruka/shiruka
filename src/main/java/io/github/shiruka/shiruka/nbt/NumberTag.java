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
 * an interface to determine numbers.
 */
public interface NumberTag extends PrimitiveTag<Double> {

  @Override
  default boolean isNumber() {
    return true;
  }

  @Override
  @NotNull
  default Double value() {
    return this.doubleValue();
  }

  /**
   * returns the value of the specified number as a {@code byte}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code byte}.
   */
  @NotNull
  default Byte byteValue() {
    return this.intValue()
      .byteValue();
  }

  /**
   * returns the value of the specified number as a {@code short}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code short}.
   */
  @NotNull
  default Short shortValue() {
    return this.intValue()
      .shortValue();
  }

  /**
   * returns the value of the specified number as an {@code int}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code int}.
   */
  @NotNull
  Integer intValue();

  /**
   * returns the value of the specified number as a {@code long}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code long}.
   */
  @NotNull
  Long longValue();

  /**
   * returns the value of the specified number as a {@code float}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code float}.
   */
  @NotNull
  Float floatValue();

  /**
   * returns the value of the specified number as a {@code double}.
   *
   * @return the numeric value represented by this object after conversion
   *   to type {@code double}.
   */
  @NotNull
  Double doubleValue();
}
