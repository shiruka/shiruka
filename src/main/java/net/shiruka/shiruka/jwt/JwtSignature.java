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

package net.shiruka.shiruka.jwt;

import java.security.Key;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine jwt signatures.
 */
public interface JwtSignature {

  /**
   * signs the given signature with the key.
   *
   * @param key the key to sign.
   * @param signature the signature to sign.
   *
   * @return signed signature.
   *
   * @throws JwtSignatureException if something goes wrong when signing.
   */
  byte[] sign(@NotNull Key key, byte @NotNull [] signature) throws JwtSignatureException;

  /**
   * validates the given signature with the key and digest.
   *
   * @param key the key to validate.
   * @param signature the signature to validate.
   * @param digest the digest to validate.
   *
   * @return {@code true} if the signature is valid.
   *
   * @throws JwtSignatureException if something goes wrong when validating.
   */
  boolean validate(@NotNull Key key, byte @NotNull [] signature, byte @NotNull [] digest) throws JwtSignatureException;
}
