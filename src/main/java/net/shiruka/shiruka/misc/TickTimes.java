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

package net.shiruka.shiruka.misc;

import java.util.Arrays;
import lombok.Getter;

/**
 * a class that represents calculator for tick times.
 */
public final class TickTimes {

  /**
   * the tick times.
   */
  @Getter
  private final long[] times;

  /**
   * ctor.
   *
   * @param length the length.
   */
  public TickTimes(final int length) {
    this.times = new long[length];
  }

  /**
   * set the given time to the given index.
   *
   * @param index the index to set.
   * @param time the time to set.
   */
  public void add(final int index, final long time) {
    this.times[index % this.times.length] = time;
  }

  /**
   * calculates and gives the average tick time.
   *
   * @return average tick time.
   */
  public double getAverage() {
    return (double) Arrays.stream(this.times).sum() / (double) this.times.length * 1.0E-6D;
  }
}
