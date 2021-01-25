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

import com.whirvis.jraknet.Packet;
import io.netty.util.AsciiString;
import java.util.Objects;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a packet that sends by clients to request a login process.
 */
public final class LoginPacket extends ShirukaPacket {

  /**
   * the chain data.
   */
  @Nullable
  private AsciiString chainData;

  /**
   * the protocol version.
   */
  private int protocolVersion;

  /**
   * the skin data.
   */
  @Nullable
  private AsciiString skinData;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public LoginPacket(@NotNull final Packet original) {
    super(ShirukaPacket.ID_LOGIN, original);
  }

  /**
   * ctor.
   */
  public LoginPacket() {
    super(ShirukaPacket.ID_LOGIN);
  }

  @Override
  public void decode() {
    this.setProtocolVersion(this.readInt());
    final var jwt = this.buffer().readSlice((int) this.readUnsignedVarInt());
    this.setChainData(ShirukaPacket.readLEAsciiString(jwt));
    this.setSkinData(ShirukaPacket.readLEAsciiString(jwt));
  }

  @Override
  public void handle(@NotNull final PacketHandler handler) {
    handler.loginPacket(this);
  }

  /**
   * obtains the chain data.
   *
   * @return chain data.
   */
  @NotNull
  public AsciiString getChainData() {
    return Objects.requireNonNull(this.chainData);
  }

  /**
   * sets the chain data.
   *
   * @param chainData the chain data to set.
   */
  public void setChainData(@NotNull final AsciiString chainData) {
    this.chainData = chainData;
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
   * sets the protocol version.
   *
   * @param protocolVersion the protocol version to set.
   */
  public void setProtocolVersion(final int protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  /**
   * obtains the skin data.
   *
   * @return skin data.
   */
  @NotNull
  public AsciiString getSkinData() {
    return Objects.requireNonNull(this.skinData);
  }

  /**
   * sets the skin data.
   *
   * @param skinData the skin data to set.
   */
  public void setSkinData(@NotNull final AsciiString skinData) {
    this.skinData = skinData;
  }
}
