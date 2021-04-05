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
import java.util.Base64;
import net.shiruka.api.Shiruka;
import net.shiruka.shiruka.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that encrypts requested forge.
 */
public final class EncryptionRequestForger {

  /**
   * the encoder.
   */
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

  /**
   * ctor.
   */
  private EncryptionRequestForger() {
  }

  /**
   * forges a new Encryption start JWT token.
   *
   * @param serverPublic the server public key to forge.
   * @param serverPrivate the server private key to forge.
   * @param clientSalt the client salt to forge.
   *
   * @return a signed and ready to be sent JWT token
   */
  @Nullable
  public static String forge(@NotNull final String serverPublic, @NotNull final Key serverPrivate,
                             final byte @NotNull [] clientSalt) {
    final var algorithm = JwtAlgorithm.ES384;
    final var header = Shiruka.JSON_MAPPER.createObjectNode();
    header.put("alg", JwtAlgorithm.ES384.name());
    header.put("x5u", serverPublic);
    final var claims = Shiruka.JSON_MAPPER.createObjectNode();
    claims.put("salt", Base64.getEncoder().encodeToString(clientSalt));
    final var builder = new StringBuilder()
      .append(EncryptionRequestForger.ENCODER.encodeToString(StringUtil.getUTF8Bytes(header.toString())))
      .append('.')
      .append(EncryptionRequestForger.ENCODER.encodeToString(StringUtil.getUTF8Bytes(claims.toString())));
    final var signatureBytes = StringUtil.getUTF8Bytes(builder.toString());
    try {
      builder.append('.')
        .append(EncryptionRequestForger.ENCODER.encodeToString(
          algorithm.getValidator().sign(serverPrivate, signatureBytes)));
      return builder.toString();
    } catch (final JwtSignatureException e) {
      e.printStackTrace();
    }
    return null;
  }
}
