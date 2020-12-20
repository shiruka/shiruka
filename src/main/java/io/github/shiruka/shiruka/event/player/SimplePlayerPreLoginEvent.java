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

package io.github.shiruka.shiruka.event.player;

import io.github.shiruka.api.events.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerPreLoginEvent}.
 */
public final class SimplePlayerPreLoginEvent implements PlayerPreLoginEvent {

  /**
   * the login data.
   */
  @NotNull
  private final LoginData loginData;

  /**
   * the cancelled.
   */
  private boolean cancelled;

  /**
   * the kick message.
   */
  @Nullable
  private String kickMessage;

  /**
   * ctor.
   *
   * @param loginData the login data.
   * @param kickMessage the kick message.
   */
  public SimplePlayerPreLoginEvent(@NotNull final LoginData loginData, @Nullable final String kickMessage) {
    this.loginData = loginData;
    this.kickMessage = kickMessage;
  }

  /**
   * ctor.
   *
   * @param loginData the login data.
   */
  public SimplePlayerPreLoginEvent(@NotNull final LoginData loginData) {
    this(loginData, null);
  }

  @Override
  public void cancel() {
    this.cancelled = true;
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void dontCancel() {
    this.cancelled = false;
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
}
