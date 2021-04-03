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
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.base.ChainData;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.event.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.scheduler.Task;
import net.shiruka.shiruka.entity.entities.ShirukaPlayerEntity;
import net.shiruka.shiruka.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents login data.
 */
@RequiredArgsConstructor
public final class LoginData {

  /**
   * the async login event.
   */
  @NotNull
  @Getter
  private final PlayerAsyncLoginEvent asyncLogin;

  /**
   * the chain data.
   */
  @NotNull
  @Getter
  private final ChainData chainData;

  /**
   * the network manager.
   */
  @NotNull
  private final NetworkManager networkManager;

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * the should login.
   */
  private final AtomicBoolean shouldLogin = new AtomicBoolean();

  /**
   * the task.
   */
  @NotNull
  private final Task task;

  /**
   * ctor.
   *
   * @param asyncLogin the async login.
   * @param chainData the chain data.
   * @param networkManager the networkd manager.
   * @param profile the profile.
   * @param task the task function.
   */
  public LoginData(@NotNull final PlayerAsyncLoginEvent asyncLogin, @NotNull final ChainData chainData,
                   @NotNull final NetworkManager networkManager, @NotNull final GameProfile profile,
                   @NotNull final Function<LoginData, Task> task) {
    this.asyncLogin = asyncLogin;
    this.chainData = chainData;
    this.networkManager = networkManager;
    this.profile = profile;
    this.task = task.apply(this);
  }

  /**
   * initializes the player.
   */
  public void initialize() {
    if (this.networkManager.getClient().isDisconnected()) {
      return;
    }
    final var player = new ShirukaPlayerEntity(this.networkManager.getServer().getWorldManager().getDefaultWorld(),
      this.networkManager, this,
      this.profile);
    if (this.asyncLogin.getLoginResult() != LoginResultEvent.LoginResult.ALLOWED) {
      player.getPlayerConnection().disconnect(this.asyncLogin.getKickMessage());
      return;
    }
    this.networkManager.initialize(player);
    this.asyncLogin.getObjects().forEach(action -> action.accept(player));
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
