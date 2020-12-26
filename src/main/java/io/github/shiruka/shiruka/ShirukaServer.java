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

package io.github.shiruka.shiruka;

import io.github.shiruka.api.Server;
import io.github.shiruka.api.command.CommandManager;
import io.github.shiruka.api.command.CommandSender;
import io.github.shiruka.api.events.EventFactory;
import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.world.WorldLoader;
import io.github.shiruka.shiruka.command.SimpleCommandManager;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.SimpleConsoleCommandSender;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.entity.ShirukaPlayerConnection;
import io.github.shiruka.shiruka.event.SimpleEventFactory;
import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.impl.ShirukaServerListener;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.scheduler.SimpleScheduler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server {

  /**
   * obtains the Shiru ka server's version
   */
  public static final String VERSION = "1.0.0";

  /**
   * the command manager.
   */
  private final CommandManager commandManager = new SimpleCommandManager();

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  /**
   * the console command sender.
   */
  @NotNull
  private final CommandSender consoleCommandSender;

  /**
   * the server's description.
   */
  @NotNull
  private final String description;

  /**
   * the event factory.
   */
  private final EventFactory eventFactory = new SimpleEventFactory();

  /**
   * the loader.
   */
  @NotNull
  private final WorldLoader loader;

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the scheduler.
   */
  private final Scheduler scheduler = new SimpleScheduler();

  /**
   * the socket.
   */
  @NotNull
  private final ServerSocket socket;

  /**
   * the tick.
   */
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * ctor.
   *
   * @param description the server's description.
   * @param loader the world loader.
   * @param socket the socket.
   */
  public ShirukaServer(@NotNull final String description, @NotNull final WorldLoader loader,
                       @NotNull final Function<ServerListener, ServerSocket> socket,
                       @NotNull final Function<Server, ShirukaConsole> console) {
    this.description = description;
    this.loader = loader;
    this.socket = socket.apply(new ShirukaServerListener(this));
    this.console = console.apply(this);
    this.consoleCommandSender = new SimpleConsoleCommandSender(this.console);
  }

  /**
   * creates a new {@link ShirukaPlayer} instance.
   *
   * @param connection the connection to create.
   *
   * @return a new player instance.
   */
  @NotNull
  public ShirukaPlayer createPlayer(@NotNull final Connection<ServerSocket> connection) {
    return new ShirukaPlayer(new ShirukaPlayerConnection(connection, this));
  }

  @NotNull
  @Override
  public CommandManager getCommandManager() {
    return this.commandManager;
  }

  @NotNull
  @Override
  public CommandSender getConsoleCommandSender() {
    return this.consoleCommandSender;
  }

  @NotNull
  @Override
  public EventFactory getEventFactory() {
    return this.eventFactory;
  }

  @Override
  public int getMaxPlayerCount() {
    return this.socket.getMaxConnections();
  }

  @Override
  public int getPlayerCount() {
    return this.socket.getConnectionsByAddress().size();
  }

  @NotNull
  @Override
  public Scheduler getScheduler() {
    return this.scheduler;
  }

  @NotNull
  @Override
  public String getServerDescription() {
    return this.description;
  }

  @Override
  public boolean isInShutdownState() {
    return !this.running.get();
  }

  @Override
  public boolean isRunning() {
    return this.running.get();
  }

  @Override
  public void startServer(final long startTime) {
    this.running.set(true);
    this.tick.start();
    Loggers.log("Loading plugins.");
    // @todo #1:60m Load plugins here.
    Loggers.log("Enabling startup plugins before the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as STARTUP.
    Loggers.log("Loading worlds.");
    this.loader.loadAll();
    Loggers.log("Enabling plugins after the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as POST_WORLD.
    final var end = System.currentTimeMillis() - startTime;
    Loggers.log("Done, took %sms.", end);
    this.console.start();
  }

  @Override
  public void stopServer() {
    this.running.set(false);
    try {
      this.socket.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    ServerThreadPool.shutdownAll();
    System.exit(0);
  }
}
