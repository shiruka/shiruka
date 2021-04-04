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

package net.shiruka.shiruka.util;

/**
 * a class that contains utility method for strings.
 */
public final class StringUtil {

  /**
   * ctor.
   */
  private StringUtil() {
  }

  /**
   * gets byte encoding from UTF8 string.
   *
   * @param input string which should be converted to its byte representation.
   *
   * @return byte array of string contents.
   */
  public static byte[] getUTF8Bytes(final String input) {
    final var output = new byte[input.length() * 3];
    var byteCount = 0;
    var fastForward = 0;
    for (final var i = Math.min(input.length(), output.length);
         fastForward < i && input.charAt(fastForward) < 128;
         output[byteCount++] = (byte) input.charAt(fastForward++)) {
    }
    for (var i = fastForward; i < input.length(); i++) {
      final var c = input.charAt(i);
      if (c < 128) {
        output[byteCount++] = (byte) c;
      } else if (c < 2048) {
        output[byteCount++] = (byte) (192 | c >> 6);
        output[byteCount++] = (byte) (128 | c & 63);
      } else if (Character.isHighSurrogate(c)) {
        if (i + 1 >= input.length()) {
          output[byteCount++] = (char) 63;
        } else {
          final var low = input.charAt(i + 1);
          if (Character.isLowSurrogate(low)) {
            final var intChar = Character.toCodePoint(c, low);
            output[byteCount++] = (byte) (240 | intChar >> 18);
            output[byteCount++] = (byte) (128 | intChar >> 12 & 63);
            output[byteCount++] = (byte) (128 | intChar >> 6 & 63);
            output[byteCount++] = (byte) (128 | intChar & 63);
            ++i;
          } else {
            output[byteCount++] = (char) 63;
          }
        }
      } else if (Character.isLowSurrogate(c)) {
        output[byteCount++] = (char) 63;
      } else {
        output[byteCount++] = (byte) (224 | c >> 12);
        output[byteCount++] = (byte) (128 | c >> 6 & 63);
        output[byteCount++] = (byte) (128 | c & 63);
      }
    }
    if (byteCount == output.length) {
      return output;
    } else {
      final var copy = new byte[byteCount];
      System.arraycopy(output, 0, copy, 0, byteCount);
      return copy;
    }
  }
}
