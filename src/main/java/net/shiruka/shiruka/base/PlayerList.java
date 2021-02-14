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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.BanList;
import net.shiruka.api.events.LoginResultEvent;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.ban.IpBanList;
import net.shiruka.shiruka.ban.ProfileBanList;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entities.ShirukaPlayer;
import net.shiruka.shiruka.network.PlayerConnection;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents player list.
 */
public final class PlayerList {

  /**
   * the ip ban list.
   */
  public final BanList ipBanList = new IpBanList();

  /**
   * the players.
   */
  public final List<ShirukaPlayer> players = new CopyOnWriteArrayList<>();

  /**
   * the profile ban list.
   */
  public final BanList profileBanList = new ProfileBanList();

  /**
   * the pending players.
   */
  private final Map<String, ShirukaPlayer> pendingPlayers = new Object2ObjectOpenHashMap<>();

  /**
   * the players by name.
   */
  private final Map<String, ShirukaPlayer> playersByName = new Object2ObjectOpenHashMap<>();

  /**
   * the players by unique id.
   */
  private final Map<UUID, ShirukaPlayer> playersByUniqueId = new Object2ObjectOpenHashMap<>();

  /**
   * the players by xbox unique id.
   */
  private final Map<String, ShirukaPlayer> playersByXboxUniqueId = new Object2ObjectOpenHashMap<>();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the max players.
   */
  public int maxPlayers = ServerConfig.DESCRIPTION_MAX_PLAYERS.getValue().orElse(20);

  /**
   * ctor.
   *
   * @param server the server.
   */
  public PlayerList(@NotNull final ShirukaServer server) {
    this.server = server;
  }

  /**
   * obtains the online players.
   *
   * @return online players.
   */
  @NotNull
  public Collection<? extends ShirukaPlayer> getPlayers() {
    synchronized (this.players) {
      return Collections.unmodifiableList(this.players);
    }
  }

  /**
   * runs when the player just created.
   * <p>
   * bunch of packets related to starting the game for the player will send here.
   *
   * @param player the player to initialize.
   * @param connection the connection to initialize.
   */
  public void initialize(@NotNull final ShirukaPlayer player, @NotNull final PlayerConnection connection) {
    final var xboxUniqueId = player.getXboxUniqueId();
    final var server = connection.getServer();
    final var pendingPlayer = this.pendingPlayers.get(xboxUniqueId);
    if (pendingPlayer != null) {
      this.pendingPlayers.remove(xboxUniqueId);
      pendingPlayer.getConnection().disconnect(TranslatedTexts.ALREADY_LOGGED_IN_REASON);
    }
    this.players.stream()
      .filter(oldPlayer -> oldPlayer.getXboxUniqueId().equals(player.getXboxUniqueId()))
      .forEach(old -> {
        // @todo #1:15m Force save old player data.
        old.kick(LoginResultEvent.LoginResult.KICK_OTHER, TranslatedTexts.ALREADY_LOGGED_IN_REASON);
      });
    player.isRealPlayer = true;
    final var event = Shiruka.getEventManager().playerLogin(player);
    if (player.isNameBanned()) {
      final var optional = player.getNameBanEntry();
      if (optional.isPresent()) {
        final var message = TranslatedText.get("shiruka.player.banned");
        final var entry = optional.get();
        entry.getExpiration().ifPresent(date ->
          message.addSiblings(TranslatedText.get("shiruka.player.banned.expiration", date)));
        event.disallow(LoginResultEvent.LoginResult.KICK_BANNED, message);
      }
    }
    if (player.isIpBanned()) {
      final var optional = player.getIpBanEntry();
      if (optional.isPresent()) {
        final var message = TranslatedText.get("shiruka.player.banned");
        final var entry = optional.get();
        entry.getExpiration().ifPresent(date ->
          message.addSiblings(TranslatedText.get("shiruka.player.banned.expiration", date)));
        event.disallow(LoginResultEvent.LoginResult.KICK_BANNED, message);
      }
    }
    if (!player.canBypassWhitelist()) {
      event.disallow(LoginResultEvent.LoginResult.KICK_WHITELIST, TranslatedTexts.WHITELIST_ON_REASON);
    }
    if (server.getOnlinePlayers().size() >= server.getMaxPlayers() && !player.canBypassPlayerLimit()) {
      event.disallow(LoginResultEvent.LoginResult.KICK_FULL, TranslatedTexts.SERVER_FULL_REASON);
    }
    if (event.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
      player.kick(event.getLoginResult(), event.getKickMessage().orElse(null));
      return;
    }
    if (this.tryToLogin(player, connection)) {
      return;
    }
    server.getTick().lastPingTime = 0L;
    throw new UnsupportedOperationException(" @todo #1:10m Implement PlayerList#initialize.");
  }

  /**
   * tries the given {@code player} to login.
   *
   * @param player the player to try.
   * @param connection the connection to try.
   *
   * @return {@code true} if the given {@code player} logged in successfully.
   */
  private boolean tryToLogin(@NotNull final ShirukaPlayer player, @NotNull final PlayerConnection connection) {
    final var xboxUniqueId = player.getXboxUniqueId();
    final var server = connection.getServer();
    return true;
  }
}
