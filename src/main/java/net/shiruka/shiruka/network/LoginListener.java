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
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.text.ChatColor;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.base.LoginData;
import net.shiruka.shiruka.base.SimpleChainData;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayerEntity;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
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
@Log4j2
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
  private final NetworkManager networkManager;

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
   * the profile.
   */
  @Nullable
  @Getter
  private GameProfile profile;

  /**
   * the wants to join.
   */
  @Nullable
  @Setter
  private ShirukaPlayerEntity wantsToJoin;

  /**
   * disconnects the connection.
   *
   * @param kickMessage the kick message to send.
   */
  public void disconnect(@NotNull final Text kickMessage) {
    try {
      // @todo #1:5m Add language support for Disconnecting {}: {}.
      LoginListener.log.info("Disconnecting {}: {}", this.getNetworkName(), kickMessage.asString());
      this.networkManager.sendPacket(new DisconnectPacket(kickMessage.asString(), false));
      this.networkManager.close(kickMessage);
    } catch (final Exception exception) {
      // @todo #1:5m Add language support for Error whilst disconnecting player.
      LoginListener.log.error("Error whilst disconnecting player", exception);
    }
  }

  /**
   * obtains the network name before login.
   *
   * @return network name of the connection.
   */
  @NotNull
  public String getNetworkName() {
    if (this.profile == null) {
      return this.networkManager.getSocketAddress().toString();
    }
    return this.profile + " (" + this.networkManager.getSocketAddress() + ")";
  }

  @Override
  public void login(@NotNull final LoginPacket packet) {
    this.latestLoginPacket = packet;
  }

  @Override
  public void onDisconnect(@NotNull final Text disconnectMessage) {
    // @todo #1:5m Add language support for {} lost connection: {}.
    LoginListener.log.info("{} lost connection: {}", this.getNetworkName(), disconnectMessage.asString());
  }

  @Override
  public void resourcePackResponse(@NotNull final ResourcePackResponsePacket packet) {
    this.latestResourcePacket = packet;
  }

  @Override
  public void tick() {
    if (!Shiruka.getServer().isRunning()) {
      this.disconnect(TranslatedTexts.RESTART_REASON);
      return;
    }
    if (this.wantsToJoin == null) {
      if (this.latestLoginPacket != null) {
        this.loginPacket0(this.latestLoginPacket);
      }
      if (this.latestResourcePacket != null) {
        this.resourcePackResponsePacket0(this.latestResourcePacket);
      }
    } else if (this.profile != null) {
      final var pending = this.networkManager.getServer().getPlayerList()
        .getActivePlayer(this.profile.getUniqueId());
      if (pending == null) {
        this.wantsToJoin = null;
      }
    }
    if (this.loginTimeoutCounter++ >= 600) {
      this.disconnect(TranslatedTexts.SLOW_LOGIN_REASON);
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
      this.disconnect(TranslatedTexts.RESTART_REASON);
      return;
    }
    final var protocolVersion = packet.getProtocolVersion();
    final var encodedChainData = packet.getChainData().toString();
    final var encodedSkinData = packet.getSkinData().toString();
    if (protocolVersion < ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      this.networkManager.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD));
      return;
    }
    if (protocolVersion > ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      this.networkManager.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD));
      return;
    }
    Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
      final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
      Languages.addLoadedLanguage(chainData.getLanguageCode());
      Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
        if (!chainData.isXboxAuthed() && ServerConfig.onlineMode) {
          this.disconnect(TranslatedTexts.NOT_AUTHENTICATED_REASON);
          return;
        }
        final var username = chainData.getUsername();
        final var matcher = LoginListener.NAME_PATTERN.matcher(username);
        if (!matcher.matches() ||
          username.equalsIgnoreCase("rcon") ||
          username.equalsIgnoreCase("console")) {
          this.disconnect(TranslatedTexts.INVALID_NAME_REASON);
          return;
        }
        if (!chainData.getSkin().isValid()) {
          this.disconnect(TranslatedTexts.INVALID_SKIN_REASON);
          return;
        }
        this.profile = new GameProfile(
          () -> ChatColor.clean(username),
          chainData.getUniqueId(),
          chainData.getXboxUniqueId());
        final var preLogin = Shiruka.getEventManager().playerPreLogin(chainData);
        preLogin.callEvent();
        if (preLogin.isCancelled()) {
          this.disconnect(preLogin.getKickMessage());
          return;
        }
        final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(chainData);
        this.loginData = new LoginData(asyncLogin, chainData, this.networkManager, this.profile, data ->
          Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
            asyncLogin.callEvent();
            if (asyncLogin.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
              Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () ->
                this.disconnect(asyncLogin.getKickMessage()));
              return;
            }
            Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
              if (data.shouldLogin()) {
                data.initialize();
              }
            });
          }));
        this.networkManager.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_SUCCESS));
        final var packInfo = Shiruka.getPackManager().getPackInfo();
        if (packInfo instanceof ShirukaPacket) {
          this.networkManager.sendPacket((ShirukaPacket) packInfo);
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
          this.disconnect(TranslatedTexts.NO_REASON);
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
            this.disconnect(TranslatedTexts.RESOURCE_PACK_REASON);
            return;
          }
          final var loaded = optional.get();
          this.networkManager.sendPacket(new ResourcePackDataInfoPacket(loaded));
        });
        break;
      case HAVE_ALL_PACKS:
        final var packStack = packManager.getPackStack();
        if (packStack instanceof ShirukaPacket) {
          this.networkManager.sendPacket((ShirukaPacket) packStack);
        }
        break;
      default:
        break;
    }
  }
}
