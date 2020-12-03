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

package io.github.shiruka.shiruka.natives.crc32c;

import java.nio.ByteBuffer;
import java.util.function.Supplier;
import java.util.zip.CRC32C;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Crc32C}.
 */
public final class Jdk9Crc32C implements Crc32C {

  /**
   * the creator supplier.
   */
  public static final Supplier<Crc32C> CREATOR = Jdk9Crc32C::new;

  /**
   * the Crc32C implementation of JDK
   */
  private final CRC32C crc32c = new CRC32C();

  /**
   * ctor.
   */
  private Jdk9Crc32C() {
  }

  @Override
  public void free() {
    // no-op
  }

  @Override
  public void update(@NotNull final ByteBuffer buffer) {
    this.crc32c.update(buffer);
  }

  @Override
  public void update(final int b) {
    this.crc32c.update(b);
  }

  @Override
  public void update(final byte[] b, final int off, final int len) {
    this.crc32c.update(b, off, len);
  }

  @Override
  public long getValue() {
    return this.crc32c.getValue();
  }

  @Override
  public void reset() {
    this.crc32c.reset();
  }
}