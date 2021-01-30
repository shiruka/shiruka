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

import com.google.common.base.Preconditions;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.protocol.Reliability;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import net.shiruka.api.Shiruka;
import net.shiruka.api.text.ChatColor;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.event.LoginData;
import net.shiruka.shiruka.event.SimpleChainData;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.network.packets.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents player connections.
 */
public final class PlayerConnection implements PacketHandler {

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
   * the queued packets.
   */
  private final Queue<ShirukaPacket> queuedPackets = PlatformDependent.newMpscQueue();

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
   * the player.
   */
  @Nullable
  private ShirukaPlayer player;

  /**
   * ctor.
   *
   * @param connection the connection.
   */
  public PlayerConnection(@NotNull final RakNetClientPeer connection) {
    this.connection = connection;
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
        final var matcher = PlayerConnection.NAME_PATTERN.matcher(username);
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
        this.loginData = new LoginData(chainData, this, () -> ChatColor.clean(username));
        final var preLogin = Shiruka.getEventManager().playerPreLogin(chainData, () -> "Some reason.");
        preLogin.callEvent();
        if (preLogin.isCancelled()) {
          this.disconnect(preLogin.getKickMessage().orElse(null));
          return;
        }
        final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(chainData);
        this.loginData.setAsyncLogin(asyncLogin);
        this.loginData.setTask(Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
          asyncLogin.callEvent();
          Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
            if (this.loginData.shouldLogin()) {
              this.loginData.initializePlayer();
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

  @Override
  public void resourcePackChunkRequestPacket(@NotNull final ResourcePackChunkRequestPacket packet) {
    final var packId = packet.getPackId();
    final var version = packet.getVersion();
    final var chunkSize = packet.getChunkSize();
    final var resourcePack = Shiruka.getPackManager().getPack(packId + "_" + version);
    if (resourcePack.isEmpty()) {
      this.disconnect(TranslatedText.get("disconnectionScreen.resourcePack").asString());
      return;
    }
    final var pack = resourcePack.get();
    final var send = new ResourcePackChunkDataPacket(chunkSize, pack.getChunk(1048576 * chunkSize, 1048576),
      packId, version, 1048576L * chunkSize);
    this.sendPacket(send);
  }

  @Override
  public void resourcePackResponsePacket(@NotNull final ResourcePackResponsePacket packet) {
    final var status = packet.getStatus();
    final var packs = packet.getPacks();
    switch (status) {
      case REFUSED:
        if (ServerConfig.FORCE_RESOURCES.getValue().orElse(false)) {
          this.disconnect(TranslatedText.get("disconnectionScreen.noReason"));
        }
        break;
      case COMPLETED:
        if (this.loginData == null) {
          return;
        }
        if (this.loginData.getTask() != null &&
          !Shiruka.getScheduler().isCurrentlyRunning(this.loginData.getTask().getTaskId())) {
          this.loginData.initializePlayer();
        } else {
          this.loginData.setShouldLogin(true);
        }
        break;
      case SEND_PACKS:
        packs.forEach(pack -> {
          final var optional = Shiruka.getPackManager().getPackByUniqueId(pack.getUniqueId());
          if (optional.isEmpty()) {
            this.disconnect(TranslatedText.get("disconnectionScreen.resourcePack"));
            return;
          }
          final var loaded = optional.get();
          this.sendPacket(new ResourcePackDataInfoPacket(loaded));
        });
        break;
      case HAVE_ALL_PACKS:
        final var packStack = Shiruka.getPackManager().getPackStack();
        if (packStack instanceof ShirukaPacket) {
          this.sendPacket((ShirukaPacket) packStack);
        }
        break;
    }
  }

  @Override
  public void violationWarningPacket(@NotNull final ViolationWarningPacket packet) {
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
    final var message = this.translate0(reason, PacketUtility.DISCONNECTED_NO_REASON).asString();
    final var disconnectPacket = new DisconnectPacket(message, reason == null);
    this.sendPacket(disconnectPacket);
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
   * obtains the player.
   *
   * @return player.
   */
  @Nullable
  public ShirukaPlayer getPlayer() {
    return this.player;
  }

  /**
   * handles {@link #queuedPackets}.
   */
  public void handleQueuedPackets() {
    var toBatch = new ObjectArrayList<ShirukaPacket>();
    @Nullable ShirukaPacket packet;
    while ((packet = this.queuedPackets.poll()) != null) {
      if (!packet.getClass().isAnnotationPresent(NoEncryption.class)) {
        toBatch.add(packet);
        continue;
      }
      if (!toBatch.isEmpty()) {
        this.sendWrapped(toBatch);
        toBatch = new ObjectArrayList<>();
      }
      this.sendWrapped(Collections.singleton(packet));
    }
    if (!toBatch.isEmpty()) {
      this.sendWrapped(toBatch);
    }
  }

  /**
   * sends the given {@code packet} to {@link #connection}.
   *
   * @param packet the packet to send.
   */
  public void sendPacket(@NotNull final ShirukaPacket packet) {
    this.queuedPackets.add(packet);
  }

  /**
   * sends the given {@code packet} to {@link #connection}.
   *
   * @param packet the packet to send.
   */
  public void sendPacketImmediately(@NotNull final ShirukaPacket packet) {
    this.sendWrapped(Collections.singleton(packet));
  }

  /**
   * sends the wrapped packets to the connection.
   *
   * @param packets the packets to send.
   */
  private void sendWrapped(@NotNull final Collection<ShirukaPacket> packets) {
    final var compressed = Unpooled.buffer();
    try {
      Protocol.serialize(compressed, packets, Deflater.DEFAULT_COMPRESSION);
      this.sendWrapped(compressed);
    } catch (final Exception e) {
      Shiruka.getLogger().error("Unable to compress packets", e);
    } finally {
      compressed.release();
    }
  }

  /**
   * sends the given compressed packet to the connection.
   *
   * @param compressed the compressed packet to send.
   */
  private synchronized void sendWrapped(@NotNull final ByteBuf compressed) {
    final var packet = Unpooled.buffer(compressed.readableBytes() + 9);
    packet.writeByte(0xfe);
    packet.writeBytes(compressed);
    this.connection.sendMessage(Reliability.RELIABLE_ORDERED, packet);
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
    if (reason == null || this.player == null) {
      finalReason = fallback;
    } else if (reason instanceof TranslatedText) {
      finalReason = () -> ((TranslatedText) reason).translate(this.player).orElse(reason.asString());
    } else {
      finalReason = reason;
    }
    return finalReason;
  }
}
