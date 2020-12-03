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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

/**
 * a class uses {@link NativeProceed} class to proceed native codes.
 */
public final class Proceed {

  /**
   * the loader.
   */
  @NotNull
  private static final NativeCode LOADER;

  /**
   * the context.
   */
  private final long context;

  static {
    LOADER = new NativeCode("crypto");
    if (!Proceed.LOADER.load()) {
      throw new IllegalStateException("Could not load crypto native extension!");
    }
  }

  public Proceed(final boolean encryptionModeToggle) {
    this.context = NativeProceed.createNewContext(encryptionModeToggle);
  }

  public void debug(final boolean debug) {
    NativeProceed.debug(this.context, debug);
  }

  public void enableCrypto(final byte[] key, final byte[] iv) {
    NativeProceed.enableCrypto(this.context, key, iv);
  }

  @NotNull
  public ByteBuf process(@NotNull final ByteBuf data) {
    try {
      final var pointerAddress = data.memoryAddress() + data.readerIndex();
      final var size = data.readableBytes();
      final var dataPointer = new SizedMemoryPointer(pointerAddress, size);
      final var processedDataPointer = NativeProceed.process(this.context, dataPointer);
      if (processedDataPointer.getAddress() == 0 || processedDataPointer.getSize() == 0) {
        return PooledByteBufAllocator.DEFAULT.directBuffer();
      }
      return Unpooled.wrappedBuffer(processedDataPointer.getAddress(), processedDataPointer.getSize(), true);
    } finally {
      data.release();
    }
  }
}
