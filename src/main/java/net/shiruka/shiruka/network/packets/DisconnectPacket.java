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

package net.shiruka.shiruka.network.packets;

import net.shiruka.shiruka.network.ShirukaPacket;
import net.shiruka.shiruka.network.VarInts;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that disconnects the sent client.
 */
public final class DisconnectPacket extends ShirukaPacket {

  /**
   * the kick message.
   */
  @NotNull
  private final String kickMessage;

  /**
   * the message skipped.
   */
  private final boolean messageSkipped;

  /**
   * ctor.
   *
   * @param kickMessage the kick message.
   * @param messageSkipped the message skipped.
   */
  public DisconnectPacket(@NotNull final String kickMessage, final boolean messageSkipped) {
    super(ShirukaPacket.ID_DISCONNECT);
    this.kickMessage = kickMessage;
    this.messageSkipped = messageSkipped;
  }

  @Override
  public void encode() {
    this.buffer().writeBoolean(this.messageSkipped);
    if (!this.messageSkipped) {
      VarInts.writeString(this.buffer(), this.kickMessage);
    }
  }
}
