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
import java.util.zip.DataFormatException;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine wrapper for {@link java.util.zip.Inflater}.
 */
public interface Inflated extends Native {

  /**
   * if the end of the compressed data stream has been reached.
   *
   * @return {@code true} if the end of the compressed data stream has been reached.
   */
  boolean finished();

  /**
   * ADLER-32 value of the uncompressed data.
   *
   * @return the ADLER-32 value of the uncompressed data.
   */
  int getAdler();

  /**
   * total number of compressed bytes input so far.
   *
   * @return the total (non-negative) number of compressed bytes input so far.
   */
  long getBytesRead();

  /**
   * inflates the given output.
   *
   * @param output the output to inflate.
   *
   * @return the actual number of uncompressed bytes.
   */
  int inflate(@NotNull ByteBuffer output) throws DataFormatException;

  /**
   * resets inflater so that a new set of input data can be processed.
   */
  void reset();

  /**
   * sets the input.
   *
   * @param input the input to set.
   */
  void setInput(@NotNull ByteBuffer input);
}
