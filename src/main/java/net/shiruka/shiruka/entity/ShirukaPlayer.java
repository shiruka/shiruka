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
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionAttachment;
import net.shiruka.api.permission.PermissionAttachmentInfo;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.event.LoginData;
import net.shiruka.shiruka.network.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Player}.
 *
 * @todo #1:60m Implement ShirukaPlayer's methods.
 */
public final class ShirukaPlayer extends ShirukaEntity implements Player {

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

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                            final boolean value) {
    return null;
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin) {
    return null;
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                                      final boolean value, final long ticks) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, final long ticks) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    return null;
  }

  @Override
  public boolean hasPermission(@NotNull final String name) {
    return false;
  }

  @Override
  public boolean hasPermission(@NotNull final Permission perm) {
    return false;
  }

  @Override
  public boolean isPermissionSet(@NotNull final String name) {
    return false;
  }

  @Override
  public boolean isPermissionSet(@NotNull final Permission perm) {
    return false;
  }

  @Override
  public void recalculatePermissions() {
  }

  @Override
  public void removeAttachment(@NotNull final PermissionAttachment attachment) {
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
    return Shiruka.getEventManager().playerKick(this, reason)
      .callEvent();
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

  @Override
  public boolean isOp() {
    return false;
  }

  @Override
  public void setOp(final boolean value) {
  }

  @Override
  public void sendMessage(@NotNull final String message) {
  }

  private void registerPlayer(@NotNull final Player player) {
//    var tracker = world.getChunkProvider().playerChunkMap;
//    connection.sendPacket(new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER, player));
//    var entry = tracker.trackedEntities.get(player.getEntityId());
//    if (entry != null && !entry.trackedPlayers.contains(this)) {
//      entry.updatePlayer(this);
//    }
  }

  private void unregisterPlayer(@NotNull final Player player) {
//    if (!(player instanceof ShirukaPlayer)) {
//      return;
//    }
//    final var shirukaPlayer = (ShirukaPlayer) player;
//    var entry = world.getChunkProvider().playerChunkMap.trackedEntities.get(shirukaPlayer.getEntityId());
//    if (entry != null) {
//      entry.clear(this);
//    }
//    if (shirukaPlayer.sentListPacket) {
//      connection.sendPacket(new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER, shirukaPlayer));
//    }
  }
}
