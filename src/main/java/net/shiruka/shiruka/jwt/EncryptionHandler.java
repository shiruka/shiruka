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

import io.netty.buffer.PooledByteBufAllocator;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.KeyAgreement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.shiruka.shiruka.base.SimpleChainData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that handles encryption.
 */
@Log4j2
@RequiredArgsConstructor
public final class EncryptionHandler {

  /**
   * the sha256 digest.
   */
  private static final ThreadLocal<MessageDigest> SHA256_DIGEST = new ThreadLocal<>();

  /**
   * the client public key.
   */
  @NotNull
  private final ECPublicKey clientPublicKey;

  /**
   * the key pair.
   */
  @NotNull
  private final KeyPair keyPair = Objects.requireNonNull(SimpleChainData.getKeyPair(), "key pair");

  /**
   * the client salt.
   */
  @Getter
  private byte[] clientSalt;

  /**
   * the iv.
   */
  @Getter
  private byte[] iv;

  /**
   * the key.
   */
  @Getter
  private byte[] key;

  /**
   * generates ECDH secret key.
   *
   * @param privateKey the private key to create.
   * @param publicKey the public key to create.
   *
   * @return a newly created ECDH secret key.
   */
  private static byte @Nullable [] generateECDHSecret(@NotNull final PrivateKey privateKey,
                                                      @NotNull final PublicKey publicKey) {
    try {
      final var agreement = KeyAgreement.getInstance("ECDH");
      agreement.init(privateKey);
      agreement.doPhase(publicKey, true);
      return agreement.generateSecret();
    } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
      // @todo #1:5m Add language support for Failed to generate Elliptic-Curve-Diffie-Hellman Shared Secret for clientside encryption.
      EncryptionHandler.log.error(
        "Failed to generate Elliptic-Curve-Diffie-Hellman Shared Secret for clientside encryption", e);
      return null;
    }
  }

  /**
   * obtains sha256.
   *
   * @return sha256.
   *
   * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
   *   algorithm.
   */
  @NotNull
  private static MessageDigest getSHA256() throws NoSuchAlgorithmException {
    var digest = EncryptionHandler.SHA256_DIGEST.get();
    if (digest != null) {
      digest.reset();
      return digest;
    }
    digest = MessageDigest.getInstance("SHA-256");
    EncryptionHandler.SHA256_DIGEST.set(digest);
    return digest;
  }

  /**
   * hashes the given messages with sha256.
   *
   * @param message the message to hash.
   *
   * @return hashed messages.
   *
   * @throws NoSuchAlgorithmException if no Provider supports a MessageDigestSpi implementation for the specified
   *   algorithm.
   */
  private static byte @NotNull [] hashSHA256(final byte @NotNull []... message) throws NoSuchAlgorithmException {
    final var digest = EncryptionHandler.getSHA256();
    final var buf = PooledByteBufAllocator.DEFAULT.directBuffer();
    Arrays.stream(message).forEach(buf::writeBytes);
    digest.update(buf.nioBuffer());
    buf.release();
    return digest.digest();
  }

  /**
   * begins the client side encryption.
   *
   * @return {@code true} if the client side encryption has succeeded.
   */
  public boolean beginClientsideEncryption() {
    if (this.key != null && this.clientSalt != null) {
      // @todo #1:5m Add language support for Already initialized.
      EncryptionHandler.log.debug("Already initialized");
      return true;
    }
    this.clientSalt = new byte[16];
    ThreadLocalRandom.current().nextBytes(this.clientSalt);
    final var secret = EncryptionHandler.generateECDHSecret(this.getServerPrivate(), this.clientPublicKey);
    if (secret == null) {
      return false;
    }
    try {
      this.key = EncryptionHandler.hashSHA256(this.clientSalt, secret);
    } catch (final NoSuchAlgorithmException e) {
      // @todo #1:5m Add language support for no sha-265 found.
      EncryptionHandler.log.error("no sha-265 found", e);
      return false;
    }
    final var result = new byte[16];
    System.arraycopy(this.key, 0, result, 0, 16);
    this.iv = result;
    return true;
  }

  /**
   * obtains the server private key.
   *
   * @return the server private key.
   */
  @NotNull
  public PrivateKey getServerPrivate() {
    return this.keyPair.getPrivate();
  }

  /**
   * obtains the server public.
   *
   * @return server public.
   */
  @NotNull
  public String serverPublic() {
    return Base64.getEncoder().encodeToString(this.keyPair.getPublic().getEncoded());
  }
}