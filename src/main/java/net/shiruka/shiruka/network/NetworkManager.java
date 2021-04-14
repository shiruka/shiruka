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

import com.nukkitx.network.raknet.RakNetSession;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.Tick;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayerEntity;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents network connections.
 */
public final class NetworkManager implements Tick {

  /**
   * the join attempts this tick.
   */
  private static int joinAttemptsThisTick;

  /**
   * the old tick.
   */
  private static int oldTick;

  /**
   * the connection.
   */
  @NotNull
  @Getter
  private final BedrockServerSession client;

  /**
   * the login listener.
   */
  @Getter
  private final LoginListener loginListener = new LoginListener(this);

  /**
   * the packet handler.
   */
  @NotNull
  private final AtomicReference<ShirukaPacketHandler> packetHandler;

  /**
   * the server.
   */
  @NotNull
  @Getter
  private final ShirukaServer server;

  /**
   * the session.
   */
  @NotNull
  @Getter
  private final RakNetSession session;

  /**
   * the disconnect message.
   */
  @Nullable
  private Text disconnectMessage;

  /**
   * the disconnection handled.
   */
  private boolean disconnectionHandled;

  /**
   * ctor.
   *
   * @param client the client.
   * @param server the server.
   */
  public NetworkManager(@NotNull final BedrockServerSession client, @NotNull final ShirukaServer server) {
    this.client = client;
    this.session = (RakNetSession) client.getConnection();
    this.server = server;
    this.packetHandler = new AtomicReference<>(this.loginListener);
    client.setPacketHandler(this.loginListener);
  }

  /**
   * closes the network connection.
   *
   * @param closeMessage the close message to close.
   */
  public void close(@Nullable final Text closeMessage) {
    if (this.session.getChannel().isOpen()) {
      this.session.getChannel().close();
      this.disconnectMessage = closeMessage;
    }
  }

  /**
   * obtains the packet handler.
   *
   * @return packet handler.
   */
  @NotNull
  public ShirukaPacketHandler getPacketHandler() {
    return this.packetHandler.get();
  }

  /**
   * sets the {@link #packetHandler}.
   *
   * @param handler the handler to set.
   */
  public void setPacketHandler(@NotNull final ShirukaPacketHandler handler) {
    this.packetHandler.set(handler);
    this.client.setPacketHandler(handler);
  }

  /**
   * gets the player.
   *
   * @return player.
   */
  @NotNull
  public Optional<ShirukaPlayerEntity> getPlayer() {
    final var handler = this.getPacketHandler();
    if (handler instanceof PlayerConnection) {
      return Optional.of(((PlayerConnection) handler).getPlayer());
    }
    return Optional.empty();
  }

  /**
   * obtains the address.
   *
   * @return address.
   */
  @NotNull
  public InetSocketAddress getSocketAddress() {
    return this.client.getAddress();
  }

  /**
   * handles the disconnection of the client.
   */
  public void handleDisconnection() {
    if (this.isConnected()) {
      return;
    }
    if (this.disconnectionHandled) {
      return;
    }
    this.disconnectionHandled = true;
    final var handler = this.getPacketHandler();
    handler.onDisconnect(Objects.requireNonNullElse(this.disconnectMessage, TranslatedTexts.DISCONNECTED_NO_REASON));
    if (handler instanceof PlayerConnection) {
      final var playerConnection = (PlayerConnection) handler;
      final var player = playerConnection.getPlayer();
      Shiruka.getEventManager().playerConnectionClose(player.getAddress(), player.getName(), player.getUniqueId(),
        player.getXboxUniqueId()).callEvent();
    } else if (handler instanceof LoginListener) {
      final var loginListener = (LoginListener) handler;
      final var profile = loginListener.getProfile();
      if (profile != null) {
        Shiruka.getEventManager().playerConnectionClose(this.getSocketAddress(), profile.getName(),
          profile.getUniqueId(), profile.getXboxUniqueId()).callEvent();
      }
    }
  }

  /**
   * runs when the player just created.
   *
   * @param player the player to initialize.
   */
  public void initialize(@NotNull final ShirukaPlayerEntity player) {
    this.setPacketHandler(player.getPlayerConnection());
    this.server.getPlayerList().initialize(player);
  }

  /**
   * checks if the client's channel is open.
   *
   * @return {@code true} if the client's channel is open.
   */
  public boolean isConnected() {
    return this.session.getChannel().isOpen();
  }

  @Override
  public void tick() {
    if (NetworkManager.oldTick != this.server.getTick().getCurrentTick()) {
      NetworkManager.oldTick = this.server.getTick().getCurrentTick();
      NetworkManager.joinAttemptsThisTick = 0;
    }
    final var handler = this.getPacketHandler();
    if (handler instanceof LoginListener &&
      NetworkManager.joinAttemptsThisTick++ < ServerConfig.maxLoginPerTick) {
      handler.tick();
    }
    if (handler instanceof PlayerConnection) {
      handler.tick();
    }
  }
}
