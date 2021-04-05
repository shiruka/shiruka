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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * an implementation for {@link JwtSignature}.
 */
public final class JwtSignatureES384 implements JwtSignature {

  /**
   * converts an ECDSA signature formatted as specified in RFC3278 (https://tools.ietf.org/html/rfc3278#section-8.2)
   * into DER format as required by the BouncyCastle signature verifier.
   *
   * @param concat the concat to convert.
   *
   * @return converted signature in DER format.
   *
   * @throws JwtSignatureException if the given signature is not a valid ECDSA signature.
   */
  private static byte[] convertConcatRSToDER(final byte[] concat) throws JwtSignatureException {
    if (concat.length != 96) {
      throw new JwtSignatureException("Invalid ECDSA signature (expected 96 bytes, got " + concat.length + ")");
    }
    final var rawLength = concat.length >> 1;
    var offsetR = 0;
    while (concat[offsetR] == 0x00) {
      offsetR++;
    }
    final var lengthR = rawLength - offsetR;
    final var padR = (concat[offsetR] & 0x80) != 0;
    var offsetL = rawLength;
    while (concat[offsetL] == 0x00) {
      offsetL++;
    }
    final var lengthL = (rawLength << 1) - offsetL;
    final var padL = (concat[offsetL] & 0x80) != 0;
    final var sigLength = 2 + lengthR + (padR ? 1 : 0) + 2 + lengthL + (padL ? 1 : 0);
    var cursor = 0;
    final var derSignature = new byte[2 + sigLength];
    derSignature[cursor++] = 0x30;
    derSignature[cursor++] = (byte) sigLength;
    derSignature[cursor++] = 0x02;
    derSignature[cursor++] = (byte) (lengthR + (padR ? 1 : 0));
    if (padR) {
      cursor++;
    }
    System.arraycopy(concat, offsetR, derSignature, cursor, lengthR);
    cursor += lengthR;
    derSignature[cursor++] = 0x02;
    derSignature[cursor++] = (byte) (lengthL + (padL ? 1 : 0));
    if (padL) {
      cursor++;
    }
    System.arraycopy(concat, offsetL, derSignature, cursor, lengthL);
    cursor += lengthL;
    if (cursor != derSignature.length) {
      throw new JwtSignatureException("COuld not convert ECDSA signature to DER format");
    }
    return derSignature;
  }

  /**
   * converts a DER signature as produced by the BouncyCastle signature signer into the ECSDA's concatenated R+S format.
   *
   * @param der the DER signature to convert.
   *
   * @return converted ECDSA signature.
   *
   * @throws JwtSignatureException in case the specified DER signature was invalid or not suitable for ECDSA-384.
   */
  private static byte[] convertDERToConcatRS(final byte[] der) throws JwtSignatureException {
    if (der.length < 8 || der[0] != 0x30 || der[1] > 128) {
      throw new JwtSignatureException("Invalid DER signature");
    }
    var offsetR = 4;
    var lengthR = der[offsetR - 1];
    var offsetL = offsetR + lengthR + 2;
    if (der[offsetR] == 0x00) {
      offsetR++;
      lengthR--;
    }
    var lengthL = der[offsetL - 1];
    if (der[offsetL] == 0x00) {
      offsetL++;
      lengthL--;
    }
    if (lengthR > 48 || lengthL > 48) {
      throw new JwtSignatureException("Invalid DER signature for ECSDA 384 bit");
    }
    final var concat = new byte[96];
    System.arraycopy(der, offsetR, concat, 48 - lengthR, lengthR);
    System.arraycopy(der, offsetL, concat, 96 - lengthL, lengthL);
    return concat;
  }

  @Override
  public byte[] sign(final Key key, final byte[] signature) throws JwtSignatureException {
    final Signature sign;
    try {
      sign = Signature.getInstance("SHA384withECDSA");
    } catch (final NoSuchAlgorithmException e) {
      throw new JwtSignatureException("Could not create signature for ES384 algorithm", e);
    }
    if (!(key instanceof PrivateKey)) {
      throw new JwtSignatureException("Signature Key must be a PrivateKey for ES384 signing");
    }
    final PrivateKey privateKey = (PrivateKey) key;
    final byte[] der;
    try {
      sign.initSign(privateKey);
      sign.update(signature);
      der = sign.sign();
    } catch (final SignatureException | InvalidKeyException e) {
      throw new JwtSignatureException("Could not sign ES384 signature", e);
    }
    return JwtSignatureES384.convertDERToConcatRS(der);
  }

  @Override
  public boolean validate(final Key key, final byte[] signature, final byte[] digest) throws JwtSignatureException {
    final Signature sign;
    try {
      sign = Signature.getInstance("SHA384withECDSA");
    } catch (final NoSuchAlgorithmException e) {
      throw new JwtSignatureException("Could not create signature for ES384 algorithm", e);
    }
    if (!(key instanceof PublicKey)) {
      throw new JwtSignatureException("Signature Key must be a PublicKey for ES384 validation");
    }
    final var publicKey = (PublicKey) key;
    final var derSignature = JwtSignatureES384.convertConcatRSToDER(digest);
    try {
      sign.initVerify(publicKey);
      sign.update(signature);
      return sign.verify(derSignature);
    } catch (final SignatureException | InvalidKeyException e) {
      throw new JwtSignatureException("Could not perform ES384 signature validation", e);
    }
  }
}
