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

package net.shiruka.shiruka.network.packet;

import com.google.common.base.Preconditions;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import java.util.Objects;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a generic abstract packet for Shiru ka packets.
 */
public abstract class ShirukaPacket extends RakNetPacket {

  /**
   * the id of the {@link LoginPacket}.
   */
  public static final int ID_LOGIN = 1;

  /**
   * the id of the {@link PlayStatusPacket}.
   */
  public static final int ID_PLAY_STATUS = 2;

  /**
   * ctor.
   *
   * @param id the id.
   */
  protected ShirukaPacket(final int id, @NotNull final RakNetPacket packet) {
    super(id);
    Preconditions.checkArgument(id >= RakNetPacket.ID_USER_PACKET_ENUM,
      "Packet ID must be in between %s-255",
      RakNetPacket.ID_USER_PACKET_ENUM);
    this.setBuffer(packet.buffer());
  }

  /**
   * decodes the packet.
   *
   * @param connection the connection to decode.
   */
  public void decode(@NotNull final RakNetClientPeer connection) {
  }

  /**
   * encodes the packet.
   *
   * @param connection the connection to encode.
   */
  public void encode(@NotNull final RakNetClientPeer connection) {
  }

  @Override
  public void encode() {
    // ignored.
  }

  @Override
  public void decode() {
    // ignored.
  }

  /**
   * handles the packet.
   *
   * @param handler the handler to handle.
   * @param connection the connection to handle.
   */
  public abstract void handle(@NotNull PacketHandler handler, @NotNull final RakNetClientPeer connection);

  /**
   * checks if the given {@code object} is null.
   *
   * @param object the object to check.
   */
  protected final void check(@Nullable final Object object) {
    Objects.requireNonNull(object, String.format("The packet %s not initiated!", this.getId()));
  }
}
