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

import io.github.shiruka.api.events.Event;
import io.github.shiruka.api.events.player.PlayerPreLoginEvent;
import io.github.shiruka.shiruka.event.player.SimplePlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an utility class that helps to call/create {@link Event}.
 */
public final class Events {

  /**
   * ctor.
   */
  private Events() {
  }

  /**
   * creates a new {@link SimplePlayerPreLoginEvent} instance.
   *
   * @param loginData the login data to create.
   * @param kickMessage the kick message to create.
   *
   * @return a new instance of {@link PlayerPreLoginEvent}.
   */
  @NotNull
  public static PlayerPreLoginEvent createPlayerPreLoginEvent(@NotNull final PlayerPreLoginEvent.LoginData loginData,
                                                              @Nullable final String kickMessage) {
    return new SimplePlayerPreLoginEvent(loginData, kickMessage);
  }

  /**
   * creates a new {@link SimplePlayerPreLoginEvent} instance.
   *
   * @param loginData the login data to create.
   *
   * @return a new instance of {@link PlayerPreLoginEvent}.
   */
  @NotNull
  public static PlayerPreLoginEvent createPlayerPreLoginEvent(@NotNull final PlayerPreLoginEvent.LoginData loginData) {
    return Events.createPlayerPreLoginEvent(loginData, null);
  }
}
