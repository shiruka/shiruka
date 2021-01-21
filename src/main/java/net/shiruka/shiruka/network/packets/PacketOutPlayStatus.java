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

import io.netty.buffer.ByteBuf;
import net.shiruka.shiruka.network.packet.PacketOut;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that sends to clients to notify connection's play status..
 */
public final class PacketOutPlayStatus extends PacketOut {

  /**
   * the status.
   */
  @NotNull
  private final Status status;

  /**
   * ctor.
   *
   * @param status the status.
   */
  public PacketOutPlayStatus(@NotNull final Status status) {
    super(PacketOutPlayStatus.class);
    this.status = status;
  }

  @Override
  public void write(@NotNull final ByteBuf buf) {
    buf.writeInt(this.status.ordinal());
  }

  /**
   * an enum class to determine play status.
   */
  public enum Status {
    /**
     * sent after Login has been successfully decoded and the player has logged in.
     */
    LOGIN_SUCCESS,
    /**
     * displays "Could not connect: Outdated client!".
     */
    LOGIN_FAILED_CLIENT_OLD,
    /**
     * displays "Could not connect: Outdated server!".
     */
    LOGIN_FAILED_SERVER_OLD,
    /**
     * sent after world data to spawn the player.
     */
    PLAYER_SPAWN,
    /**
     * displays "Unable to connect to world. Your school does not have access to this server.".
     */
    LOGIN_FAILED_INVALID_TENANT,
    /**
     * displays "The server is not running Minecraft: Education Edition. Failed to connect.".
     */
    LOGIN_FAILED_EDITION_MISMATCH_EDU_TO_VANILLA,
    /**
     * displays "The server is running an incompatible edition of Minecraft. Failed to connect.".
     */
    LOGIN_FAILED_EDITION_MISMATCH_VANILLA_TO_EDU,
    /**
     * displays "Wow this server is popular! Check back later to see if space opens up. Server Full".
     */
    FAILED_SERVER_FULL_SUB_CLIENT
  }
}
