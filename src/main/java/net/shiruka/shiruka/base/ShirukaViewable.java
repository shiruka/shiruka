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

package net.shiruka.shiruka.base;

import java.util.Arrays;
import net.shiruka.api.base.Viewable;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation of {@link Viewable}.
 */
public interface ShirukaViewable extends Viewable {

  /**
   * Sends a packet to all viewers and the viewable element if it is a player.
   *
   * @param packet the packet to send
   */
  default void sendPacketToViewersAndSelf(@NotNull final ShirukaPacket packet) {
    if (this instanceof ShirukaPlayer) {
      ((ShirukaPlayer) this).getConnection().sendPacket(packet);
    }
    this.sendPacketsToViewers(packet);
  }

  /**
   * sends multiple packets to all viewers.
   *
   * @param packets the packets to send.
   */
  default void sendPacketsToViewers(@NotNull final ShirukaPacket... packets) {
    this.getViewers().stream()
      .filter(ShirukaPlayer.class::isInstance)
      .map(ShirukaPlayer.class::cast)
      .forEach(player ->
        Arrays.stream(packets).forEach(packet ->
          player.getConnection().sendPacket(packet)));
  }
}
