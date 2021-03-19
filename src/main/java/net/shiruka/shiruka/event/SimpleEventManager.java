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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import net.shiruka.api.base.ChainData;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.entity.Player;
import net.shiruka.api.event.EventManager;
import net.shiruka.api.event.Listener;
import net.shiruka.api.event.events.Event;
import net.shiruka.api.event.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.event.events.player.PlayerConnectionCloseEvent;
import net.shiruka.api.event.events.player.PlayerKickEvent;
import net.shiruka.api.event.events.player.PlayerLoginEvent;
import net.shiruka.api.event.events.player.PlayerPreLoginEvent;
import net.shiruka.api.event.events.player.PlayerQuitEvent;
import net.shiruka.api.event.events.server.AsyncTabCompleteEvent;
import net.shiruka.api.event.events.server.ServerCommandEvent;
import net.shiruka.api.event.events.server.ServerExceptionEvent;
import net.shiruka.api.event.events.server.ServerTickEndEvent;
import net.shiruka.api.event.events.server.ServerTickStartEvent;
import net.shiruka.api.event.events.server.exception.ServerException;
import net.shiruka.api.event.method.MethodAdapter;
import net.shiruka.api.event.method.SimpleMethodAdapter;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.event.events.player.SimplePlayerAsyncLoginEvent;
import net.shiruka.shiruka.event.events.player.SimplePlayerConnectionCloseEvent;
import net.shiruka.shiruka.event.events.player.SimplePlayerKickEvent;
import net.shiruka.shiruka.event.events.player.SimplePlayerLoginEvent;
import net.shiruka.shiruka.event.events.player.SimplePlayerPreLoginEvent;
import net.shiruka.shiruka.event.events.player.SimplePlayerQuitEvent;
import net.shiruka.shiruka.event.events.server.SimpleAsyncTabCompleteEvent;
import net.shiruka.shiruka.event.events.server.SimpleServerCommandEvent;
import net.shiruka.shiruka.event.events.server.SimpleServerExceptionEvent;
import net.shiruka.shiruka.event.events.server.SimpleServerTickEndEvent;
import net.shiruka.shiruka.event.events.server.SimpleServerTickStartEvent;
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

  @NotNull
  @Override
  public AsyncTabCompleteEvent asyncTabComplete(@NotNull final CommandSender completer,
                                                @NotNull final List<String> completions, @NotNull final String text) {
    return new SimpleAsyncTabCompleteEvent(completer, text, completions);
  }

  @Override
  public void call(@NotNull final Event event) {
    this.adapter.call(event);
  }

  @NotNull
  @Override
  public PlayerAsyncLoginEvent playerAsyncLogin(@NotNull final ChainData chainData) {
    return new SimplePlayerAsyncLoginEvent(chainData);
  }

  @NotNull
  @Override
  public PlayerConnectionCloseEvent playerConnectionClose(@NotNull final InetSocketAddress address,
                                                          @NotNull final Text name, @NotNull final UUID uniqueId,
                                                          @Nullable final String xboxUniqueId) {
    return new SimplePlayerConnectionCloseEvent(address, name, uniqueId, xboxUniqueId);
  }

  @NotNull
  @Override
  public PlayerKickEvent playerKick(@NotNull final Player player, @NotNull final Text kickMessage,
                                    @NotNull final Text leaveMessage) {
    return new SimplePlayerKickEvent(player, kickMessage, leaveMessage);
  }

  @NotNull
  @Override
  public PlayerLoginEvent playerLogin(@NotNull final Player player) {
    return new SimplePlayerLoginEvent(player);
  }

  @NotNull
  @Override
  public PlayerPreLoginEvent playerPreLogin(@NotNull final ChainData chainData, @NotNull final Text kickMessage) {
    return new SimplePlayerPreLoginEvent(chainData, kickMessage);
  }

  @NotNull
  @Override
  public PlayerQuitEvent playerQuit(@NotNull final Player player, @NotNull final Text quitMessage,
                                    @NotNull final PlayerQuitEvent.QuitReason quitReason) {
    return new SimplePlayerQuitEvent(player, quitReason, quitMessage);
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
    return new SimpleServerExceptionEvent(isAsync, serverException);
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
