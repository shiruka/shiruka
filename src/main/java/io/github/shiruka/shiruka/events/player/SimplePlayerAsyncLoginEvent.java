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

package io.github.shiruka.shiruka.events.player;

import io.github.shiruka.api.entity.Player;
import io.github.shiruka.api.events.player.PlayerAsyncLoginEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerAsyncLoginEvent}.
 */
public final class SimplePlayerAsyncLoginEvent implements PlayerAsyncLoginEvent {

  /**
   * the actions that will run after player initialization.
   */
  private final List<Consumer<Player>> actions = new ArrayList<>();

  /**
   * the login data.
   */
  @NotNull
  private final LoginData loginData;

  /**
   * the kick message.
   */
  @Nullable
  private String kickMessage;

  /**
   * the login result.
   */
  @NotNull
  private LoginResult loginResult = LoginResult.SUCCESS;

  /**
   * ctor.
   *
   * @param loginData the login data.
   */
  public SimplePlayerAsyncLoginEvent(@NotNull final LoginData loginData) {
    this.loginData = loginData;
  }

  @Override
  public void add(@NotNull final Consumer<Player> o) {
    this.actions.add(o);
  }

  @NotNull
  @Override
  public List<Consumer<Player>> objects() {
    return Collections.unmodifiableList(this.actions);
  }

  @Override
  public void remove(@NotNull final Consumer<Player> o) {
    this.actions.remove(o);
  }

  @Nullable
  @Override
  public String kickMessage() {
    return this.kickMessage;
  }

  @Override
  public void kickMessage(@Nullable final String message) {
    this.kickMessage = message;
  }

  @NotNull
  @Override
  public LoginData loginData() {
    return this.loginData;
  }

  @NotNull
  @Override
  public LoginResult loginResult() {
    return this.loginResult;
  }

  @Override
  public void loginResult(@NotNull final LoginResult loginResult) {
    this.loginResult = loginResult;
  }
}
