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

package io.github.shiruka.shiruka.natives.zlip;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Deflated}.
 */
public final class Java11Deflater implements Deflated {

  /**
   * the original deflater.
   */
  @NotNull
  private final Deflater deflater;

  /**
   * ctor.
   *
   * @param level the level.
   * @param nowrap the no wrap.
   */
  Java11Deflater(final int level, final boolean nowrap) {
    this.deflater = new Deflater(level, nowrap);
  }

  @Override
  public int deflate(@NotNull final ByteBuffer output) {
    this.deflater.finish();
    return this.deflater.deflate(output);
  }

  @Override
  public boolean finished() {
    return this.deflater.finished();
  }

  @Override
  public int getAdler() {
    return this.deflater.getAdler();
  }

  @Override
  public void reset() {
    this.deflater.reset();
  }

  @Override
  public void setInput(@NotNull final ByteBuffer input) {
    this.deflater.setInput(input);
  }

  @Override
  public void setLevel(final int level) {
    this.deflater.setLevel(level);
  }

  @Override
  public void free() {
    this.deflater.end();
  }
}
