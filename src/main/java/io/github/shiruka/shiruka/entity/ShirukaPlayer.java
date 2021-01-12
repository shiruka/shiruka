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

package io.github.shiruka.shiruka.entity;

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.base.GameProfile;
import io.github.shiruka.api.base.Location;
import io.github.shiruka.api.entity.Player;
import io.github.shiruka.api.events.KickEvent;
import io.github.shiruka.api.events.LoginDataEvent;
import io.github.shiruka.api.metadata.MetadataValue;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.text.Text;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.ShirukaServer;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Player}.
 *
 * @todo #1:60m Implement methods of shiruka player.
 */
public final class ShirukaPlayer implements Player {

  /**
   * the chain data.
   */
  @NotNull
  private final LoginDataEvent.ChainData chainData;

  /**
   * the connection.
   */
  @NotNull
  private final PlayerConnection connection;

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * ctor.
   *
   * @param chainData the chain data.
   * @param connection the connection.
   * @param profile the profile.
   */
  public ShirukaPlayer(@NotNull final LoginDataEvent.ChainData chainData,
                       @NotNull final PlayerConnection connection, @NotNull final GameProfile profile) {
    this.chainData = chainData;
    this.connection = connection;
    this.profile = profile;
  }

  /**
   * obtains the leave message.
   *
   * @return leave message.
   */
  @NotNull
  private static TranslatedText getLeaveMessage() {
    return TranslatedText.get("multiplayer.player.left");
  }

  @Nullable
  @Override
  public Location getBedSpawnLocation() {
    return null;
  }

  @Override
  public long getFirstPlayed() {
    return 0;
  }

  @Override
  public long getLastLogin() {
    return 0;
  }

  @Override
  public long getLastSeen() {
    return 0;
  }

  @NotNull
  @Override
  public Optional<Player> getPlayer() {
    return Optional.empty();
  }

  @Override
  public boolean hasPlayedBefore() {
    return false;
  }

  @Override
  public boolean isBanned() {
    return false;
  }

  @Override
  public boolean isOnline() {
    return false;
  }

  @Override
  public boolean isWhitelisted() {
    return false;
  }

  @Override
  public void setWhitelisted(final boolean value) {
  }

  @NotNull
  @Override
  public LoginDataEvent.ChainData getChainData() {
    return this.chainData;
  }

  @NotNull
  @Override
  public GameProfile getProfile() {
    return this.profile;
  }

  @NotNull
  @Override
  public ShirukaServer getServer() {
    return this.connection.getServer();
  }

  @Override
  public boolean kick(@NotNull final KickEvent.Reason reason, @NotNull final String reasonString,
                      final boolean isAdmin) {
    final var event = Shiruka.getEventManager().playerKick(this, reason, ShirukaPlayer.getLeaveMessage());
    event.callEvent();
    if (event.cancelled()) {
      return false;
    }
    final String message;
    if (isAdmin) {
      if (this.isBanned()) {
        message = reasonString;
      } else {
        message = "Kicked by admin." + (!reasonString.isEmpty() ? " Reason: " + reasonString : "");
      }
    } else {
      if (reasonString.isEmpty()) {
        message = "disconnectionScreen.noReason";
      } else {
        message = reasonString;
      }
    }
    this.connection.disconnect(event.kickMessage(), message);
    return true;
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

  /**
   * obtains the player connection.
   *
   * @return player connection.
   */
  @NotNull
  public PlayerConnection getPlayerConnection() {
    return this.connection;
  }

  @Override
  public void sendMessage(@NotNull final Text message, @NotNull final Object... params) {
    // @todo #1:15m Implement sendMessage method.
    System.out.println(message);
  }

  @Override
  public void tick() {
    // @todo #1:30m Implement tick method of shiruka player.
  }
}
