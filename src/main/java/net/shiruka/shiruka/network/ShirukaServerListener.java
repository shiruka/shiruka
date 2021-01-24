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

import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.peer.RakNetState;
import com.whirvis.jraknet.server.RakNetServer;
import com.whirvis.jraknet.server.RakNetServerListener;
import com.whirvis.jraknet.server.ServerPing;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.network.protocol.Protocol;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link RakNetServerListener}.
 */
public final class ShirukaServerListener implements RakNetServerListener {

  /**
   * the server instance.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaServerListener(@NotNull final ShirukaServer server) {
    this.server = server;
  }

  @Override
  public void onPing(final RakNetServer server, final ServerPing ping) {
    this.server.onPing(ping);
  }

  @Override
  public void handleMessage(final RakNetServer server, final RakNetClientPeer peer, final RakNetPacket packet,
                            final int channel) {
    if (peer.getState() != RakNetState.CONNECTED) {
      return;
    }
    final var packetId = packet.readUnsignedByte();
    if (packetId == 0xfe) {
      packet.buffer().markReaderIndex();
      Protocol.deserialize(packet, peer);
    }
  }
}
