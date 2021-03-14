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

package net.shiruka.shiruka.entity.entities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.ChainData;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.base.Location;
import net.shiruka.api.entity.Player;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.base.LoginData;
import net.shiruka.shiruka.base.OpEntry;
import net.shiruka.shiruka.config.OpsConfig;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.Tag;
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
  private static final Map<Plugin, WeakReference<Plugin>> PLUGIN_WEAK_REFERENCES = new WeakHashMap<>();

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
  private final Map<UUID, Set<WeakReference<Plugin>>> hiddenPlayers = new Object2ObjectOpenHashMap<>();

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
   * the is real player.
   */
  public boolean isRealPlayer;

  /**
   * the login time.
   */
  public long loginTime;

  /**
   * the data file.
   */
  @Nullable
  private File dataFile;

  /**
   * the first played.
   */
  private long firstPlayed = 0L;

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
   * the player file.
   */
  @Nullable
  private File playerFile;

  /**
   * ctor.
   *
   * @param connection the connection.
   * @param loginData the login data.
   * @param profile the profile.
   */
  public ShirukaPlayer(@NotNull final PlayerConnection connection, @NotNull final LoginData loginData,
                       @NotNull final GameProfile profile) {
    super(profile);
    this.connection = connection;
    this.loginData = loginData;
  }

  @Nullable
  private static WeakReference<Plugin> getPluginWeakReference(@Nullable final Plugin plugin) {
    return plugin == null
      ? null
      : ShirukaPlayer.PLUGIN_WEAK_REFERENCES.computeIfAbsent(plugin, WeakReference::new);
  }

  /**
   * checks if the player can bypass the player limit.
   *
   * @return {@code true} if the player can bypass the player limit.
   */
  public boolean canBypassPlayerLimit() {
    return this.isOp() && !this.getOpEntry().isBypassesPlayerLimit();
  }

  /**
   * checks if the player can bypass the whitelist and join the server.
   *
   * @return {@code true} if the player can bypass the whitelist and join the server.
   */
  public boolean canBypassWhitelist() {
    return this.isOp() ||
      !ServerConfig.whiteList ||
      this.isWhitelisted();
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
  public ChainData getChainData() {
    return Objects.requireNonNull(this.loginData, "player did not initialize").getChainData();
  }

  @Override
  public long getPing() {
    return this.ping.get();
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
    hidingPlugins = new ObjectOpenHashSet<>();
    hidingPlugins.add(ShirukaPlayer.getPluginWeakReference(plugin));
    this.hiddenPlayers.put(player.getUniqueId(), hidingPlugins);
//    this.unregisterPlayer(player);
  }

  @Override
  public boolean kick(@NotNull final LoginResultEvent.LoginResult reason, @Nullable final Text reasonString,
                      final boolean isAdmin) {
    final var done = Shiruka.getEventManager().playerKick(this, reason).callEvent();
    if (done) {
      this.connection.disconnect(reasonString);
    }
    return done;
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
//    this.registerPlayer(player);
  }

  /**
   * creates a default compound tag when the player joins the server for first time.
   *
   * @return default compound tag.
   */
  @NotNull
  public CompoundTag createDefaultTag() {
    final var defaultWorld = this.connection.getServer().getDefaultWorld();
    final var spawn = defaultWorld.getSpawn();
    final var tag = Tag.createCompound();
    tag.setLong("first-played", System.currentTimeMillis() / 1000L);
    tag.setLong("last-played", System.currentTimeMillis() / 1000L);
    tag.setList("Pos", List.of(
      Tag.createString(String.valueOf(spawn.getX())),
      Tag.createString(String.valueOf(spawn.getY())),
      Tag.createString(String.valueOf(spawn.getZ()))));
    final var worldUniqueId = defaultWorld.getUniqueId();
    tag.setLong("WorldUUIDMost", worldUniqueId.getMostSignificantBits());
    tag.setLong("WorldUUIDLeast", worldUniqueId.getLeastSignificantBits());
    return tag;
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

  /**
   * sets the first played.
   *
   * @param firstPlayed the first played to set.
   */
  public void setFirstPlayed(final long firstPlayed) {
    this.firstPlayed = firstPlayed;
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
  public boolean isOnline() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaPlayer#isOnline.");
  }

  @Override
  public boolean isWhitelisted() {
    return Shiruka.getServer().isInWhitelist(this);
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

  /**
   * obtains the {@link #dataFile}.
   *
   * @return data file.
   */
  @NotNull
  public File getDataFile() {
    if (this.dataFile == null) {
      this.dataFile = new File(ShirukaMain.HOME_PATH + "/players/" + this.getUniqueId() + ".dat");
    }
    return this.dataFile;
  }

  /**
   * obtains the player file.
   *
   * @return player file.
   */
  @NotNull
  public File getPlayerFile() {
    return this.getPlayerFile(false);
  }

  /**
   * obtains the player file.
   * if it's not exist and {@code create} is true, creates the player file..
   *
   * @param create the create to get.
   *
   * @return player file.
   */
  @NotNull
  public File getPlayerFile(final boolean create) {
    if (this.playerFile == null) {
      this.playerFile = new File(ShirukaMain.players, this.getUniqueId() + ".dat");
    }
    if (!create) {
      return this.playerFile;
    }
    final var path = this.playerFile.toPath();
    if (!Files.notExists(path)) {
      return this.playerFile;
    }
    try {
      Files.createFile(path);
    } catch (final IOException e) {
      this.connection.getServer().getLogger().error("Failed to create player data file for {}",
        this.getName().asString());
    }
    return this.playerFile;
  }

  @NotNull
  @Override
  public UUID getUniqueId() {
    return this.getProfile().getUniqueId();
  }

  @Override
  public int hashCode() {
    if (this.hash == 0 || this.hash == 485) {
      this.hash = 97 * 5 + this.getUniqueId().hashCode();
    }
    return this.hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Player)) {
      return false;
    }
    final var other = (Player) obj;
    return this.getUniqueId().equals(other.getUniqueId()) &&
      this.getEntityId() == other.getEntityId();
  }

  @Override
  public String toString() {
    return "ShirukaPlayer{" + "name=" + this.getName().asString() + '}';
  }

  @Override
  public boolean isOp() {
    return OpsConfig.contains(this.getUniqueId().toString());
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
  public void sendMessage(@NotNull final TranslatedText message) {
    final var translated = message.translate(this);
    if (translated.isPresent()) {
      this.sendMessage(translated.get());
    } else {
      this.sendMessage(message.asString());
    }
  }

  @Override
  public void sendMessage(@NotNull final String message) {
  }

  /**
   * teleports the player to the spawn of the given {@code world}.
   *
   * @param world the world to spawn.
   */
  public void teleportToSpawn(@Nullable final World world) {
    if (world == null) {
      return;
    }
  }

  @Override
  public void tick() {
  }

  @Override
  public void spawnIn(@Nullable final World world) {
    super.spawnIn(world);
    if (world == null) {
      return;
    }
  }

  /**
   * obtains the {@link #opEntry}.
   *
   * @return op entry.
   */
  @NotNull
  private OpEntry getOpEntry() {
    if (this.opEntry == null) {
      this.opEntry = new OpEntry(ServerConfig.opsPassPlayerLimit, this.getProfile());
    }
    return this.opEntry;
  }
}
