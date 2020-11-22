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

import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine primitive tags.
 *
 * @param <T> type of the tag's value.
 */
public interface PrimitiveTag<T> extends Tag {

  @Override
  default boolean isPrimitive() {
    return true;
  }

  @NotNull
  @Override
  default PrimitiveTag<?> asPrimitive() {
    return this;
  }

  @Override
  default void write(@NotNull final DataOutput output) throws IOException {
    if (this.isString()) {
      output.writeUTF(this.asString().value());
      return;
    }
    if (!this.isNumber()) {
      return;
    }
    final var number = this.asNumber();
    if (this.isByte()) {
      output.writeByte(number.byteValue());
    } else if (this.isDouble()) {
      output.writeDouble(number.doubleValue());
    } else if (this.isFloat()) {
      output.writeFloat(number.floatValue());
    } else if (this.isInt()) {
      output.writeInt(number.intValue());
    } else if (this.isLong()) {
      output.writeLong(number.longValue());
    } else if (this.isShort()) {
      output.writeShort(number.shortValue());
    }
  }

  /**
   * obtains the tag's value.
   *
   * @return the tag's value.
   */
  @NotNull
  T value();
}
