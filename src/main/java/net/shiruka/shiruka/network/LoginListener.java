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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.ClientCacheStatusPacket;
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket;
import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkSettingsPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackDataInfoPacket;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
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
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents login listener.
 */
@RequiredArgsConstructor
@Log4j2
public final class LoginListener implements ShirukaPacketHandler {

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
   * the blob cache support.
   */
  private boolean blobCacheSupport;

  /**
   * the latest login packet.
   */
  @Nullable
  private LoginPacket latestLoginPacket;

  /**
   * the latest resource packet.
   */
  @Nullable
  private ResourcePackClientResponsePacket latestResourcePacket;

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
      final var packet = new DisconnectPacket();
      packet.setKickMessage(kickMessage.asString());
      packet.setMessageSkipped(false);
      this.networkManager.getClient().sendPacket(packet);
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
  public boolean handle(@NotNull final ClientCacheStatusPacket packet) {
    this.blobCacheSupport = packet.isSupported();
    return true;
  }

  @Override
  public boolean handle(@NotNull final ClientToServerHandshakePacket packet) {
    final var networkSettings = new NetworkSettingsPacket();
    networkSettings.setCompressionThreshold((short) 1);
    this.networkManager.getClient().sendPacket(networkSettings);
    final var playStatusPacket = new PlayStatusPacket();
    playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
    this.networkManager.getClient().sendPacket(playStatusPacket);
    final var packInfo = Shiruka.getPackManager().getPackInfo();
    if (packInfo instanceof BedrockPacket) {
      this.networkManager.getClient().sendPacket((BedrockPacket) packInfo);
    }
    return true;
  }

  @Override
  public boolean handle(@NotNull final LoginPacket packet) {
    this.latestLoginPacket = packet;
    return true;
  }

  @Override
  public boolean handle(@NotNull final ResourcePackClientResponsePacket packet) {
    this.latestResourcePacket = packet;
    return true;
  }

  @Override
  public void onDisconnect(@NotNull final Text disconnectMessage) {
    // @todo #1:5m Add language support for {} lost connection: {}.
    LoginListener.log.info("{} lost connection: {}", this.getNetworkName(), disconnectMessage.asString());
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
    final var client = this.networkManager.getClient();
    if (protocolVersion < ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      final var playStatusPacket = new PlayStatusPacket();
      playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
      client.sendPacket(playStatusPacket);
      return;
    }
    if (protocolVersion > ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
      final var playStatusPacket = new PlayStatusPacket();
      playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
      client.sendPacket(playStatusPacket);
      return;
    }
    final var internalPlugin = ShirukaServer.INTERNAL_PLUGIN;
    Shiruka.getScheduler().scheduleAsync(internalPlugin, () -> {
      final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
      Languages.addLoadedLanguage(chainData.getLanguageCode());
      Shiruka.getScheduler().schedule(internalPlugin, () -> {
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
          Shiruka.getScheduler().scheduleAsync(internalPlugin, () -> {
            asyncLogin.callEvent();
            if (asyncLogin.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
              Shiruka.getScheduler().schedule(internalPlugin, () ->
                this.disconnect(asyncLogin.getKickMessage()));
              return;
            }
            Shiruka.getScheduler().schedule(internalPlugin, () -> {
              if (data.shouldLogin()) {
                data.initialize();
              }
            });
          }));
        if (!EncryptionUtils.canUseEncryption()) {
          final var playStatusPacket = new PlayStatusPacket();
          playStatusPacket.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
          client.sendPacket(playStatusPacket);
          final var packInfo = Shiruka.getPackManager().getPackInfo();
          if (packInfo instanceof BedrockPacket) {
            client.sendPacket((BedrockPacket) packInfo);
          }
        } else {
          Shiruka.getScheduler().scheduleAsync(internalPlugin, () -> {
            try {
              final var publicKey = (ECPublicKey) SimpleChainData.generateKey(chainData.getPublicKey());
              final var clientJwt = JWSObject.parse(encodedSkinData);
              EncryptionUtils.verifyJwt(clientJwt, publicKey);
              final var generator = KeyPairGenerator.getInstance("EC");
              generator.initialize(new ECGenParameterSpec("secp384r1"));
              final var serverKeyPair = generator.generateKeyPair();
              final var token = EncryptionUtils.generateRandomToken();
              final var encryptionKey = EncryptionUtils.getSecretKey(serverKeyPair.getPrivate(), publicKey, token);
              client.enableEncryption(encryptionKey);
              final var encryptionRequest = new ServerToClientHandshakePacket();
              encryptionRequest.setJwt(EncryptionUtils.createHandshakeJwt(serverKeyPair, token).serialize());
              client.sendPacketImmediately(encryptionRequest);
            } catch (final NoSuchAlgorithmException | InvalidKeySpecException | ParseException | JOSEException |
              InvalidAlgorithmParameterException | InvalidKeyException e) {
              // @todo #1:5m Add language support for Error when generating client's public key.
              LoginListener.log.fatal("Error when generating client's public key", e);
            }
          });
        }
      });
    });
  }

  /**
   * handles the resource pack response packet.
   *
   * @param packet the packet to handle.
   */
  private void resourcePackResponsePacket0(@NotNull final ResourcePackClientResponsePacket packet) {
    final var status = packet.getStatus();
    final var packs = packet.getPackIds();
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
        if (Shiruka.getScheduler().isCurrentlyRunning(this.loginData.getTask().getTaskId())) {
          this.loginData.setShouldLogin(true);
        } else {
          this.loginData.initialize();
        }
        break;
      case SEND_PACKS:
        packs.forEach(entry -> {
          final var optional = packManager.getPackByIdVersion(entry);
          if (optional.isEmpty()) {
            this.disconnect(TranslatedTexts.RESOURCE_PACK_REASON);
            return;
          }
          final var loaded = optional.get();
          final var dataInfoPacket = new ResourcePackDataInfoPacket();
          dataInfoPacket.setPackId(loaded.getId());
          dataInfoPacket.setPackVersion(loaded.getVersion().toString());
          dataInfoPacket.setMaxChunkSize(1048576);
          dataInfoPacket.setChunkCount(loaded.getSize() / dataInfoPacket.getMaxChunkSize());
          dataInfoPacket.setCompressedPackSize(loaded.getSize());
          dataInfoPacket.setHash(loaded.getHash());
          dataInfoPacket.setType(loaded.getType());
          this.networkManager.getClient().sendPacket(dataInfoPacket);
        });
        break;
      case HAVE_ALL_PACKS:
        final var packStack = packManager.getPackStack();
        if (packStack instanceof BedrockPacket) {
          this.networkManager.getClient().sendPacket((BedrockPacket) packStack);
        }
        break;
      default:
        break;
    }
  }
}
