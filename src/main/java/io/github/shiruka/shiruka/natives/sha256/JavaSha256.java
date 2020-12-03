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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link  Sha256}.
 */
public final class JavaSha256 implements Sha256 {

  /**
   * the creator supplier.
   */
  public static final Supplier<Sha256> CREATOR = JavaSha256::new;

  /**
   * the diggest.
   */
  @NotNull
  private final MessageDigest digest;

  private JavaSha256() {
    try {
      this.digest = MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public byte[] digest() {
    return this.digest.digest();
  }

  @Override
  public void reset() {
    this.digest.reset();
  }

  @Override
  public void update(final byte[] input, final int offset, final int len) {
    this.digest.update(input, offset, len);
  }

  @Override
  public void update(@NotNull final ByteBuffer buffer) {
    this.digest.update(buffer);
  }

  @Override
  public void free() {
  }
}
