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

package io.github.shiruka.shiruka.natives.sha256;

import io.github.shiruka.shiruka.natives.util.ByteBufferUtils;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import sun.nio.ch.DirectBuffer;

/**
 * an implementation for {@link Sha256}.
 */
public final class NativeSha256 implements Sha256 {

  /**
   * the creator supplier.
   */
  public static final Supplier<Sha256> CREATOR = NativeSha256::new;

  /**
   * the context.
   */
  private volatile long ctx;

  /**
   * ctor.
   */
  private NativeSha256() {
    this.ctx = this.init();
  }

  public byte[] digest() {
    return this.digest(this.ctx);
  }

  @Override
  public void reset() {
    this.ensureOpen();
    this.reset(this.ctx);
  }

  public void update(final byte[] input, final int offset, final int len) {
    this.ensureOpen();
    this.update(this.ctx, input, offset, len);
  }

  public void update(@NotNull final ByteBuffer buffer) {
    this.ensureOpen();
    if (buffer.isDirect()) {
      this.update(this.ctx, ((DirectBuffer) buffer).address() + buffer.arrayOffset(), buffer.remaining());
    } else {
      final byte[] array = ByteBufferUtils.getBufferArray(buffer);
      final int offset = ByteBufferUtils.getBufferOffset(buffer);
      this.update(this.ctx, array, offset, buffer.remaining());
    }
  }

  @Override
  public synchronized void free() {
    if (this.ctx != 0) {
      this.free(this.ctx);
      this.ctx = 0;
    }
  }

  private native byte[] digest(long ctx);

  private void ensureOpen() {
    if (this.ctx == 0) {
      throw new IllegalStateException("Native resource has already been freed");
    }
  }

  private native void free(long ctx);

  private native long init();

  private native void reset(long ctx);

  private native void update(long ctx, byte[] bytes, int offset, int length);

  private native void update(long ctx, long in, int length);
}
