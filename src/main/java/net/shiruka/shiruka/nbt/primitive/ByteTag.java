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

import net.shiruka.shiruka.nbt.TagTypes;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link NumberTagEnvelope}.
 */
public final class ByteTag extends NumberTagEnvelope {

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ByteTag(final byte original) {
    super(original);
  }

  @NotNull
  @Override
  public ByteTag asByte() {
    return this;
  }

  @NotNull
  @Override
  public TagTypes getType() {
    return TagTypes.BYTE;
  }

  @Override
  public boolean isByte() {
    return true;
  }
}
