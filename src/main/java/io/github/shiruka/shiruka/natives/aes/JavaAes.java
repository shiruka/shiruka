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

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.function.Supplier;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Aes}.
 */
public final class JavaAes implements Aes {

  /**
   * the factory.
   */
  public static final AesFactory FACTORY = JavaAes::new;

  /**
   * the creator supplier.
   */
  public static final Supplier<AesFactory> CREATOR = () -> JavaAes.FACTORY;

  /**
   * the cipher.
   */
  @NotNull
  private final Cipher cipher;

  /**
   * ctor.
   *
   * @param encrypt the encrypt.
   * @param key th key.
   * @param iv the iv.
   */
  private JavaAes(final boolean encrypt, @NotNull final SecretKey key, @NotNull final IvParameterSpec iv) {
    try {
      this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
      final var mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
      this.cipher.init(mode, key, iv);
    } catch (final InvalidAlgorithmParameterException e) {
      throw new IllegalArgumentException("Invalid key given");
    } catch (final GeneralSecurityException e) {
      throw new AssertionError("Expected AES to be available");
    }
  }

  @Override
  public void cipher(@NotNull final ByteBuffer input, @NotNull final ByteBuffer output) throws ShortBufferException {
    this.cipher.update(input, output);
  }

  @Override
  public void free() {
  }
}
