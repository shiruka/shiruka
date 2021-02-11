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

import com.google.common.base.Preconditions;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains utility methods for packets.
 */
public final class PacketUtility {

  /**
   * ctor.
   */
  private PacketUtility() {
  }

  /**
   * disconnects the connection.
   *
   * @param connection the connection to disconnect.
   * @param reason the reason to disconnect.
   */
  public static void disconnect(@NotNull final PlayerConnection connection, @Nullable final String reason) {
    if (reason == null) {
      PacketUtility.disconnect(connection);
    } else {
      PacketUtility.disconnect(connection, () -> reason);
    }
  }

  /**
   * disconnects the connection.
   *
   * @param connection the connection to disconnect.
   */
  public static void disconnect(@NotNull final PlayerConnection connection) {
    PacketUtility.disconnect(connection, (Text) null);
  }

  /**
   * disconnects the connection.
   *
   * @param connection the connection to disconnect.
   * @param reason the reason to disconnect.
   */
  public static void disconnect(@NotNull final PlayerConnection connection, @Nullable final Text reason) {
    Preconditions.checkState(connection.getConnection().isConnected(), "not connected");
    final var message = reason == null ? TranslatedTexts.DISCONNECTED_NO_REASON : reason;
    connection.sendPacket(new DisconnectPacket(message.asString(), reason == null));
  }
}
