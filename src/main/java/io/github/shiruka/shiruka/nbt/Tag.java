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

/**
 * an interface to determine named binary tag.
 */
public interface Tag {

  /**
   * checks if {@code this} is a {@link CompoundTag}.
   *
   * @return {@code true} if {@code this} is a {@link CompoundTag}.
   */
  boolean isCompound();

  /**
   * an instance of {@code this} as a {@link CompoundTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link CompoundTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link CompoundTag}.
   */
  default CompoundTag asCompound() {
    if (this.isCompound()) {
      return (CompoundTag) this;
    }
    throw new IllegalStateException(this.getClass() + " cannot cast as a CompoundTag");
  }
}
