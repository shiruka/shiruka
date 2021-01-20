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

package net.shiruka.shiruka.event;

import net.shiruka.api.entity.Player;
import net.shiruka.api.event.Listener;
import net.shiruka.api.event.method.MethodAdapter;
import net.shiruka.api.event.method.SimpleMethodAdapter;
import net.shiruka.api.events.Event;
import net.shiruka.api.events.EventManager;
import net.shiruka.api.events.KickEvent;
import net.shiruka.api.events.LoginDataEvent;
import net.shiruka.api.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.events.player.PlayerKickEvent;
import net.shiruka.api.events.player.PlayerPreLoginEvent;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.events.player.SimplePlayerAsyncLoginEvent;
import net.shiruka.shiruka.events.player.SimplePlayerKickEvent;
import net.shiruka.shiruka.events.player.SimplePlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an utility class that helps to call/create {@link Event}.
 */
public final class SimpleEventManager implements EventManager {

  /**
   * the adapter.
   */
  private final MethodAdapter adapter = new SimpleMethodAdapter();

  @Override
  public void call(@NotNull final Event event) {
    this.adapter.call(event);
  }

  @NotNull
  @Override
  public PlayerAsyncLoginEvent playerAsyncLogin(@NotNull final LoginDataEvent.LoginData loginData) {
    return new SimplePlayerAsyncLoginEvent(loginData);
  }

  @NotNull
  @Override
  public PlayerKickEvent playerKick(@NotNull final Player player, @NotNull final KickEvent.Reason reason,
                                    @NotNull final Text kickMessage) {
    return new SimplePlayerKickEvent(player, reason, kickMessage);
  }

  @NotNull
  @Override
  public PlayerPreLoginEvent playerPreLogin(@NotNull final LoginDataEvent.LoginData loginData,
                                            @Nullable final Text kickMessage) {
    return new SimplePlayerPreLoginEvent(loginData, kickMessage);
  }

  @Override
  public void register(@NotNull final Listener listener) {
    this.adapter.register(listener);
  }

  @Override
  public void unregister(@NotNull final Listener listener) {
    this.adapter.unregister(listener);
  }
}
