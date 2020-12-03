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

import java.nio.ByteBuffer;
import java.security.DigestException;

/**
 * an interface to determine .
 */
public interface NativeDigest extends Native {

  /**
   * ...
   *
   * @return ...
   */
  byte[] digest();

  /**
   * ...
   *
   * @param input ...
   * @param offset ...
   * @param len ...
   *
   * @return ...
   *
   * @throws DigestException ...
   */
  default int digest(final byte[] input, final int offset, final int len) throws DigestException {
    if (input == null) {
      throw new IllegalArgumentException("No output buffer given");
    }
    if (input.length - offset < len) {
      throw new IllegalArgumentException("Output buffer too small for specified offset and length");
    }
    final byte[] digest = this.digest();
    if (len < digest.length) {
      throw new DigestException("partial digests not returned");
    }
    if (input.length - offset < digest.length) {
      throw new DigestException("insufficient space in the output " + "buffer to store the digest");
    }
    System.arraycopy(digest, 0, input, offset, digest.length);
    return digest.length;
  }

  default void digest(final ByteBuffer buffer) {
    buffer.put(this.digest());
  }

  void reset();

  void update(byte[] input, int offset, int len);

  void update(ByteBuffer buffer);

  default void update(final byte[] input) {
    this.update(input, 0, input.length);
  }
}
