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
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.base.Tick;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.text.ChatColor;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.base.LoginData;
import net.shiruka.shiruka.base.SimpleChainData;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayer;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkDataPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkRequestPacket;
import net.shiruka.shiruka.network.packets.ResourcePackDataInfoPacket;
import net.shiruka.shiruka.network.packets.ResourcePackResponsePacket;
import net.shiruka.shiruka.network.packets.ViolationWarningPacket;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents player connections.
 */
public final class PlayerConnection implements PacketHandler, Tick {

  /**
   * the name pattern to check client's usernames.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z\\s\\d_]{3,16}+$");

  /**
   * the join attempts this tick.
   */
  private static int joinAttemptsThisTick;

  /**
   * the old tick.
   */
  private static int oldTick;

  /**
   * the login listener.
   */
  public final LoginListener loginListener = new LoginListener();

  /**
   * the connection.
   */
  @NotNull
  private final RakNetClientPeer connection;

  /**
   * the packet handler.
   */
  private final AtomicReference<PacketHandler> packetHandler = new AtomicReference<>(this.loginListener);

  /**
   * the queued packets.
   */
  private final PriorityQueue<ShirukaPacket> queuedPackets = new ObjectArrayFIFOQueue<>();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the blob cache support.
   */
  private boolean blobCacheSupport;

  /**
   * the player.
   */
  @Nullable
  private ShirukaPlayer player;

  /**
   * the profile.
   */
  @Nullable
  private GameProfile profile;

  /**
   * ctor.
   *
   * @param connection the connection.
   * @param server the server.
   */
  public PlayerConnection(@NotNull final RakNetClientPeer connection, @NotNull final ShirukaServer server) {
    this.connection = connection;
    this.server = server;
  }

  @Override
  public void clientCacheStatusPacket(@NotNull final ClientCacheStatusPacket packet) {
    this.blobCacheSupport = packet.isBlobCacheSupport();
  }

  @Override
  public void resourcePackChunkRequestPacket(@NotNull final ResourcePackChunkRequestPacket packet) {
    final var packId = packet.getPackId();
    final var version = packet.getVersion();
    final var chunkSize = packet.getChunkSize();
    final var resourcePack = Shiruka.getPackManager().getPack(packId + "_" + version);
    if (resourcePack.isEmpty()) {
      this.disconnect(TranslatedTexts.RESOURCE_PACK_REASON.asString());
      return;
    }
    final var pack = resourcePack.get();
    final var chunk = pack.getChunk(1048576 * chunkSize, 1048576);
    final var send = new ResourcePackChunkDataPacket(chunkSize, chunk, packId, version, 1048576L * chunkSize);
    this.sendPacket(send);
  }

  @Override
  public void tick() {
    if (this.connection.isConnected() && Shiruka.isPrimaryThread()) {
      this.handleQueuedPackets();
    }
    if (PlayerConnection.oldTick != this.server.getTick().getCurrentTick()) {
      PlayerConnection.oldTick = this.server.getTick().getCurrentTick();
      PlayerConnection.joinAttemptsThisTick = 0;
    }
    final var handler = this.packetHandler.get();
    if (handler instanceof LoginListener &&
      PlayerConnection.joinAttemptsThisTick++ < ServerConfig.maxLoginPerTick) {
      handler.tick();
    }
    if (handler instanceof PlayerConnection) {
      ((PlayerConnection) handler).doTick();
    }
  }

  @Override
  public void violationWarningPacket(@NotNull final ViolationWarningPacket packet) {
    Shiruka.getLogger().error("Something went wrong when reading a packet!");
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
    final var message = this.translate0(reason, TranslatedTexts.DISCONNECTED_NO_REASON).asString();
    this.sendPacketImmediately(new DisconnectPacket(message, reason == null));
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
   * obtains the packet handler.
   *
   * @return packet handler.
   */
  @NotNull
  public PacketHandler getPacketHandler() {
    return this.packetHandler.get();
  }

  /**
   * sets the {@link #packetHandler}.
   *
   * @param handler the handler to set.
   */
  public void setPacketHandler(@NotNull final PacketHandler handler) {
    this.packetHandler.set(handler);
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
   * obtains the server.
   *
   * @return server.
   */
  @NotNull
  public ShirukaServer getServer() {
    return this.server;
  }

  /**
   * polls all packets in {@link #queuedPackets} and handles them.
   */
  public void handleQueuedPackets() {
    if (this.queuedPackets.isEmpty()) {
      return;
    }
    var toBatch = new ObjectArrayList<ShirukaPacket>();
    while (!this.queuedPackets.isEmpty()) {
      final var packet = this.queuedPackets.dequeue();
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
   * runs when the player just created.
   *
   * @param player the player to initialize.
   */
  public void initialize(@NotNull final ShirukaPlayer player) {
    this.player = player;
    this.setPacketHandler(this);
    this.server.playerList.initialize(player);
  }

  /**
   * sends the given {@code packet} to {@link #connection}.
   *
   * @param packet the packet to send.
   */
  public void sendPacket(@NotNull final ShirukaPacket packet) {
    this.queuedPackets.enqueue(packet);
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
   * ticks.
   */
  private void doTick() {
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

  /**
   * an enum class to determine protocol statements.
   */
  enum ProtocolState {

    /**
     * the empty.
     */
    EMPTY,
    /**
     * the key.
     */
    KEY,
    /**
     * the authenticating.
     */
    AUTHENTICATING,
    /**
     * the ready to accept.
     */
    READY_TO_ACCEPT,
    /**
     * the delay accept.
     */
    DELAY_ACCEPT,
    /**
     * accepted.
     */
    ACCEPTED
  }

  /**
   * a class that represents login listener.
   */
  public final class LoginListener implements PacketHandler {

    /**
     * the wants to join.
     */
    @Nullable
    public ShirukaPlayer wantsToJoin;

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

    @Override
    public void loginPacket(@NotNull final LoginPacket packet) {
      this.latestLoginPacket = packet;
    }

    @Override
    public void resourcePackResponsePacket(@NotNull final ResourcePackResponsePacket packet) {
      this.latestResourcePacket = packet;
    }

    @Override
    public void tick() {
      if (!Shiruka.getServer().isRunning()) {
        PlayerConnection.this.disconnect(TranslatedTexts.RESTART_REASON);
        return;
      }
      if (this.wantsToJoin == null) {
        if (this.latestLoginPacket != null) {
          this.loginPacket0(this.latestLoginPacket);
        }
        if (this.latestResourcePacket != null) {
          this.resourcePackResponsePacket0(this.latestResourcePacket);
        }
      } else if (PlayerConnection.this.profile != null) {
        final var pending = PlayerConnection.this.server.playerList
          .getActivePlayer(PlayerConnection.this.profile.getUniqueId());
        if (pending == null) {
          this.wantsToJoin = null;
        }
      }
      if (this.loginTimeoutCounter++ >= 600) {
        PlayerConnection.this.disconnect(TranslatedTexts.SLOW_LOGIN_REASON);
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
        PlayerConnection.this.disconnect(TranslatedTexts.RESTART_REASON);
        return;
      }
      final var protocolVersion = packet.getProtocolVersion();
      final var encodedChainData = packet.getChainData().toString();
      final var encodedSkinData = packet.getSkinData().toString();
      if (protocolVersion < ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
        PlayerConnection.this.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD));
        return;
      }
      if (protocolVersion > ShirukaMain.MINECRAFT_PROTOCOL_VERSION) {
        PlayerConnection.this.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD));
        return;
      }
      Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
        final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
        Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
          Languages.addLoadedLanguage(chainData.getLanguageCode());
          if (!chainData.isXboxAuthed() && ServerConfig.onlineMode) {
            PlayerConnection.this.disconnect(TranslatedTexts.NOT_AUTHENTICATED_REASON);
            return;
          }
          final var username = chainData.getUsername();
          final var matcher = PlayerConnection.NAME_PATTERN.matcher(username);
          if (!matcher.matches() ||
            username.equalsIgnoreCase("rcon") ||
            username.equalsIgnoreCase("console")) {
            PlayerConnection.this.disconnect(TranslatedTexts.INVALID_NAME_REASON);
            return;
          }
          if (!chainData.getSkin().isValid()) {
            PlayerConnection.this.disconnect(TranslatedTexts.INVALID_SKIN_REASON);
            return;
          }
          PlayerConnection.this.profile = new GameProfile(
            () -> ChatColor.clean(username),
            chainData.getUniqueId(),
            chainData.getXboxUniqueId());
          this.loginData = new LoginData(chainData, PlayerConnection.this, PlayerConnection.this.profile);
          final var preLogin = Shiruka.getEventManager().playerPreLogin(chainData);
          preLogin.callEvent();
          if (preLogin.isCancelled()) {
            PlayerConnection.this.disconnect(preLogin.getKickMessage().orElse(null));
            return;
          }
          final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(chainData);
          this.loginData.setAsyncLogin(asyncLogin);
          this.loginData.setTask(Shiruka.getScheduler().scheduleAsync(ShirukaServer.INTERNAL_PLUGIN, () -> {
            asyncLogin.callEvent();
            if (asyncLogin.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
              Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () ->
                PlayerConnection.this.disconnect(asyncLogin.getKickMessage().orElse(null)));
              return;
            }
            Shiruka.getScheduler().schedule(ShirukaServer.INTERNAL_PLUGIN, () -> {
              if (this.loginData.shouldLogin()) {
                this.loginData.initialize();
              }
            });
          }));
          PlayerConnection.this.sendPacket(new PlayStatusPacket(PlayStatusPacket.Status.LOGIN_SUCCESS));
          final var packInfo = Shiruka.getPackManager().getPackInfo();
          if (packInfo instanceof ShirukaPacket) {
            PlayerConnection.this.sendPacket((ShirukaPacket) packInfo);
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
            PlayerConnection.this.disconnect(TranslatedTexts.NO_REASON);
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
              PlayerConnection.this.disconnect(TranslatedTexts.RESOURCE_PACK_REASON);
              return;
            }
            final var loaded = optional.get();
            PlayerConnection.this.sendPacket(new ResourcePackDataInfoPacket(loaded));
          });
          break;
        case HAVE_ALL_PACKS:
          final var packStack = packManager.getPackStack();
          if (packStack instanceof ShirukaPacket) {
            PlayerConnection.this.sendPacket((ShirukaPacket) packStack);
          }
          break;
        default:
          break;
      }
    }
  }
}
