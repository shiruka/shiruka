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

package io.github.shiruka.shiruka.network.impl;

import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.natives.Proceed;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.ConnectionListener;
import io.github.shiruka.shiruka.network.ConnectionState;
import io.github.shiruka.shiruka.network.DisconnectReason;
import io.github.shiruka.shiruka.network.misc.EncapsulatedPacket;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.util.Constants;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ConnectionListener}.
 */
public final class ShirukaConnectionListener implements ConnectionListener {

  /**
   * the connection.
   */
  @NotNull
  private final Connection<ServerSocket> connection;

  /**
   * the input processor to decrypt.
   */
  @NotNull
  private final Proceed inputProcessor = new Proceed(false);

  /**
   * the output processor to encrypt.
   */
  @NotNull
  private final Proceed outputProcessor = new Proceed(true);

  /**
   * ctor.
   *
   * @param connection the connection.
   */
  ShirukaConnectionListener(@NotNull final Connection<ServerSocket> connection) {
    this.connection = connection;
  }

  @Override
  public void onDirect(@NotNull final ByteBuf packet) {
    Loggers.debug("onDirect");
  }

  @Override
  public void onDisconnect(@NotNull final DisconnectReason reason) {
    Loggers.debug("onDisconnect");
  }

  @Override
  public void onEncapsulated(@NotNull final EncapsulatedPacket packet) {
    Loggers.debug("onEncapsulated");
    final var buffer = packet.getBuffer();
    if (buffer.readableBytes() <= 0) {
      return;
    }
    final var packetId = buffer.readUnsignedByte();
    if (packetId != Constants.BATCH_MAGIC) {
      buffer.readerIndex(0);
      return;
    }
    final var purePacket = this.inputProcessor.process(buffer);
    if (purePacket.readableBytes() <= 0) {
      return;
    }
    try {
      while (purePacket.readableBytes() > 0) {
        final int packetLength = VarInts.readUnsignedVarInt(purePacket);
        final int currentIndex = purePacket.readerIndex();
        final var packetID = this.handlePacket(purePacket, currentIndex + packetLength);
        final var consumedByPacket = purePacket.readerIndex() - currentIndex;
        if (consumedByPacket != packetLength) {
          final int remaining = packetLength - consumedByPacket;
          Loggers.error("Malformed batch packet payload: Could not read enclosed packet data correctly: 0x%s remaining %s bytes",
            Integer.toHexString(packetID), remaining);
          return;
        }
      }
    } finally {
      purePacket.release();
    }
  }

  @Override
  public void onStateChanged(@NotNull final ConnectionState old, @NotNull final ConnectionState state) {
    Loggers.debug("onStateChanged");
  }

  /**
   * handles the given buffer packet.
   *
   * @param buffer the buffer to handler
   * @param skippablePosition the skippable position to handle.
   *
   * @return handled packet's id.
   */
  private int handlePacket(@NotNull final ByteBuf buffer, final int skippablePosition) {
    final int rawId = VarInts.readUnsignedVarInt(buffer);
    final int packetId = rawId & 0x3FF;
    if (packetId == Constants.BATCH_MAGIC) {
      Loggers.error("Malformed batch packet payload: Batch packets are not allowed to contain further batch packets!");
      return packetId;
    }
    System.out.println(packetId);
    return packetId;
  }
}
