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

package io.github.shiruka.shiruka.event;

import io.github.shiruka.api.base.GameProfile;
import io.github.shiruka.api.events.LoginDataEvent;
import io.github.shiruka.api.events.LoginResultEvent;
import io.github.shiruka.api.events.player.PlayerAsyncLoginEvent;
import io.github.shiruka.api.events.player.PlayerPreLoginEvent;
import io.github.shiruka.api.text.Text;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.scheduler.AsyncTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link PlayerPreLoginEvent.LoginData}.
 */
public final class SimpleLoginData implements LoginDataEvent.LoginData {

  /**
   * the chain data.
   */
  @NotNull
  private final LoginDataEvent.ChainData chainData;

  /**
   * the player.
   */
  @NotNull
  private final PlayerConnection connection;

  /**
   * the username.
   */
  @NotNull
  private final Text username;

  /**
   * the async login event.
   */
  @Nullable
  private PlayerAsyncLoginEvent asyncLogin;

  /**
   * the process.
   */
  @Nullable
  private AsyncTask process;

  /**
   * the should login.
   */
  private boolean shouldLogin;

  /**
   * ctor.
   *
   * @param chainData the chain data.
   * @param connection the connection.
   * @param username the username.
   */
  public SimpleLoginData(@NotNull final LoginDataEvent.ChainData chainData,
                         @NotNull final PlayerConnection connection, @NotNull final Text username) {
    this.chainData = chainData;
    this.username = username;
    this.connection = connection;
  }

  @NotNull
  @Override
  public LoginDataEvent.ChainData chainData() {
    return this.chainData;
  }

  /**
   * obtains the process.
   *
   * @return process.
   */
  @Nullable
  public AsyncTask getProcess() {
    return this.process;
  }

  /**
   * initializes the player.
   */
  public void initializePlayer() {
    if (this.asyncLogin == null) {
      return;
    }
    if (this.connection.getConnection().isClosed()) {
      return;
    }
    if (this.asyncLogin.loginResult() == LoginResultEvent.LoginResult.KICK) {
      this.connection.disconnect(this.asyncLogin.kickMessage().map(Text::asString).orElse(null));
      return;
    }
    final var profile = new GameProfile(this.username, this.chainData.uniqueId(), this.chainData.xuid());
    final var player = new ShirukaPlayer(this.chainData, this.connection, profile);
    this.connection.setState(PlayerConnection.State.LOGIN);
    player.getPlayerConnection().getServer().addPlayer(player);
    // @todo #1:60m Continue to development here.
    this.asyncLogin.objects().forEach(action -> action.accept(player));
  }

  /**
   * sets the async login event.
   *
   * @param asyncLogin async login event to set.
   */
  public void setAsyncLogin(@NotNull final PlayerAsyncLoginEvent asyncLogin) {
    this.asyncLogin = asyncLogin;
  }

  /**
   * sets the process.
   *
   * @param process the process to set.
   */
  public void setAsyncProcess(@NotNull final AsyncTask process) {
    this.process = process;
  }

  /**
   * sets the should login.
   *
   * @param shouldLogin the should login to set.
   */
  public void setShouldLogin(final boolean shouldLogin) {
    this.shouldLogin = shouldLogin;
  }

  /**
   * obtains the should login.
   *
   * @return should login.
   */
  public boolean shouldLogin() {
    return this.shouldLogin;
  }
}
