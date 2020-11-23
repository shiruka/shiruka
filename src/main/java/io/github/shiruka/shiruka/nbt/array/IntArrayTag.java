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

package io.github.shiruka.shiruka.nbt.array;

import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class IntArrayTag extends ArrayTagEnvelope<Integer> {

  /**
   * ctor.
   *
   * @param original the original.
   */
  public IntArrayTag(@NotNull final Integer @NotNull [] original) {
    super(original);
  }

  @Override
  public boolean isInt() {
    return true;
  }

  @NotNull
  @Override
  public IntArrayTag asIntArray() {
    return this;
  }

  @Override
  public byte id() {
    return 11;
  }

  @Override
  public void write(@NotNull final DataOutput output) throws IOException {
    final var value = this.value();
    output.writeInt(value.length);
    for (final var integer : value) {
      output.writeInt(integer);
    }
  }
}
