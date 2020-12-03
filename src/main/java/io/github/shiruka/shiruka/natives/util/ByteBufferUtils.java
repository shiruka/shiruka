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

package io.github.shiruka.shiruka.natives.util;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains byte buffer utility methods.
 */
public final class ByteBufferUtils {

  /**
   * byte buffer's byte array field's offset.
   */
  private static final long BYTE_BUFFER_ARRAY_OFFSET = UnsafeUtils.objectFieldOffset(ByteBuffer.class, "hb");

  /**
   * byte buffer's offset field's offset.
   */
  private static final long BYTE_BUFFER_OFFSET_OFFSET = UnsafeUtils.objectFieldOffset(ByteBuffer.class, "offset");

  /**
   * ctor.
   */
  private ByteBufferUtils() {
  }

  /**
   * obtains byte array from the given byte buffer.
   *
   * @param byteBuffer the byte buffer to get.
   *
   * @return a byte array instance of byte buffer.
   */
  public static byte[] getBufferArray(@NotNull final ByteBuffer byteBuffer) {
    return (byte[]) UnsafeUtils.getObject(byteBuffer, ByteBufferUtils.BYTE_BUFFER_ARRAY_OFFSET);
  }

  /**
   * obtains buffer offset from the given byte buffer.
   *
   * @param byteBuffer the byte buffer to get.
   *
   * @return a offset integer of byte buffer.
   */
  public static int getBufferOffset(@NotNull final ByteBuffer byteBuffer) {
    return UnsafeUtils.getInt(byteBuffer, ByteBufferUtils.BYTE_BUFFER_OFFSET_OFFSET);
  }
}