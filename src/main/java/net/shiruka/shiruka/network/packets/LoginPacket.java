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

import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import io.netty.util.AsciiString;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.packet.ShirukaPacket;
import net.shiruka.shiruka.network.util.PacketHelper;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that sends by clients to request a login process.
 */
public final class LoginPacket extends ShirukaPacket {

  /**
   * the chain data.
   */
  private AsciiString chainData;

  /**
   * the protocol version.
   */
  private int protocolVersion;

  /**
   * the skin data.
   */
  private AsciiString skinData;

  /**
   * ctor.
   */
  public LoginPacket(@NotNull final RakNetPacket packet) throws IllegalArgumentException {
    super(ShirukaPacket.ID_LOGIN, packet);
  }

  @Override
  public void decode() {
    this.protocolVersion = this.readInt();
    final var jwt = this.buffer().readSlice((int) this.readUnsignedVarInt());
    this.chainData = PacketHelper.readLEAsciiString(jwt);
    this.skinData = PacketHelper.readLEAsciiString(jwt);
  }

  @Override
  public void handle(@NotNull final PacketHandler handler, @NotNull final RakNetClientPeer connection) {
    handler.loginPacket(this, connection);
  }

  /**
   * obtains the chain data.
   *
   * @return chain data.
   */
  @NotNull
  public AsciiString getChainData() {
    return this.chainData;
  }

  /**
   * obtains the protocol version.
   *
   * @return protocol version.
   */
  public int getProtocolVersion() {
    return this.protocolVersion;
  }

  /**
   * obtains the skin data.
   *
   * @return skin data.
   */
  @NotNull
  public AsciiString getSkinData() {
    return this.skinData;
  }
}
