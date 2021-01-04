/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
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

import io.github.shiruka.api.events.LoginDataEvent;
import io.github.shiruka.api.events.LoginResultEvent;
import io.github.shiruka.api.events.player.PlayerAsyncLoginEvent;
import io.github.shiruka.api.events.player.PlayerPreLoginEvent;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
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
  private final ShirukaPlayer player;

  /**
   * the username.
   */
  @NotNull
  private final String username;

  /**
   * the async login event.
   */
  @Nullable
  private PlayerAsyncLoginEvent asyncLogin;

  /**
   * the should login.
   */
  private boolean shouldLogin;

  /**
   * ctor.
   *
   * @param chainData the chain data.
   * @param player the player.
   * @param username the username.
   */
  public SimpleLoginData(@NotNull final LoginDataEvent.ChainData chainData, @NotNull final ShirukaPlayer player,
                         @NotNull final String username) {
    this.chainData = chainData;
    this.username = username;
    this.player = player;
  }

  @NotNull
  @Override
  public LoginDataEvent.ChainData chainData() {
    return this.chainData;
  }

  /**
   * initializes the player.
   */
  public void initializePlayer() {
    if (this.asyncLogin == null) {
      return;
    }
    if (this.player.getPlayerConnection().getConnection().isClosed()) {
      return;
    }
    if (this.asyncLogin.loginResult() == LoginResultEvent.LoginResult.KICK) {
      this.player.disconnect(this.asyncLogin.kickMessage());
      return;
    }
    if (!this.shouldLogin()) {
      return;
    }
    this.asyncLogin.objects().forEach(action ->
      action.accept(this.player));
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
