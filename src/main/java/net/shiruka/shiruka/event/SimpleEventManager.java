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

import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.entity.Player;
import net.shiruka.api.event.Listener;
import net.shiruka.api.event.method.MethodAdapter;
import net.shiruka.api.event.method.SimpleMethodAdapter;
import net.shiruka.api.events.ChainDataEvent;
import net.shiruka.api.events.Event;
import net.shiruka.api.events.EventManager;
import net.shiruka.api.events.LoginResultEvent;
import net.shiruka.api.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.events.player.PlayerKickEvent;
import net.shiruka.api.events.player.PlayerLoginEvent;
import net.shiruka.api.events.player.PlayerPreLoginEvent;
import net.shiruka.api.events.server.ServerCommandEvent;
import net.shiruka.api.events.server.ServerExceptionEvent;
import net.shiruka.api.events.server.ServerTickEndEvent;
import net.shiruka.api.events.server.ServerTickStartEvent;
import net.shiruka.api.events.server.exception.ServerException;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.events.player.SimplePlayerAsyncLoginEvent;
import net.shiruka.shiruka.events.player.SimplePlayerKickEvent;
import net.shiruka.shiruka.events.player.SimplePlayerLoginEvent;
import net.shiruka.shiruka.events.player.SimplePlayerPreLoginEvent;
import net.shiruka.shiruka.events.server.SimpleServerCommandEvent;
import net.shiruka.shiruka.events.server.SimpleServerExceptionEvent;
import net.shiruka.shiruka.events.server.SimpleServerTickEndEvent;
import net.shiruka.shiruka.events.server.SimpleServerTickStartEvent;
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
  public PlayerAsyncLoginEvent playerAsyncLogin(@NotNull final ChainDataEvent.ChainData chainData) {
    return new SimplePlayerAsyncLoginEvent(chainData);
  }

  @NotNull
  @Override
  public PlayerKickEvent playerKick(@NotNull final Player player, @NotNull final LoginResultEvent.LoginResult reason,
                                    @NotNull final Text kickMessage) {
    return new SimplePlayerKickEvent(player, reason, kickMessage);
  }

  @NotNull
  @Override
  public PlayerLoginEvent playerLogin(@NotNull final Player player) {
    return new SimplePlayerLoginEvent(player);
  }

  @NotNull
  @Override
  public PlayerPreLoginEvent playerPreLogin(@NotNull final ChainDataEvent.ChainData chainData,
                                            @Nullable final Text kickMessage) {
    return new SimplePlayerPreLoginEvent(chainData, kickMessage);
  }

  @Override
  public void register(@NotNull final Listener listener) {
    this.adapter.register(listener);
  }

  @NotNull
  @Override
  public ServerCommandEvent serverCommand(@NotNull final CommandSender sender, @NotNull final String command) {
    return new SimpleServerCommandEvent(sender, command);
  }

  @NotNull
  @Override
  public ServerExceptionEvent serverException(@NotNull final ServerException serverException,
                                              final boolean isAsync) {
    return new SimpleServerExceptionEvent(serverException, isAsync);
  }

  @NotNull
  @Override
  public ServerTickEndEvent serverTickEnd(final int tick, final double duration, final long remaining) {
    return new SimpleServerTickEndEvent(duration, remaining, tick);
  }

  @NotNull
  @Override
  public ServerTickStartEvent serverTickStart(final int tick) {
    return new SimpleServerTickStartEvent(tick);
  }

  @Override
  public void unregister(@NotNull final Listener listener) {
    this.adapter.unregister(listener);
  }
}
