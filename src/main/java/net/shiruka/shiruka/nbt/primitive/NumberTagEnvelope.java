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

package net.shiruka.shiruka.nbt.primitive;

import java.util.Objects;
import lombok.experimental.Delegate;
import net.shiruka.shiruka.nbt.NumberTag;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link NumberTag}.
 */
public abstract class NumberTagEnvelope implements NumberTag {

  /**
   * the number.
   */
  @NotNull
  @Delegate
  private final Number number;

  /**
   * ctor.
   *
   * @param original the original.
   */
  NumberTagEnvelope(@NotNull final Number original) {
    this.number = original;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.number);
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj != null &&
      this.getClass() == obj.getClass() &&
      this.number.equals(((NumberTagEnvelope) obj).number);
  }

  @Override
  public final String toString() {
    return this.number.toString();
  }
}
