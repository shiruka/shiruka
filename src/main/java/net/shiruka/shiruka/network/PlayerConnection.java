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

import lombok.Getter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.event.events.player.PlayerQuitEvent;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.entity.entities.ShirukaPlayerEntity;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkDataPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkRequestPacket;
import net.shiruka.shiruka.network.packets.ViolationWarningPacket;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents player connections.
 */
public final class PlayerConnection implements PacketHandler {

  /**
   * the network manager.
   */
  @NotNull
  private final NetworkManager networkManager;

  /**
   * the player.
   */
  @NotNull
  @Getter
  private final ShirukaPlayerEntity player;

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
   * the join runnable with async support.
   */
  @Nullable
  private Runnable joinRunnable;

  /**
   * the processed disconnect.
   */
  private boolean processedDisconnect;

  /**
   * ctor.
   *
   * @param player the player.
   */
  public PlayerConnection(@NotNull final ShirukaPlayerEntity player) {
    this.player = player;
    this.networkManager = player.getNetworkManager();
    this.server = this.networkManager.getServer();
  }

  @Override
  public void clientCacheStatus(@NotNull final ClientCacheStatusPacket packet) {
    this.blobCacheSupport = packet.isBlobCacheSupport();
  }

  @Override
  public void onDisconnect(@NotNull final Text disconnectMessage) {
    if (this.processedDisconnect) {
      return;
    } else {
      this.processedDisconnect = true;
    }
  }

  @Override
  public void resourcePackChunkRequest(@NotNull final ResourcePackChunkRequestPacket packet) {
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
    this.player.getNetworkManager().sendPacket(send);
  }

  @Override
  public void violationWarning(@NotNull final ViolationWarningPacket packet) {
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
    if (this.processedDisconnect) {
      return;
    }
    final var reasonWithFallback = this.translate0(reason, TranslatedTexts.DISCONNECTED_NO_REASON);
    final var event = Shiruka.getEventManager().playerKick(this.player, reasonWithFallback);
    if (this.server.isRunning()) {
      event.callEvent();
    }
    if (event.isCancelled()) {
      return;
    }
    final var kickMessage = event.getKickMessage();
    this.player.setQuitReason(PlayerQuitEvent.QuitReason.KICKED);
    this.networkManager.sendPacketImmediately(new DisconnectPacket(kickMessage.asString(), false), future ->
      this.networkManager.close(reasonWithFallback));
    this.onDisconnect(reasonWithFallback);
  }

  @Override
  public void tick() {
    final Runnable join = this.joinRunnable;
    if (join != null) {
      this.joinRunnable = null;
      join.run();
    }
    if (this.player.isValid()) {
    }
  }

  /**
   * the internal simple translation.
   *
   * @param reason the reason to translate.
   * @param fallback the fallback to translate.
   *
   * @return translated string.
   */
  @NotNull
  private Text translate0(@Nullable final Text reason, @NotNull final Text fallback) {
    final Text finalReason;
    if (reason == null) {
      finalReason = fallback;
    } else if (reason instanceof TranslatedText) {
      finalReason = () -> ((TranslatedText) reason).translate(this.player).orElse(reason.asString());
    } else {
      finalReason = reason;
    }
    return finalReason;
  }
}
