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

package net.shiruka.shiruka.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents rolling average.
 */
public final class RollingAverage {

  /**
   * the seconds in a nano.
   */
  private static final long SEC_IN_NANO = 1000000000;

  /**
   * the samples.
   */
  @NotNull
  private final BigDecimal[] samples;

  /**
   * the size.
   */
  private final int size;

  /**
   * the times.
   */
  private final long[] times;

  /**
   * the index.
   */
  private int index = 0;

  /**
   * the time.
   */
  private long time;

  /**
   * the total.
   */
  @NotNull
  private BigDecimal total;

  /**
   * ctor.
   *
   * @param size the ize.
   */
  public RollingAverage(final int size) {
    this.size = size;
    this.time = size * RollingAverage.SEC_IN_NANO;
    this.total = RollingAverage.toDecimal(ShirukaTick.TPS)
      .multiply(RollingAverage.toDecimal(RollingAverage.SEC_IN_NANO))
      .multiply(RollingAverage.toDecimal(size));
    this.samples = new BigDecimal[size];
    this.times = new long[size];
    for (var i = 0; i < size; i++) {
      this.samples[i] = RollingAverage.toDecimal(ShirukaTick.TPS);
      this.times[i] = RollingAverage.SEC_IN_NANO;
    }
  }

  /**
   * converts the given {@code time} to {@link BigDecimal} instance.
   *
   * @param time the time to convert.
   *
   * @return converted big decimal instance.
   */
  @NotNull
  private static BigDecimal toDecimal(final long time) {
    return new BigDecimal(time);
  }

  /**
   * adds the given {@code time} to the given {@code decimal}.
   *
   * @param decimal the decimal to add.
   * @param time the time to add.
   */
  public void add(@NotNull final BigDecimal decimal, final long time) {
    this.time -= this.times[this.index];
    this.total = this.total.subtract(this.samples[this.index]
      .multiply(RollingAverage.toDecimal(this.times[this.index])));
    this.samples[this.index] = decimal;
    this.times[this.index] = time;
    this.time += time;
    this.total = this.total.add(decimal.multiply(RollingAverage.toDecimal(time)));
    if (++this.index == this.size) {
      this.index = 0;
    }
  }

  /**
   * obtains the average.
   *
   * @return average.
   */
  public double getAverage() {
    return this.total.divide(RollingAverage.toDecimal(this.time), 30, RoundingMode.HALF_UP).doubleValue();
  }
}
