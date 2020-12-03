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

package io.github.shiruka.shiruka.natives;

import io.github.shiruka.shiruka.natives.exceptions.BufferNoSpaceException;
import io.github.shiruka.shiruka.natives.exceptions.RequiredBytesToEncodeException;
import java.nio.ByteBuffer;
import javax.crypto.ShortBufferException;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine ciphers.
 */
public interface NativeCipher extends Native {

  /**
   * ciphers the given input into the given output.
   *
   * @param input the input to cipher.
   * @param output the output to cipher.
   *
   * @throws BufferNoSpaceException if the given output does not have enough space to write in it.
   * @throws RequiredBytesToEncodeException if the given input requires the specified bytes size to encode.
   */
  void cipher(@NotNull ByteBuffer input, @NotNull ByteBuffer output) throws ShortBufferException;
}
