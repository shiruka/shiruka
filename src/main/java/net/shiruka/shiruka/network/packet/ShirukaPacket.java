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

import com.whirvis.jraknet.Packet;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import org.jetbrains.annotations.NotNull;

/**
 * a generic abstract packet for Shiru ka packets.
 */
public abstract class ShirukaPacket extends Packet {

  /**
   * the id of the {@link LoginPacket}.
   */
  public static final int ID_LOGIN = 1;

  /**
   * the id of the {@link PlayStatusPacket}.
   */
  public static final int ID_PLAY_STATUS = 2;

  /**
   * the id.
   */
  private final int id;

  /**
   * ctor.
   *
   * @param id the id.
   */
  protected ShirukaPacket(final int id) {
    this.id = id;
  }

  public void decode() {
    // ignored.
  }

  public void encode() {
    // ignored.
  }

  /**
   * obtains the id.
   *
   * @return id.
   */
  public int getId() {
    return this.id;
  }

  /**
   * handles the packet.
   *
   * @param handler the handler to handle.
   */
  public void handle(@NotNull final PacketHandler handler) {
  }
}
