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

import io.github.shiruka.shiruka.natives.Native;
import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine wrapper for {@link java.util.zip.Deflater}.
 */
public interface Deflated extends Native {

  /**
   * deflates the given output.
   *
   * @param output the output to deflate.
   *
   * @return the actual number of bytes of compressed data written to the
   *   output buffer.
   */
  int deflate(@NotNull ByteBuffer output);

  /**
   * end of the compressed data output stream has been reached.
   *
   * @return {@code true} if the end of the compressed data output stream has
   *   been reached.
   */
  boolean finished();

  /**
   * ADLER-32 value of the uncompressed data.
   *
   * @return the ADLER-32 value of the uncompressed data.
   */
  int getAdler();

  /**
   * resets deflater so that a new set of input data can be processed.
   * <p>
   * keeps current compression level and strategy settings.
   */
  void reset();

  /**
   * sets the input.
   *
   * @param input the input to set.
   */
  void setInput(@NotNull ByteBuffer input);

  /**
   * sets the level.
   *
   * @param level the level set.
   */
  void setLevel(int level);
}
