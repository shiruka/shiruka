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

package net.shiruka.shiruka.nbt;

import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine numbers.
 */
public interface NumberTag extends PrimitiveTag<Double> {

  @NotNull
  @Override
  default NumberTag asNumber() {
    return this;
  }

  @Override
  default boolean isNumber() {
    return true;
  }

  /**
   * returns the value of the specified number as a {@code byte}.
   *
   * @return the numeric value represented by this object after conversion to type {@code byte}.
   */
  byte byteValue();

  /**
   * returns the value of the specified number as a {@code double}.
   *
   * @return the numeric value represented by this object after conversion to type {@code double}.
   */
  double doubleValue();

  /**
   * returns the value of the specified number as a {@code float}.
   *
   * @return the numeric value represented by this object after conversion to type {@code float}.
   */
  float floatValue();

  /**
   * returns the value of the specified number as an {@code int}.
   *
   * @return the numeric value represented by this object after conversion to type {@code int}.
   */
  int intValue();

  /**
   * returns the value of the specified number as a {@code long}.
   *
   * @return the numeric value represented by this object after conversion to type {@code long}.
   */
  long longValue();

  /**
   * returns the value of the specified number as a {@code short}.
   *
   * @return the numeric value represented by this object after conversion to type {@code short}.
   */
  short shortValue();

  @Override
  @NotNull
  default Double value() {
    return this.doubleValue();
  }
}
