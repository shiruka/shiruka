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

package net.shiruka.shiruka.network;

import net.shiruka.api.base.Tick;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.ClientToServerHandshakePacket;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkRequestPacket;
import net.shiruka.shiruka.network.packets.ResourcePackResponsePacket;
import net.shiruka.shiruka.network.packets.ViolationWarningPacket;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine packet handlers.
 */
public interface PacketHandler extends Tick {

  /**
   * handles the client cache status packet.
   *
   * @param packet the packet to handle.
   */
  default void clientCacheStatus(@NotNull final ClientCacheStatusPacket packet) {
  }

  /**
   * handles the client to server handshake packet.
   *
   * @param packet the packet to handle.
   */
  default void clientToServerHandshake(@NotNull final ClientToServerHandshakePacket packet) {
  }

  /**
   * obtains the network manager.
   *
   * @return network manager.
   */
  @NotNull
  NetworkManager getNetworkManager();

  /**
   * handles the login packet.
   *
   * @param packet the packet to handle.
   */
  default void login(@NotNull final LoginPacket packet) {
  }

  /**
   * runs when the connection is disconnected.
   *
   * @param disconnectMessage the disconnect message to disconnect.
   */
  void onDisconnect(@NotNull Text disconnectMessage);

  /**
   * handles the resource pack chunk request packet.
   *
   * @param packet the packet to handle.
   */
  default void resourcePackChunkRequest(@NotNull final ResourcePackChunkRequestPacket packet) {
  }

  /**
   * handles the resource pack response packet.
   *
   * @param packet the packet to handle.
   */
  default void resourcePackResponse(@NotNull final ResourcePackResponsePacket packet) {
  }

  /**
   * handles the violation warning packet.
   *
   * @param packet the packet to handle.
   */
  default void violationWarning(@NotNull final ViolationWarningPacket packet) {
  }
}
