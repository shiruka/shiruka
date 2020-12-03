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

/**
 * an interface to determine methods that have to wrote by native codes.
 */
public class NativeProceed {

  /**
   * ctor.
   */
  public NativeProceed() {
  }

  /**
   * constructs a new context in native.
   *
   * @param encryptionModeToggle {@code true} for encryption and compression,
   *   {@code false} for decryption and decompression.
   *
   * @return reference to the native struct.
   */
  static native long createNewContext(boolean encryptionModeToggle);

  /**
   * enables or disables debug.
   *
   * @param ctx for which we want to change the debug flag.
   * @param debug true when enabled, false otherwise.
   */
  static native void debug(long ctx, boolean debug);

  /**
   * destroys the context given.
   *
   * @param ctx which should be destroyed.
   */
  static native void destroyContext(long ctx);

  /**
   * enables the cryptographic part of the extension.
   *
   * @param ctx which should be enabled for crypto.
   * @param key to use for encrypt / decrypt.
   * @param iv to use for init of random data.
   */
  static native void enableCrypto(long ctx, byte[] key, byte[] iv);

  /**
   * how much bytes should be allocate when decompressing.
   *
   * @param ctx for which we want to set the preallocate size.
   * @param size which should be used for preallocate.
   */
  static native void preallocateSize(long ctx, int size);

  /**
   * processes given data.
   *
   * @param ctx with which we want to process given data.
   * @param pointer which holds the data.
   *
   * @return pointer which holds the processed data.
   */
  static native SizedMemoryPointer process(long ctx, SizedMemoryPointer pointer);
}
