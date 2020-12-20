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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.PacketPriority;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Packets;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that sends by clients to request a login process.
 */
public final class PacketInLogin extends PacketIn {

  /**
   * ctor.
   */
  public PacketInLogin() {
    super(PacketInLogin.class);
  }

  @Override
  public void read(@NotNull final ByteBuf buf, @NotNull final ShirukaPlayer player) {
    final var protocolVersion = buf.readInt();
    final var jwt = buf.readSlice(VarInts.readUnsignedVarInt(buf));
    final var chainData = Packets.readLEAsciiString(jwt);
    final var skinData = Packets.readLEAsciiString(jwt);
    if (protocolVersion < Constants.MINECRAFT_PROTOCOL_VERSION) {
      final var packet = new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_CLIENT_OLD);
      player.getPlayerConnection().sendPacket(packet, PacketPriority.IMMEDIATE);
    } else if (protocolVersion > Constants.MINECRAFT_PROTOCOL_VERSION) {
      final var packet = new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_SERVER_OLD);
      player.getPlayerConnection().sendPacket(packet, PacketPriority.IMMEDIATE);
      return;
    }
    // TODO Continue to development here.
  }
}
