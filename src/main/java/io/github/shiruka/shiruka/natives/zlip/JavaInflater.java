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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Inflated}.
 */
public final class JavaInflater implements Inflated {

  /**
   * the chunk bytes.
   */
  private final byte[] chunkBytes = new byte[Zlib.CHUNK_BYTES];

  /**
   * the original inflater.
   */
  @NotNull
  private final Inflater inflater;

  /**
   * ctor.
   *
   * @param nowrap the no wrap.
   */
  JavaInflater(final boolean nowrap) {
    this.inflater = new Inflater(nowrap);
  }

  @Override
  public boolean finished() {
    return this.inflater.finished();
  }

  @Override
  public int getAdler() {
    return this.inflater.getAdler();
  }

  @Override
  public long getBytesRead() {
    return this.inflater.getBytesRead();
  }

  @Override
  public int inflate(@NotNull final ByteBuffer output) throws DataFormatException {
    if (output.hasArray()) {
      return this.inflater.inflate(output.array(), output.arrayOffset() + output.position(), output.remaining());
    }
    final var startPos = output.position();
    while (output.remaining() > 0 && !this.inflater.finished()) {
      final var length = Math.min(output.remaining(), Zlib.CHUNK_BYTES);
      final var result = this.inflater.inflate(this.chunkBytes, 0, length);
      output.put(this.chunkBytes, 0, result);
    }
    return output.position() - startPos;
  }

  @Override
  public void reset() {
    this.inflater.reset();
  }

  @Override
  public void setInput(final ByteBuffer input) {
    if (input.hasArray()) {
      this.inflater.setInput(input.array(), input.arrayOffset() + input.position(), input.remaining());
      return;
    }
    final var bytes = new byte[input.remaining()];
    input.get(bytes);
    this.inflater.setInput(bytes);
  }

  @Override
  public void free() {
    this.inflater.end();
  }
}
