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

/**
 * a class that contains minimum and maximum integers.
 */
public final class IntRange {

  /**
   * the minimum number a.k.a. start.
   */
  private final int minimum;

  /**
   * the maximum number a.k.a. end.
   */
  private int maximum;

  /**
   * ctor.
   *
   * @param minimum the minimum point.
   * @param maximum the maximum point.
   */
  public IntRange(final int minimum, final int maximum) {
    if (minimum > maximum) {
      throw new IllegalStateException("The maximum number must bigger or equal to the minimum number!");
    }
    this.minimum = minimum;
    this.maximum = maximum;
  }

  /**
   * ctor.
   *
   * @param number the number to set minimum and maximum points.
   */
  public IntRange(final int number) {
    this(number, number);
  }

  /**
   * obtains the maximum number.
   *
   * @return the maximum a.k.a. end.
   */
  public int getMaximum() {
    return this.maximum;
  }

  /**
   * sets the maximum
   *
   * @param maximum the maximum to set.
   */
  public void setMaximum(final int maximum) {
    this.maximum = maximum;
  }

  /**
   * obtains the minimum number.
   *
   * @return the minimum a.k.a. start.
   */
  public int getMinimum() {
    return this.minimum;
  }
}
