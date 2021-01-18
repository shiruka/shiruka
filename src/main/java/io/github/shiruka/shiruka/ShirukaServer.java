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

package io.github.shiruka.shiruka;

import io.github.shiruka.api.Server;
import io.github.shiruka.api.base.BanList;
import io.github.shiruka.api.command.CommandManager;
import io.github.shiruka.api.console.ConsoleCommandSender;
import io.github.shiruka.api.events.EventManager;
import io.github.shiruka.api.language.LanguageManager;
import io.github.shiruka.api.pack.PackManager;
import io.github.shiruka.api.pack.PackManifest;
import io.github.shiruka.api.permission.PermissionManager;
import io.github.shiruka.api.plugin.PluginManager;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.api.world.WorldManager;
import io.github.shiruka.shiruka.command.SimpleCommandManager;
import io.github.shiruka.shiruka.concurrent.ShirukaAsyncTaskHandler;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.SimpleConsoleCommandSender;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.event.SimpleEventManager;
import io.github.shiruka.shiruka.language.SimpleLanguageManager;
import io.github.shiruka.shiruka.network.impl.ShirukaServerListener;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.pack.SimplePackManager;
import io.github.shiruka.shiruka.pack.loader.RplDirectory;
import io.github.shiruka.shiruka.pack.loader.RplZip;
import io.github.shiruka.shiruka.pack.pack.ResourcePack;
import io.github.shiruka.shiruka.permission.SimplePermissionManager;
import io.github.shiruka.shiruka.plugin.InternalShirukaPlugin;
import io.github.shiruka.shiruka.plugin.SimplePluginManager;
import io.github.shiruka.shiruka.scheduler.SimpleScheduler;
import io.github.shiruka.shiruka.world.SimpleWorldManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server {

  /**
   * the internal plugin of Shiru ka.
   */
  public static final InternalShirukaPlugin INTERNAL_PLUGIN = new InternalShirukaPlugin();

  /**
   * obtains the Shiru ka server's version
   */
  public static final String VERSION = "1.0.0-SNAPSHOT";

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Shiruka");

  /**
   * the packs path.
   */
  private static final Path PACKS_PATH = Misc.HOME_PATH.resolve("packs");

  /**
   * the command manager.
   */
  @NotNull
  private final SimpleCommandManager commandManager;

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  /**
   * the console command sender.
   */
  @NotNull
  private final SimpleConsoleCommandSender consoleCommandSender;

  /**
   * the server's description.
   */
  @NotNull
  private final String description;

  /**
   * the event manager.
   */
  @NotNull
  private final SimpleEventManager eventManager;

  /**
   * the singleton interface implementations.
   */
  private final Map<Class<?>, Object> interfaces = new ConcurrentHashMap<>();

  /**
   * the language manager.
   */
  @NotNull
  private final SimpleLanguageManager languageManager;

  /**
   * the pack manager.
   */
  @NotNull
  private final SimplePackManager packManager;

  /**
   * the permission manager.
   */
  @NotNull
  private final SimplePermissionManager permissionManager;

  /**
   * the player list.
   */
  private final Map<InetSocketAddress, ShirukaPlayer> players = new ConcurrentHashMap<>();

  /**
   * the plugin manager.
   */
  @NotNull
  private final SimplePluginManager pluginManager;

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the scheduler.
   */
  @NotNull
  private final SimpleScheduler scheduler;

  /**
   * the server thread.
   */
  @NotNull
  private final Thread serverThread;

  /**
   * the socket.
   */
  @NotNull
  private final ServerSocket socket;

  /**
   * the stop lock.
   */
  private final Object stopLock = new Object();

  /**
   * the tick.
   */
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * the task handler.
   */
  private final ShirukaAsyncTaskHandler taskHandler = new ShirukaAsyncTaskHandler(this, this.tick);

  /**
   * the world manager.
   */
  @NotNull
  private final SimpleWorldManager worldManager;

  /**
   * the is stopped.
   */
  private boolean isStopped;

  /**
   * the shutdown thread.
   */
  @Nullable
  private volatile Thread shutdownThread;

  /**
   * ctor.
   *
   * @param description the server's description.
   * @param serverLanguage the server language.
   * @param socket the socket.
   * @param console the console.
   */
  ShirukaServer(@NotNull final String description, @NotNull final Locale serverLanguage,
                @NotNull final Function<ServerListener, ServerSocket> socket,
                @NotNull final Function<Server, ShirukaConsole> console) {
    this.description = description;
    this.socket = socket.apply(new ShirukaServerListener(this));
    this.console = console.apply(this);
    this.serverThread = Thread.currentThread();
    this.commandManager = new SimpleCommandManager();
    this.consoleCommandSender = new SimpleConsoleCommandSender(this.console);
    this.eventManager = new SimpleEventManager();
    this.languageManager = new SimpleLanguageManager(serverLanguage);
    this.packManager = new SimplePackManager();
    this.permissionManager = new SimplePermissionManager();
    this.pluginManager = new SimplePluginManager();
    this.scheduler = new SimpleScheduler();
    this.worldManager = new SimpleWorldManager();
  }

  /**
   * adds the given address and player into {@link #players}.
   *
   * @param player the player to add.
   */
  public void addPlayer(@NotNull final ShirukaPlayer player) {
    this.players.put(player.getPlayerConnection().getConnection().getAddress(), player);
  }

  @NotNull
  @Override
  public BanList getBanList(@NotNull final BanList.Type type) {
    // @todo #1:15m create implementations for ip and profile(name) ban list.
    return null;
  }

  @NotNull
  @Override
  public <I> I getInterface(@NotNull final Class<I> cls) {
    //noinspection unchecked
    return (I) Objects.requireNonNull(this.interfaces.get(cls),
      String.format("Implementation not found for %s!", cls.toString()));
  }

  @NotNull
  @Override
  public Logger getLogger() {
    return ShirukaServer.LOGGER;
  }

  @Override
  public int getMaxPlayerCount() {
    return this.socket.getMaxConnections();
  }

  @Override
  public int getPlayerCount() {
    return this.players.size();
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
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.start_server.starting"));
    this.reloadPacks();
    this.running.set(true);
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.start_server.loading_plugins"));
    // @todo #1:60m Load plugins here.
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.start_server.enabling_plugin"));
    // @todo #1:60m enable plugins which set PluginLoadOrder as STARTUP.
    ShirukaServer.LOGGER.info("§eLoading worlds.");
    // this.worldManager.loadAll();
    ShirukaServer.LOGGER.info("§eEnabling plugins after the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as POST_WORLD.
    final var end = System.currentTimeMillis() - startTime;
    new Thread(this.console::start).start();
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.start_server.done", end));
    this.tick.run();
  }

  @Override
  public void stopServer() {
    this.isStopped = true;
    try {
      this.stop0();
    } catch (final Throwable throwable) {
      ShirukaServer.LOGGER.error(TranslatedText.get("shiruka.server.stop_server.stopping_exception"), throwable);
    }
  }

  @Override
  public <I> void unregisterInterface(@NotNull final Class<I> cls) {
    this.interfaces.remove(cls);
  }

  /**
   * obtains the server thread.
   *
   * @return server thread.
   */
  @NotNull
  public Thread getServerThread() {
    return this.serverThread;
  }

  /**
   * obtains the shutdown thread.
   *
   * @return shutdown thread.
   */
  @Nullable
  public Thread getShutdownThread() {
    return this.shutdownThread;
  }

  /**
   * obtains the task handler.
   *
   * @return task handler.
   */
  @NotNull
  public ShirukaAsyncTaskHandler getTaskHandler() {
    return this.taskHandler;
  }

  /**
   * obtains the tick.
   *
   * @return tick.
   */
  @NotNull
  public ShirukaTick getTick() {
    return this.tick;
  }

  /**
   * checks if the server stopped.
   *
   * @return {@code true} if the server is stopped.
   */
  public boolean isStopped() {
    return this.isStopped;
  }

  /**
   * removes the given player from {@link #players}.
   *
   * @param player the player to remove.
   */
  public void removePlayer(@NotNull final ShirukaPlayer player) {
    this.players.remove(player.getPlayerConnection().getConnection().getAddress());
  }

  /**
   * registers the implementation of the interfaces which are singleton.
   */
  private void registerImplementations() {
    this.registerInterface(ConsoleCommandSender.class, this.consoleCommandSender);
    this.registerInterface(EventManager.class, this.eventManager);
    this.registerInterface(LanguageManager.class, this.languageManager);
    this.registerInterface(PackManager.class, this.packManager);
    this.registerInterface(PermissionManager.class, this.permissionManager);
    this.registerInterface(PluginManager.class, this.pluginManager);
    this.registerInterface(Scheduler.class, this.scheduler);
    this.registerInterface(WorldManager.class, this.worldManager);
    this.registerInterface(CommandManager.class, this.commandManager);
  }

  /**
   * reloads packs.
   */
  private void reloadPacks() {
    ShirukaServer.LOGGER.debug("§7Reloading packs.");
    this.packManager.registerLoader(RplZip.class, RplZip.FACTORY);
    this.packManager.registerLoader(RplDirectory.class, RplDirectory.FACTORY);
    this.packManager.registerPack(PackManifest.PackType.RESOURCES, ResourcePack.FACTORY);
    if (Files.notExists(ShirukaServer.PACKS_PATH)) {
      try {
        Files.createDirectory(ShirukaServer.PACKS_PATH);
      } catch (final IOException e) {
        throw new IllegalStateException("Unable to create packs directory");
      }
    }
    this.packManager.loadPacks(ShirukaServer.PACKS_PATH);
    this.packManager.closeRegistration();
  }

  /**
   * stops the server.
   */
  private void stop0() {
    ShirukaServer.LOGGER.info("§eStopping the server.");
    synchronized (this.stopLock) {
      if (this.running.get()) {
        return;
      }
      this.running.set(false);
    }
    this.shutdownThread = Thread.currentThread();
  }
}
