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

package io.github.shiruka.shiruka.nbt.primitive;

import io.github.shiruka.shiruka.nbt.NumberTag;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link NumberTag}.
 */
public abstract class NumberTagEnvelope implements NumberTag {

  /**
   * the number.
   */
  @NotNull
  private final Number number;

  /**
   * ctor.
   *
   * @param original the original.
   */
  NumberTagEnvelope(@NotNull final Number original) {
    this.number = original;
  }

  @NotNull
  @Override
  public final Double doubleValue() {
    return this.number.doubleValue();
  }

  @NotNull
  @Override
  public final Float floatValue() {
    return this.number.floatValue();
  }

  @NotNull
  @Override
  public final Integer intValue() {
    return this.number.intValue();
  }

  @NotNull
  @Override
  public final Long longValue() {
    return this.number.longValue();
  }
}
