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

package io.github.shiruka.shiruka.natives.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * an exception to determine the buffer does not have enough space to write in it.
 */
public final class BufferNoSpaceException extends IllegalArgumentException {

  /**
   * ctor.
   */
  public BufferNoSpaceException() {
  }

  /**
   * ctor.
   *
   * @param s the detail message.
   */
  public BufferNoSpaceException(@NotNull final String s) {
    super(s);
  }

  /**
   * ctor.
   *
   * @param message the detail message (which is saved for later retrieval
   *   by the {@link Throwable#getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the
   *   {@link Throwable#getCause()} method).  (A {@code null} value
   *   is permitted, and indicates that the cause is nonexistent or
   *   unknown.)
   */
  public BufferNoSpaceException(@NotNull final String message, @NotNull final Throwable cause) {
    super(message, cause);
  }

  /**
   * ctor.
   *
   * @param cause the cause (which is saved for later retrieval by the
   *   {@link Throwable#getCause()} method).  (A {@code null} value is
   *   permitted, and indicates that the cause is nonexistent or
   *   unknown.)
   */
  public BufferNoSpaceException(@NotNull final Throwable cause) {
    super(cause);
  }
}
