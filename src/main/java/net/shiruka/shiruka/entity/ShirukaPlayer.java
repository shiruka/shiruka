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

import com.google.common.base.Preconditions;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.base.Location;
import net.shiruka.api.entity.Player;
import net.shiruka.api.events.KickEvent;
import net.shiruka.api.events.LoginDataEvent;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionAttachment;
import net.shiruka.api.permission.PermissionAttachmentInfo;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.ChatColor;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.event.LoginData;
import net.shiruka.shiruka.event.SimpleChainData;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Player}.
 *
 * @todo #1:60m Implement ShirukaPlayer's methods.
 */
public final class ShirukaPlayer extends ShirukaEntity implements Player, PacketHandler {

  /**
   * the disconnected with no reason.
   */
  private static final Text DISCONNECTED_NO_REASON = TranslatedText.get("disconnect.disconnected");

  /**
   * the name pattern to check client's usernames.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z\\s\\d_]{3,16}+$");

  /**
   * the connection.
   */
  @NotNull
  private final RakNetClientPeer connection;

  /**
   * the ping.
   */
  private final AtomicInteger ping = new AtomicInteger();

  /**
   * the blob cache support.
   */
  private boolean blobCacheSupport;

  /**
   * the login data.
   */
  @Nullable
  private LoginData loginData;

  /**
   * the profile.
   */
  @Nullable
  private GameProfile profile;

  /**
   * ctor.
   *
   * @param connection the connection.
   */
  public ShirukaPlayer(@NotNull final RakNetClientPeer connection) {
    this.connection = connection;
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
  public void clientCacheStatusPacket(@NotNull final ClientCacheStatusPacket packet) {
    this.blobCacheSupport = packet.isBlobCacheSupport();
  }

  @Override
  public void loginPacket(@NotNull final LoginPacket packet) {
    // @todo #1:60m Add Server_To_Client_Handshake Client_To_Server_Handshake packets to request encryption key.
    final var protocolVersion = packet.getProtocolVersion();
    final var encodedChainData = packet.getChainData().toString();
    final var encodedSkinData = packet.getSkinData().toString();
    if (protocolVersion < ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      final var playStatusPacket = new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
      this.sendPacket(playStatusPacket);
      return;
    }
    if (protocolVersion > ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      final var playStatusPacket = new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
      this.sendPacket(playStatusPacket);
      return;
    }
    Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
      final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
      Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
        Languages.addLoadedLanguage(chainData.getLanguageCode());
        if (!chainData.getXboxAuthed() && ServerConfig.ONLINE_MODE.getValue().orElse(false)) {
          this.disconnect(TranslatedText.get("disconnectionScreen.notAuthenticated"));
          return;
        }
        final var username = chainData.getUsername();
        final var matcher = ShirukaPlayer.NAME_PATTERN.matcher(username);
        if (!matcher.matches() ||
          username.equalsIgnoreCase("rcon") ||
          username.equalsIgnoreCase("console")) {
          this.disconnect(TranslatedText.get("disconnectionScreen.invalidName"));
          return;
        }
        if (!chainData.getSkin().isValid()) {
          this.disconnect(TranslatedText.get("disconnectionScreen.invalidSkin"));
          return;
        }
        final var loginData = new LoginData(chainData, this, () -> ChatColor.clean(username));
        final var preLogin = Shiruka.getEventManager().playerPreLogin(chainData, () -> "Some reason.");
        preLogin.callEvent();
        if (preLogin.isCancelled()) {
          this.disconnect(preLogin.getKickMessage().orElse(null));
          return;
        }
        final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(chainData);
        loginData.setAsyncLogin(asyncLogin);
        loginData.setTask(Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
          asyncLogin.callEvent();
          Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
            if (loginData.shouldLogin()) {
              loginData.initializePlayer();
            }
          });
        }));
        this.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_SUCCESS));
        final var packInfo = Shiruka.getPackManager().getPackInfo();
        if (packInfo instanceof ShirukaPacket) {
          this.sendPacket((ShirukaPacket) packInfo);
        }
      });
    });
  }

  /**
   * disconnects the connection.
   */
  public void disconnect() {
    this.disconnect((Text) null);
  }

  /**
   * disconnects the connection.
   *
   * @param reason the reason to disconnect.
   */
  public void disconnect(@Nullable final Text reason) {
    Preconditions.checkState(this.connection.isConnected(), "not connected");
    final var message = this.translate0(reason, ShirukaPlayer.DISCONNECTED_NO_REASON).asString();
    final var disconnectPacket = new DisconnectPacket(message, reason == null);
    this.sendPacket(disconnectPacket);
  }

  /**
   * disconnects the connection.
   *
   * @param reason the reason to disconnect.
   */
  public void disconnect(@Nullable final String reason) {
    if (reason == null) {
      this.disconnect();
    } else {
      this.disconnect(() -> reason);
    }
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
  public boolean kick(@NotNull final KickEvent.Reason reason, @Nullable final Text reasonString,
                      final boolean isAdmin) {
    final var event = Shiruka.getEventManager().playerKick(this, reason);
    event.callEvent();
    return !event.isCancelled();
  }

  /**
   * obtains the connection.
   *
   * @return connection.
   */
  @NotNull
  public RakNetClientPeer getConnection() {
    return this.connection;
  }

  /**
   * obtains the login data.
   *
   * @return login data.
   */
  @Nullable
  public LoginData getLoginData() {
    return this.loginData;
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
  }

  @Override
  public boolean isOp() {
    return false;
  }

  @Override
  public void setOp(final boolean value) {
  }

  /**
   * runs when the player pass the login packet.
   *
   * @param loginData the login data to set.
   * @param profile the profile to set.
   */
  public void onLogin(@Nullable final LoginData loginData, @NotNull final GameProfile profile) {
    this.loginData = loginData;
    this.profile = profile;
  }

  @Override
  public void sendMessage(@NotNull final String message) {
  }

  /**
   * sends the given {@code packet} to {@link #connection}.
   *
   * @param packet the packet to send.
   */
  public void sendPacket(@NotNull final ShirukaPacket packet) {
  }

  /**
   * the internal simple translation..
   *
   * @param reason the reason to translate.
   * @param fallback the fallback to translate.
   *
   * @return translated string..
   */
  @NotNull
  private Text translate0(@Nullable final Text reason, @NotNull final Text fallback) {
    final Text finalReason;
    if (reason == null) {
      finalReason = fallback;
    } else if (reason instanceof TranslatedText) {
      finalReason = () -> ((TranslatedText) reason).translate(this).orElse(reason.asString());
    } else {
      finalReason = reason;
    }
    return finalReason;
  }
}
