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

package io.github.shiruka.shiruka.nbt.primitive;

import io.github.shiruka.shiruka.nbt.PrimitiveTag;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link PrimitiveTag}.
 */
public final class StringTag implements PrimitiveTag<String> {

  /**
   * the original.
   */
  @NotNull
  private final String original;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public StringTag(@NotNull final String original) {
    this.original = original;
  }

  @NotNull
  @Override
  public StringTag asString() {
    return this;
  }

  @Override
  public byte id() {
    return 8;
  }

  @Override
  public boolean isString() {
    return true;
  }

  @Override
  public String toString() {
    return this.original;
  }

  @NotNull
  @Override
  public String value() {
    return this.original;
  }
}
