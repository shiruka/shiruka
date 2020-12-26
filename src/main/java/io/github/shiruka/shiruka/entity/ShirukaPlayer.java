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

import io.github.shiruka.api.Server;
import io.github.shiruka.api.entity.Player;
import io.github.shiruka.api.metadata.MetadataValue;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.shiruka.network.PacketPriority;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
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
  private final PlayerConnection connection;

  /**
   * ctor.
   *
   * @param connection the connection.
   */
  public ShirukaPlayer(@NotNull final PlayerConnection connection) {
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
    this.connection.sendPacket(new PacketOutDisconnect(finalReason, messageSkipped), PacketPriority.IMMEDIATE);
  }

  @NotNull
  @Override
  public Server getServer() {
    return this.connection.getServer();
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
  }

  @Override
  public void removeMetadata(@NotNull final String key, @NotNull final Plugin plugin) {
  }

  @Override
  public void setMetadata(@NotNull final String key, @NotNull final MetadataValue value) {
  }

  @NotNull
  public PlayerConnection getPlayerConnection() {
    return this.connection;
  }

  @NotNull
  @Override
  public String name() {
    return "null";
  }

  @Override
  public void sendMessage(@NotNull final String s) {
    System.out.println(s);
  }
}
