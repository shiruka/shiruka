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
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.command.CommandManager;
import io.github.shiruka.api.console.ConsoleCommandSender;
import io.github.shiruka.api.events.EventFactory;
import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.api.pack.PackManager;
import io.github.shiruka.api.pack.PackManifest;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.world.WorldLoader;
import io.github.shiruka.shiruka.command.SimpleCommandManager;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.SimpleConsoleCommandSender;
import io.github.shiruka.shiruka.event.SimpleEventFactory;
import io.github.shiruka.shiruka.network.impl.ShirukaServerListener;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.pack.SimplePackManager;
import io.github.shiruka.shiruka.pack.loader.RplDirectory;
import io.github.shiruka.shiruka.pack.loader.RplZip;
import io.github.shiruka.shiruka.pack.pack.ResourcePack;
import io.github.shiruka.shiruka.plugin.InternalPlugin;
import io.github.shiruka.shiruka.scheduler.SimpleScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server {

  /**
   * the internal plugin.
   */
  public static final InternalPlugin INTERNAL_PLUGIN = new InternalPlugin();

  /**
   * obtains the Shiru ka server's version
   */
  public static final String VERSION = "1.0.0";

  /**
   * the packs path.
   */
  private static final Path PACKS_PATH = Misc.HOME_PATH.resolve("packs");

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  /**
   * the server's description.
   */
  @NotNull
  private final String description;

  /**
   * the singleton interface implementations.
   */
  private final Map<Class<?>, Object> interfaces = new ConcurrentHashMap<>();

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
   * @param console the console.
   */
  public ShirukaServer(@NotNull final String description, @NotNull final WorldLoader loader,
                       @NotNull final Function<ServerListener, ServerSocket> socket,
                       @NotNull final Function<Server, ShirukaConsole> console) {
    this.description = description;
    this.loader = loader;
    this.socket = socket.apply(new ShirukaServerListener(this));
    this.console = console.apply(this);
  }

  /**
   * reloads packs.
   */
  private static void reloadPacks() {
    final var manager = Shiruka.getPackManager();
    manager.registerLoader(RplZip.class, RplZip.FACTORY);
    manager.registerLoader(RplDirectory.class, RplDirectory.FACTORY);
    manager.registerPack(PackManifest.PackType.RESOURCES, ResourcePack.FACTORY);
    if (Files.notExists(ShirukaServer.PACKS_PATH)) {
      try {
        Files.createDirectory(ShirukaServer.PACKS_PATH);
      } catch (final IOException e) {
        throw new IllegalStateException("Unable to create packs directory");
      }
    }
    manager.loadPacks(ShirukaServer.PACKS_PATH);
    manager.closeRegistration();
  }

  @NotNull
  @Override
  public <I> I getInterface(@NotNull final Class<I> cls) {
    //noinspection unchecked
    return (I) Objects.requireNonNull(this.interfaces.get(cls),
      String.format("Implementation not found for %s!", cls.toString()));
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
  public <I> void registerInterface(@NotNull final Class<I> cls, @NotNull final I implementation) {
    if (this.interfaces.containsKey(cls)) {
      return;
    }
    this.interfaces.put(cls, implementation);
  }

  @Override
  public void startServer(final long startTime) {
    this.registerImplementations();
    ShirukaServer.reloadPacks();
    this.running.set(true);
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
    this.tick.start();
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

  @Override
  public <I> void unregisterInterface(@NotNull final Class<I> cls) {
    this.interfaces.remove(cls);
  }

  /**
   * registers the implementation of the interfaces which are singleton.
   */
  private void registerImplementations() {
    this.registerInterface(CommandManager.class, new SimpleCommandManager());
    this.registerInterface(ConsoleCommandSender.class, new SimpleConsoleCommandSender(this.console));
    this.registerInterface(EventFactory.class, new SimpleEventFactory());
    this.registerInterface(Scheduler.class, new SimpleScheduler());
    this.registerInterface(PackManager.class, new SimplePackManager());
  }
}
