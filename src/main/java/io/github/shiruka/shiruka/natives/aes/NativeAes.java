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

package io.github.shiruka.shiruka.natives.aes;

import io.github.shiruka.shiruka.natives.exceptions.BufferNoSpaceException;
import io.github.shiruka.shiruka.natives.exceptions.RequiredBytesToEncodeException;
import io.github.shiruka.shiruka.natives.util.ByteBufferUtils;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.jetbrains.annotations.NotNull;
import sun.nio.ch.DirectBuffer;

/**
 * an implementation for {@link Aes}.
 */
public final class NativeAes implements Aes {

  /**
   * the factory.
   */
  public static final AesFactory FACTORY = NativeAes::new;

  /**
   * the creator supplier.
   */
  public static final Supplier<AesFactory> CREATOR = () -> NativeAes.FACTORY;

  /**
   * the context.
   */
  private long ctx;

  /**
   * ctor.
   *
   * @param encrypt the encrypt.
   * @param key the key.
   * @param iv the iv
   */
  private NativeAes(final boolean encrypt, @NotNull final SecretKey key, @NotNull final IvParameterSpec iv) {
    if (!"AES".equals(key.getAlgorithm())) {
      throw new IllegalArgumentException("Invalid key given");
    }
    this.ctx = NativeAes.init(encrypt, key.getEncoded(), iv.getIV());
  }

  private static native void free(long ctx);

  private static native long init(boolean encrypt, byte[] key, byte[] iv);

  @Override
  public void cipher(final ByteBuffer input, final ByteBuffer output) {
    this.ensureOpen();
    final var inPos = input.position();
    final var inRem = Math.max(input.limit() - inPos, 0);
    final var outPos = output.position();
    final var outRem = Math.max(output.limit() - outPos, 0);
    if (inRem < 16) {
      throw new RequiredBytesToEncodeException("AES requires at least 16 bytes to encode");
    }
    if (outRem < inRem) {
      throw new BufferNoSpaceException("output buffer does not have enough space");
    }
    if (input.isDirect()) {
      final var inAddress = ((DirectBuffer) input).address();
      if (output.isDirect()) {
        final var outAddress = ((DirectBuffer) output).address();
        this.cipherBufferToBuffer(this.ctx, inAddress, outAddress, inRem);
      } else {
        final var outArray = ByteBufferUtils.getBufferArray(output);
        final var outOffset = ByteBufferUtils.getBufferOffset(output);
        this.cipherBufferToBytes(this.ctx, inAddress, outArray, outOffset, inRem);
      }
    } else {
      final var inArray = ByteBufferUtils.getBufferArray(input);
      final var inOffset = ByteBufferUtils.getBufferOffset(input);
      if (output.isDirect()) {
        final var outAddress = ((DirectBuffer) output).address();
        this.cipherBytesToBuffer(this.ctx, inArray, inOffset, outAddress, inRem);
      } else {
        final var outArray = ByteBufferUtils.getBufferArray(output);
        final var outOffset = ByteBufferUtils.getBufferOffset(output);
        this.cipherBytesToBytes(this.ctx, inArray, inOffset, outArray, outOffset, inRem);
      }
    }
  }

  @Override
  public synchronized void free() {
    if (this.ctx != 0) {
      NativeAes.free(this.ctx);
      this.ctx = 0;
    }
  }

  private native void cipherBufferToBuffer(long ctx, long inAddress, long outAddress, int len);

  private native void cipherBufferToBytes(long ctx, long inAddress, byte[] out, int outOff, int len);

  private native void cipherBytesToBuffer(long ctx, byte[] in, int inOff, long outAddress, int len);

  private native void cipherBytesToBytes(long ctx, byte[] in, int inOff, byte[] out, int outOff, int len);

  private void ensureOpen() {
    if (this.ctx == 0) {
      throw new IllegalStateException("Native resource has already been freed");
    }
  }
}
