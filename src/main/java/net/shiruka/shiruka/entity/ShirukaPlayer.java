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

package net.shiruka.shiruka.entity;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.base.Location;
import net.shiruka.api.entity.Player;
import net.shiruka.api.events.ChainDataEvent;
import net.shiruka.api.events.KickEvent;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.base.OpEntry;
import net.shiruka.shiruka.config.OpsConfig;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.event.LoginData;
import net.shiruka.shiruka.network.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Player}.
 */
public final class ShirukaPlayer extends ShirukaHumanEntity implements Player {

  /**
   * the plugin weak references.
   */
  private static final WeakHashMap<Plugin, WeakReference<Plugin>> PLUGIN_WEAK_REFERENCES = new WeakHashMap<>();

  /**
   * the viewable entities.
   */
  protected final Set<ShirukaEntity> viewableEntities = new CopyOnWriteArraySet<>();

  /**
   * the connection.
   */
  @NotNull
  private final PlayerConnection connection;

  /**
   * the hidden players.
   */
  private final Map<UUID, Set<WeakReference<Plugin>>> hiddenPlayers = new HashMap<>();

  /**
   * the login data.
   */
  @NotNull
  private final LoginData loginData;

  /**
   * the ping.
   */
  private final AtomicInteger ping = new AtomicInteger();

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * the hash.
   */
  private int hash = 0;

  /**
   * the lazy initiated op entry instance.
   */
  @Nullable
  private OpEntry opEntry;

  /**
   * ctor.
   *
   * @param connection the connection.
   * @param loginData the login data.
   * @param profile the profile.
   */
  public ShirukaPlayer(@NotNull final PlayerConnection connection, @NotNull final LoginData loginData,
                       @NotNull final GameProfile profile) {
    this.connection = connection;
    this.loginData = loginData;
    this.profile = profile;
  }

  @Nullable
  private static WeakReference<Plugin> getPluginWeakReference(@Nullable final Plugin plugin) {
    return plugin == null
      ? null
      : ShirukaPlayer.PLUGIN_WEAK_REFERENCES.computeIfAbsent(plugin, WeakReference::new);
  }

  @Override
  public boolean addViewer(@NotNull final Player player) {
    if (this.equals(player)) {
      return false;
    }
    if (!super.addViewer(player)) {
      return false;
    }
    this.showPlayer(null, player);
    return true;
  }

  @Override
  public boolean removeViewer(final @NotNull Player player) {
    if (this.equals(player) || !(player instanceof ShirukaPlayer)) {
      return false;
    }
    final var result = super.removeViewer(player);
    final var viewerConnection = ((ShirukaPlayer) player).getConnection();
//    viewerConnection.sendPacket(getRemovePlayerToList());
//    if (this.getTeam() != null && this.getTeam().getMembers().size() == 1) {
//      viewerConnection.sendPacket(this.getTeam().createTeamDestructionPacket());
//    }
    return result;
  }

  @Override
  public void tick() {
    this.connection.handleQueuedPackets();
  }

  @Override
  public boolean canSee(@NotNull final Player player) {
    return !this.hiddenPlayers.containsKey(player.getUniqueId());
  }

  @NotNull
  @Override
  public InetSocketAddress getAddress() {
    return this.connection.getConnection().getAddress();
  }

  @NotNull
  @Override
  public ChainDataEvent.ChainData getChainData() {
    return Objects.requireNonNull(this.loginData, "not initialized player").chainData();
  }

  @Override
  public long getPing() {
    return this.ping.get();
  }

  @NotNull
  @Override
  public GameProfile getProfile() {
    return Objects.requireNonNull(this.profile, "not initialized player");
  }

  @Override
  public void hidePlayer(@Nullable final Plugin plugin, @NotNull final Player player) {
    if (this.equals(player)) {
      return;
    }
    var hidingPlugins = this.hiddenPlayers.get(player.getUniqueId());
    if (hidingPlugins != null) {
      hidingPlugins.add(ShirukaPlayer.getPluginWeakReference(plugin));
      return;
    }
    hidingPlugins = new HashSet<>();
    hidingPlugins.add(ShirukaPlayer.getPluginWeakReference(plugin));
    this.hiddenPlayers.put(player.getUniqueId(), hidingPlugins);
    this.unregisterPlayer(player);
  }

  @Override
  public boolean kick(@NotNull final KickEvent.Reason reason, @Nullable final Text reasonString,
                      final boolean isAdmin) {
    return Shiruka.getEventManager().playerKick(this, reason).callEvent();
  }

  @Override
  public void showPlayer(@Nullable final Plugin plugin, @NotNull final Player player) {
    if (this.equals(player)) {
      return;
    }
    final var hidingPlugins = this.hiddenPlayers.get(player.getUniqueId());
    if (hidingPlugins == null) {
      return;
    }
    hidingPlugins.remove(ShirukaPlayer.getPluginWeakReference(plugin));
    if (!hidingPlugins.isEmpty()) {
      return;
    }
    this.hiddenPlayers.remove(player.getUniqueId());
    this.registerPlayer(player);
  }

  @Nullable
  @Override
  public Location getBedSpawnLocation() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#getBedSpawnLocation.");
  }

  @Override
  public long getFirstPlayed() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#getFirstPlayed.");
  }

  @Override
  public long getLastLogin() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#getLastLogin.");
  }

  @Override
  public long getLastSeen() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#getLastSeen.");
  }

  @NotNull
  @Override
  public Optional<Player> getPlayer() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#getPlayer.");
  }

  @Override
  public boolean hasPlayedBefore() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#hasPlayedBefore.");
  }

  @Override
  public boolean isBanned() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#isBanned.");
  }

  @Override
  public boolean isOnline() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#isOnline.");
  }

  @Override
  public boolean isWhitelisted() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#isWhitelisted.");
  }

  @Override
  public void setWhitelisted(final boolean value) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#setWhitelisted.");
  }

  /**
   * obtains the connection.
   *
   * @return connection.
   */
  @NotNull
  public PlayerConnection getConnection() {
    return this.connection;
  }

  @Override
  public int hashCode() {
    if (this.hash == 0 || this.hash == 485) {
      this.hash = 97 * 5 + this.getXboxUniqueId().hashCode();
    }
    return this.hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Player)) {
      return false;
    }
    final var other = (Player) obj;
    return this.getXboxUniqueId().equals(other.getXboxUniqueId()) &&
      this.getEntityId() == other.getEntityId();
  }

  @Override
  public String toString() {
    return "ShirukaPlayer{" + "name=" + this.getName().asString() + '}';
  }

  /**
   * runs when the player just created.
   * <p>
   * bunch of packets related to starting the game for the player will send here.
   */
  public void initialize() {

  }

  @Override
  public boolean isOp() {
    return OpsConfig.getInstance().getConfiguration().contains(this.profile.getXboxUniqueId());
  }

  @Override
  public void setOp(final boolean value) {
    if (value == this.isOp()) {
      return;
    }
    if (value) {
      OpsConfig.addOp(this.getOpEntry());
    } else {
      OpsConfig.removeOp(this.getOpEntry());
    }
    this.permissible.recalculatePermissions();
  }

  @Override
  public void sendMessage(@NotNull final String message) {
  }

  /**
   * creates a new {@link OpEntry} instance.
   *
   * @return a newly created op entry instance.
   */
  @NotNull
  private OpEntry getOpEntry() {
    if (this.opEntry == null) {
      this.opEntry = new OpEntry(this.profile, ServerConfig.OPS_PASS_PLAYER_LIMIT.getValue().orElse(false));
    }
    return this.opEntry;
  }

  private void registerPlayer(@NotNull final Player player) {
//    final var tracker = world.getChunkProvider().playerChunkMap;
//    this.connection.sendPacket(new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER, player));
//    final var entry = tracker.trackedEntities.get(player.getEntityId());
//    if (entry != null && !entry.trackedPlayers.contains(this)) {
//      entry.updatePlayer(this);
//    }
  }

  private void unregisterPlayer(@NotNull final Player player) {
//    if (!(player instanceof ShirukaPlayer)) {
//      return;
//    }
//    final var shirukaPlayer = (ShirukaPlayer) player;
//    final var entry = world.getChunkProvider().playerChunkMap.trackedEntities.get(shirukaPlayer.getEntityId());
//    if (entry != null) {
//      entry.clear(this);
//    }
//    if (shirukaPlayer.sentListPacket) {
//      this.connection.sendPacket(new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER, shirukaPlayer));
//    }
  }
}
