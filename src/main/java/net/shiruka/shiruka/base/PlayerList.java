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
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.BanList;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.ban.IpBanList;
import net.shiruka.shiruka.ban.ProfileBanList;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.config.UserCacheConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayer;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.Tag;
import net.shiruka.shiruka.text.TranslatedTexts;
import net.shiruka.shiruka.world.DimensionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents player list.
 */
@RequiredArgsConstructor
public final class PlayerList {

  /**
   * the ip ban list.
   */
  public final BanList ipBanList = new IpBanList();

  /**
   * the pending players.
   */
  public final Map<UUID, ShirukaPlayer> pendingPlayers = new Object2ObjectOpenHashMap<>();

  /**
   * the players.
   */
  public final List<ShirukaPlayer> players = new CopyOnWriteArrayList<>();

  /**
   * the players by name.
   */
  public final Map<String, ShirukaPlayer> playersByName = new Object2ObjectOpenHashMap<>();

  /**
   * the players by unique id.
   */
  public final Map<UUID, ShirukaPlayer> playersByUniqueId = new Object2ObjectOpenHashMap<>();

  /**
   * the players by xbox unique id.
   */
  public final Map<String, ShirukaPlayer> playersByXboxUniqueId = new Object2ObjectOpenHashMap<>();

  /**
   * the profile ban list.
   */
  public final BanList profileBanList = new ProfileBanList();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * gets the active player from {@link #playersByUniqueId} or {@link #pendingPlayers}.
   *
   * @param uniqueId the unique id to get.
   *
   * @return active player instance.
   */
  @Nullable
  public ShirukaPlayer getActivePlayer(@NotNull final UUID uniqueId) {
    final var player = this.playersByUniqueId.get(uniqueId);
    return player != null ? player : this.pendingPlayers.get(uniqueId);
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
   */
  public void initialize(@NotNull final ShirukaPlayer player) {
    final var uniqueId = player.getUniqueId();
    final var pendingPlayer = this.pendingPlayers.get(uniqueId);
    if (pendingPlayer != null) {
      this.pendingPlayers.remove(uniqueId);
      pendingPlayer.getConnection().disconnect(TranslatedTexts.ALREADY_LOGGED_IN_REASON);
    }
    this.players.stream()
      .filter(oldPlayer -> oldPlayer.getUniqueId().equals(player.getUniqueId()))
      .forEach(old -> {
        // @todo #1:15m Force save old player data.
        old.kick(LoginResultEvent.LoginResult.KICK_OTHER, TranslatedTexts.ALREADY_LOGGED_IN_REASON);
      });
    final var event = Shiruka.getEventManager().playerLogin(player);
    if (player.isNameBanned()) {
      final var optional = player.getNameBanEntry();
      if (optional.isPresent()) {
        final var entry = optional.get();
        final var message = TranslatedText.get("shiruka.player.banned", entry.getReason());
        entry.getExpiration().ifPresent(date ->
          message.addSiblings(TranslatedText.get("shiruka.player.banned.expiration", date)));
        event.disallow(LoginResultEvent.LoginResult.KICK_BANNED, message);
      }
    }
    if (player.isIpBanned()) {
      final var optional = player.getIpBanEntry();
      if (optional.isPresent()) {
        final var entry = optional.get();
        final var message = TranslatedText.get("shiruka.player.banned", entry.getReason());
        entry.getExpiration().ifPresent(date ->
          message.addSiblings(TranslatedText.get("shiruka.player.banned.expiration", date)));
        event.disallow(LoginResultEvent.LoginResult.KICK_BANNED, message);
      }
    }
    if (!player.canBypassWhitelist()) {
      event.disallow(LoginResultEvent.LoginResult.KICK_WHITELIST, TranslatedTexts.WHITELIST_ON_REASON);
    }
    if (this.server.getOnlinePlayers().size() >= this.server.getMaxPlayers() && !player.canBypassPlayerLimit()) {
      event.disallow(LoginResultEvent.LoginResult.KICK_FULL, TranslatedTexts.SERVER_FULL_REASON);
    }
    if (event.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
      player.kick(event.getLoginResult(), event.getKickMessage().orElse(null));
      return;
    }
    this.tryToLogin(player);
  }

  /**
   * loads the given {@code player}'s compound tag from the players folder.
   *
   * @param player the player to load.
   * @param create the create to load.
   *
   * @return loaded compound tag instance.
   */
  @Contract("_, true -> !null")
  private CompoundTag loadPlayerCompound(@NotNull final ShirukaPlayer player, final boolean create) {
    CompoundTag tag = null;
    final var file = player.getPlayerFile();
    try {
      var tempFile = file;
      boolean wrongFile = false;
      if (ServerConfig.onlineMode && !file.exists()) {
        tempFile = new File(ShirukaMain.players,
          UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().asString()).getBytes(StandardCharsets.UTF_8)) + ".dat");
        if (tempFile.exists()) {
          wrongFile = true;
          this.server.getLogger().warn("Using offline mode UUID file for player {} as it is the only copy we can find.",
            player.getName().asString());
        }
      }
      if (tempFile.exists() && tempFile.isFile()) {
        tag = Tag.createGZIPReader(new FileInputStream(tempFile)).readCompoundTag();
      }
      if (wrongFile) {
        tempFile.renameTo(new File(tempFile.getPath() + ".offline-read"));
      }
    } catch (final Exception e) {
      this.server.getLogger().error("Failed to load player data for {}", player.getName().asString());
    }
    if (tag == null && create) {
      tag = player.createDefaultTag();
    }
    final var modified = player.getPlayerFile().lastModified();
    if (modified < player.getFirstPlayed()) {
      player.setFirstPlayed(modified);
    }
    player.load(tag);
    return tag;
  }

  /**
   * spawn the given {@code player} in the server.
   *
   * @param player the player to login.
   */
  private void login(@NotNull final ShirukaPlayer player) {
    player.isRealPlayer = true;
    final var oldPending = this.pendingPlayers.put(player.getUniqueId(), player);
    if (oldPending != null) {
      oldPending.getConnection().disconnect(TranslatedTexts.ALREADY_LOGGED_IN_REASON);
    }
    player.loginTime = System.currentTimeMillis();
    final var optional = UserCacheConfig.getProfileByUniqueId(player.getUniqueId());
    var lastKnownName = optional.isPresent()
      ? optional.get().getName().asString()
      : player.getName().asString();
    UserCacheConfig.addProfile(player.getProfile(), true);
    final var tag = this.loadPlayerCompound(player, true);
    if (tag.containsKey("shiruka")) {
      final var shiruka = tag.getCompoundTag("shiruka").orElseThrow();
      lastKnownName = shiruka.hasKeyOfType("lastKnownName", 8)
        ? shiruka.getString("lastKnownName").orElseThrow()
        : lastKnownName;
    }
    final var resourced = DimensionManager.fromTag(tag);
    final var world = this.server.getWorld(resourced).orElseGet(this.server::getDefaultWorld);
    player.spawnIn(world);
    // @todo #1:1m Continue to development here.
    this.server.getTick().setLastPingTime(0L);
  }

  /**
   * tries login for the given {@code player}.
   *
   * @param player the player to try.
   */
  private void tryToLogin(@NotNull final ShirukaPlayer player) {
    final var uniqueId = player.getUniqueId();
    if (this.pendingPlayers.containsKey(uniqueId) ||
      this.playersByUniqueId.containsKey(uniqueId)) {
      player.getConnection().getLoginListener().setWantsToJoin(player);
      return;
    }
    this.login(player);
  }
}
