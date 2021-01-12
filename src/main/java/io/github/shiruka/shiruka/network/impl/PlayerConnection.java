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

package io.github.shiruka.shiruka.network.impl;

import io.github.shiruka.api.text.Text;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.ShirukaServer;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.event.SimpleLoginData;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.packets.PacketOutDisconnect;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents player connections.
 */
public final class PlayerConnection {

  /**
   * the connection.
   */
  @NotNull
  private final Connection<ServerSocket> connection;

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the login data.
   */
  @Nullable
  private SimpleLoginData loginData;

  /**
   * the player.
   */
  @Nullable
  private ShirukaPlayer player;

  /**
   * the state.
   */
  @NotNull
  private State state = State.HANDSHAKE;

  /**
   * ctor.
   *
   * @param connection the connection.
   * @param server the server.
   */
  public PlayerConnection(@NotNull final Connection<ServerSocket> connection,
                          @NotNull final ShirukaServer server) {
    this.connection = connection;
    this.server = server;
  }

  /**
   * disconnects the connection.
   *
   * @param reason the reason to disconnect.
   */
  public void disconnect(@Nullable final Text reason) {
    this.connection.checkForClosed();
    final Text finalReason;
    final boolean messageSkipped;
    if (reason == null) {
      finalReason = TranslatedText.get("disconnect.disconnected");
      messageSkipped = true;
    } else {
      finalReason = reason;
      messageSkipped = false;
    }
    this.sendPacket(new PacketOutDisconnect(finalReason, messageSkipped));
  }

  /**
   * obtains the connection.
   *
   * @return connection.
   */
  @NotNull
  public Connection<ServerSocket> getConnection() {
    return this.connection;
  }

  /**
   * obtains the latest login data.
   *
   * @return latest login data.
   */
  @Nullable
  public SimpleLoginData getLatestLoginData() {
    return this.loginData;
  }

  /**
   * sets the latest login data of the player.
   *
   * @param loginData the login data to set.
   */
  public void setLatestLoginData(@NotNull final SimpleLoginData loginData) {
    this.loginData = loginData;
  }

  /**
   * obtains the player.
   *
   * @return player.
   */
  @NotNull
  public Optional<ShirukaPlayer> getPlayer() {
    return Optional.ofNullable(this.player);
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
   * obtains the state.
   *
   * @return state.
   */
  @NotNull
  public State getState() {
    return this.state;
  }

  /**
   * sets the state.
   *
   * @param state the state to set.
   */
  public void setState(@NotNull final State state) {
    this.state = state;
  }

  public void sendPacket(@NotNull final PacketOut packet) {
    this.connection.checkForClosed();
    this.connection.addQueuedPacket(packet);
  }

  /**
   * represents the current connection state that the client is in whilst connecting to the server.
   */
  public enum State {
    /**
     * handshake, attempting to connect to server.
     */
    HANDSHAKE,
    /**
     * obtain server status via ping.
     */
    STATUS,
    /**
     * login, authenticate and complete connection formalities before joining.
     */
    LOGIN,
    /**
     * normal gameplay.
     */
    PLAY,
    /**
     * for any statement of the connection.
     */
    ANY
  }
}
