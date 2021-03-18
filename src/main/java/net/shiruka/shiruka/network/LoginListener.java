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

import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.text.ChatColor;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.base.LoginData;
import net.shiruka.shiruka.base.SimpleChainData;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayer;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import net.shiruka.shiruka.network.packets.ResourcePackDataInfoPacket;
import net.shiruka.shiruka.network.packets.ResourcePackResponsePacket;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents login listener.
 */
@RequiredArgsConstructor
public final class LoginListener implements PacketHandler {

  /**
   * the name pattern to check client's usernames.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z\\s\\d_]{3,16}+$");

  /**
   * the connection.
   */
  @NotNull
  @Getter
  private final PlayerConnection connection;

  /**
   * the latest login packet.
   */
  @Nullable
  private LoginPacket latestLoginPacket;

  /**
   * the latest resource packet.
   */
  @Nullable
  private ResourcePackResponsePacket latestResourcePacket;

  /**
   * the login data.
   */
  @Nullable
  private LoginData loginData;

  /**
   * the login timeout counter.
   */
  private int loginTimeoutCounter;

  /**
   * the wants to join.
   */
  @Nullable
  @Setter
  private ShirukaPlayer wantsToJoin;

  @Override
  public void login(@NotNull final LoginPacket packet) {
    this.latestLoginPacket = packet;
  }

  @Override
  public void resourcePackResponse(@NotNull final ResourcePackResponsePacket packet) {
    this.latestResourcePacket = packet;
  }

  @Override
  public void tick() {
    if (!Shiruka.getServer().isRunning()) {
      this.connection.disconnect(TranslatedTexts.RESTART_REASON);
      return;
    }
    if (this.wantsToJoin == null) {
      if (this.latestLoginPacket != null) {
        this.loginPacket0(this.latestLoginPacket);
      }
      if (this.latestResourcePacket != null) {
        this.resourcePackResponsePacket0(this.latestResourcePacket);
      }
    } else if (this.connection.getProfile() != null) {
      final var pending = this.connection.getServer().playerList
        .getActivePlayer(this.connection.getProfile().getUniqueId());
      if (pending == null) {
        this.wantsToJoin = null;
      }
    }
    if (this.loginTimeoutCounter++ >= 600) {
      this.connection.disconnect(TranslatedTexts.SLOW_LOGIN_REASON);
    }
  }

  /**
   * handles the login packet.
   *
   * @param packet the packet to handle.
   *
   * @todo #1:60m Add ServerToClientHandshake ClientToServerHandshake packets to request encryption key.
   */
  private void loginPacket0(@NotNull final LoginPacket packet) {
    this.latestLoginPacket = null;
    if (Shiruka.isStopping()) {
      this.connection.disconnect(TranslatedTexts.RESTART_REASON);
      return;
    }
    final var protocolVersion = packet.getProtocolVersion();
    final var encodedChainData = packet.getChainData().toString();
    final var encodedSkinData = packet.getSkinData().toString();
    if (protocolVersion < ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      this.connection.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD));
      return;
    }
    if (protocolVersion > ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      this.connection.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD));
      return;
    }
    Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
      final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
      Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
        Languages.addLoadedLanguage(chainData.getLanguageCode());
        if (!chainData.isXboxAuthed() && ServerConfig.onlineMode) {
          this.connection.disconnect(TranslatedTexts.NOT_AUTHENTICATED_REASON);
          return;
        }
        final var username = chainData.getUsername();
        final var matcher = LoginListener.NAME_PATTERN.matcher(username);
        if (!matcher.matches() ||
          username.equalsIgnoreCase("rcon") ||
          username.equalsIgnoreCase("console")) {
          this.connection.disconnect(TranslatedTexts.INVALID_NAME_REASON);
          return;
        }
        if (!chainData.getSkin().isValid()) {
          this.connection.disconnect(TranslatedTexts.INVALID_SKIN_REASON);
          return;
        }
        this.connection.setProfile(new GameProfile(
          () -> ChatColor.clean(username),
          chainData.getUniqueId(),
          chainData.getXboxUniqueId()));
        assert this.connection.getProfile() != null;
        this.loginData = new LoginData(chainData, this.connection, this.connection.getProfile());
        final var preLogin = Shiruka.getEventManager().playerPreLogin(chainData);
        preLogin.callEvent();
        if (preLogin.isCancelled()) {
          this.connection.disconnect(preLogin.getKickMessage().orElse(null));
          return;
        }
        final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(chainData);
        this.loginData.setAsyncLogin(asyncLogin);
        this.loginData.setTask(Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
          asyncLogin.callEvent();
          if (asyncLogin.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
            Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () ->
              this.connection.disconnect(asyncLogin.getKickMessage().orElse(null)));
            return;
          }
          Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
            if (this.loginData.shouldLogin()) {
              this.loginData.initialize();
            }
          });
        }));
        this.connection.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_SUCCESS));
        final var packInfo = Shiruka.getPackManager().getPackInfo();
        if (packInfo instanceof ShirukaPacket) {
          this.connection.sendPacket((ShirukaPacket) packInfo);
        }
      });
    });
  }

  /**
   * handles the resource pack response packet.
   *
   * @param packet the packet to handle.
   */
  private void resourcePackResponsePacket0(@NotNull final ResourcePackResponsePacket packet) {
    final var status = packet.getStatus();
    final var packs = packet.getPacks();
    final var packManager = Shiruka.getPackManager();
    switch (status) {
      case REFUSED:
        if (ServerConfig.forceResources) {
          this.connection.disconnect(TranslatedTexts.NO_REASON);
        }
        break;
      case COMPLETED:
        if (this.loginData == null) {
          return;
        }
        if (this.loginData.getTask() != null &&
          Shiruka.getScheduler().isCurrentlyRunning(this.loginData.getTask().getTaskId())) {
          this.loginData.setShouldLogin(true);
        } else {
          this.loginData.initialize();
        }
        break;
      case SEND_PACKS:
        packs.forEach(pack -> {
          final var optional = packManager.getPackByUniqueId(pack.getUniqueId());
          if (optional.isEmpty()) {
            this.connection.disconnect(TranslatedTexts.RESOURCE_PACK_REASON);
            return;
          }
          final var loaded = optional.get();
          this.connection.sendPacket(new ResourcePackDataInfoPacket(loaded));
        });
        break;
      case HAVE_ALL_PACKS:
        final var packStack = packManager.getPackStack();
        if (packStack instanceof ShirukaPacket) {
          this.connection.sendPacket((ShirukaPacket) packStack);
        }
        break;
      default:
        break;
    }
  }
}
