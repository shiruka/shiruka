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
 * an implementation for {@link Deflated}
 */
public final class JavaDeflater implements Deflated {

  /**
   * the chunk bytes.
   */
  private final byte[] chunkBytes = new byte[Zlib.CHUNK_BYTES];

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
  JavaDeflater(final int level, final boolean nowrap) {
    this.deflater = new Deflater(level, nowrap);
  }

  @Override
  public int deflate(@NotNull final ByteBuffer output) {
    this.deflater.finish();
    if (output.hasArray()) {
      return this.deflater.deflate(output.array(), output.arrayOffset() + output.position(), output.remaining());
    }
    final var startPos = output.position();
    while (output.remaining() > 0 && !this.deflater.finished()) {
      final var length = Math.min(output.remaining(), Zlib.CHUNK_BYTES);
      final var result = this.deflater.deflate(this.chunkBytes, 0, length);
      output.put(this.chunkBytes, 0, result);
    }
    return output.position() - startPos;
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
    if (input.hasArray()) {
      this.deflater.setInput(input.array(), input.arrayOffset() + input.position(), input.remaining());
      return;
    }
    final var bytes = new byte[input.remaining()];
    input.get(bytes);
    this.deflater.setInput(bytes);
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
