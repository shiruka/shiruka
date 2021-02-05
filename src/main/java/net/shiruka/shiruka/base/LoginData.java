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

package net.shiruka.shiruka.base;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.events.ChainDataEvent;
import net.shiruka.api.events.LoginResultEvent;
import net.shiruka.api.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.scheduler.Task;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.network.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents login data.
 */
public final class LoginData {

  /**
   * the chain data.
   */
  @NotNull
  private final ChainDataEvent.ChainData chainData;

  /**
   * the player.
   */
  @NotNull
  private final PlayerConnection connection;

  /**
   * the should login.
   */
  private final AtomicBoolean shouldLogin = new AtomicBoolean();

  /**
   * the task.
   */
  @NotNull
  private final AtomicReference<Task> task = new AtomicReference<>();

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
   * ctor.
   *
   * @param chainData the chain data.
   * @param connection the connection.
   * @param username the username.
   */
  public LoginData(@NotNull final ChainDataEvent.ChainData chainData, @NotNull final PlayerConnection connection,
                   @NotNull final Text username) {
    this.chainData = chainData;
    this.username = username;
    this.connection = connection;
  }

  @NotNull
  public ChainDataEvent.ChainData chainData() {
    return this.chainData;
  }

  /**
   * obtains the task.
   *
   * @return task.
   */
  @Nullable
  public Task getTask() {
    return this.task.get();
  }

  /**
   * sets the task.
   *
   * @param task task to set.
   */
  public void setTask(@NotNull final Task task) {
    this.task.set(task);
  }

  /**
   * initializes the player.
   */
  public void initializePlayer() {
    if (this.asyncLogin == null) {
      return;
    }
    if (this.connection.getConnection().isDisconnected()) {
      return;
    }
    if (this.asyncLogin.getLoginResult() == LoginResultEvent.LoginResult.KICK) {
      this.connection.disconnect(this.asyncLogin.getKickMessage().orElse(null));
      return;
    }
    final var profile = new GameProfile(this.username, this.chainData.getUniqueId(), this.chainData.getXboxUniqueId());
    final var player = new ShirukaPlayer(this.connection, this, profile);
    this.connection.setPlayer(player);
    player.initialize();
    this.asyncLogin.getActions().forEach(action -> action.accept(player));
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
  public synchronized void setShouldLogin(final boolean shouldLogin) {
    this.shouldLogin.set(shouldLogin);
  }

  /**
   * obtains the should login.
   *
   * @return should login.
   */
  public synchronized boolean shouldLogin() {
    return this.shouldLogin.get();
  }
}
