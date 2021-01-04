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

package io.github.shiruka.shiruka.entity;

import io.github.shiruka.api.entity.Player;
import io.github.shiruka.api.metadata.MetadataValue;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.shiruka.ShirukaServer;
import io.github.shiruka.shiruka.event.SimpleLoginData;
import io.github.shiruka.shiruka.network.packets.PacketOutDisconnect;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Player}.
 */
public final class ShirukaPlayer implements Player {

  /**
   * the connection.
   */
  @NotNull
  private final ShirukaPlayerConnection connection;

  /**
   * the login data.
   */
  @Nullable
  private SimpleLoginData loginData;

  /**
   * ctor.
   *
   * @param connection the connection.
   */
  public ShirukaPlayer(@NotNull final ShirukaPlayerConnection connection) {
    this.connection = connection;
  }

  @Override
  public void disconnect(@Nullable final String reason) {
    this.connection.getConnection().checkForClosed();
    final String finalReason;
    final boolean messageSkipped;
    if (reason == null) {
      finalReason = "disconnect.disconnected";
      messageSkipped = true;
    } else {
      finalReason = reason;
      messageSkipped = false;
    }
    this.connection.sendPacket(new PacketOutDisconnect(finalReason, messageSkipped));
  }

  @NotNull
  @Override
  public ShirukaServer getServer() {
    return this.connection.getServer();
  }

  /**
   * obtains the latest login data.
   *
   * @return latest login data.
   */
  @Nullable
  public SimpleLoginData getLatestLoginData() {
    return this.loginData;
  }

  /**
   * sets the latest login data of the player.
   *
   * @param loginData the login data to set.
   */
  public void setLatestLoginData(@NotNull final SimpleLoginData loginData) {
    this.loginData = loginData;
  }

  @NotNull
  @Override
  public List<MetadataValue> getMetadata(@NotNull final String key) {
    return Collections.emptyList();
  }

  @Override
  public boolean hasMetadata(@NotNull final String key) {
    return false;
  }

  @Override
  public void removeAllMetadata(@NotNull final String key) {
    // @todo #1:15m Implement removeAllMetadata method.
  }

  @Override
  public void removeMetadata(@NotNull final String key, @NotNull final Plugin plugin) {
    // @todo #1:15m Implement removeMetadata method.
  }

  @Override
  public void setMetadata(@NotNull final String key, @NotNull final MetadataValue value) {
    // @todo #1:15m Implement setMetadata method.
  }

  @NotNull
  public ShirukaPlayerConnection getPlayerConnection() {
    return this.connection;
  }

  @NotNull
  @Override
  public String name() {
    return "null";
  }

  @Override
  public void sendMessage(@NotNull final String message) {
    // @todo #1:15m Implement sendMessage method.
    System.out.println(message);
  }
}
