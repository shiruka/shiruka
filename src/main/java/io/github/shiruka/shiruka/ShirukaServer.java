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

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.shiruka.api.Server;
import io.github.shiruka.api.Shiruka;
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
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.SimpleConsoleCommandSender;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.event.SimpleEventManager;
import io.github.shiruka.shiruka.language.SimpleLanguageManager;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.network.impl.ShirukaServerListener;
import io.github.shiruka.shiruka.network.server.ServerListener;
import io.github.shiruka.shiruka.network.server.ServerSocket;
import io.github.shiruka.shiruka.network.util.Misc;
import io.github.shiruka.shiruka.pack.SimplePackManager;
import io.github.shiruka.shiruka.pack.loader.RplDirectory;
import io.github.shiruka.shiruka.pack.loader.RplZip;
import io.github.shiruka.shiruka.pack.pack.ResourcePack;
import io.github.shiruka.shiruka.permission.SimplePermissionManager;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Shiruka");

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
   * the main thread.
   */
  @NotNull
  private final Thread mainThread;

  /**
   * the player list.
   */
  private final Map<InetSocketAddress, ShirukaPlayer> players = new ConcurrentHashMap<>();

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the scheduler service.
   */
  private final ListeningScheduledExecutorService schedulerService = MoreExecutors.listeningDecorator(
    Executors.newScheduledThreadPool(4, PoolSpec.SCHEDULER));

  /**
   * the server language.
   */
  @NotNull
  private final Locale serverLanguage;

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
   * @param serverLanguage the server language.
   * @param socket the socket.
   * @param console the console.
   */
  ShirukaServer(@NotNull final String description, @NotNull final Locale serverLanguage,
                @NotNull final Function<ServerListener, ServerSocket> socket,
                @NotNull final Function<Server, ShirukaConsole> console) {
    this.description = description;
    this.serverLanguage = serverLanguage;
    this.socket = socket.apply(new ShirukaServerListener(this));
    this.console = console.apply(this);
    this.mainThread = Thread.currentThread();
  }

  /**
   * reloads packs.
   */
  private static void reloadPacks() {
    ShirukaServer.LOGGER.debug("§7Reloading packs.");
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
    // Translation is active after here.
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.starting"));
    ShirukaServer.reloadPacks();
    this.running.set(true);
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.loading_plugins"));
    // @todo #1:60m Load plugins here.
    ShirukaServer.LOGGER.info(TranslatedText.get("shiruka.server.enabling_plugin"));
    // @todo #1:60m enable plugins which set PluginLoadOrder as STARTUP.
    ShirukaServer.LOGGER.info("§eLoading worlds.");
//    this.worldManager.loadAll();
    ShirukaServer.LOGGER.info("§eEnabling plugins after the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as POST_WORLD.
    final var end = System.currentTimeMillis() - startTime;
    new Thread(this.console::start).start();
    ShirukaServer.LOGGER.info("§aDone, took {}ms.", end);
    this.tick.run();
  }

  @Override
  public void stopServer() {
    ShirukaServer.LOGGER.info("§eStopping the server.");
    this.running.set(false);
    try {
      this.socket.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    ServerThreadPool.shutdownAll();
    var wait = 50;
    this.schedulerService.shutdown();
    while (!this.schedulerService.isTerminated() && wait-- > 0) {
      try {
        this.schedulerService.awaitTermination(100, TimeUnit.MILLISECONDS);
      } catch (final InterruptedException e) {
        JiraExceptionCatcher.serverException(e);
      }
    }
    if (wait <= 0) {
      final var remainRunning = this.schedulerService.shutdownNow();
      remainRunning.forEach(runnable ->
        ShirukaServer.LOGGER.warn("Runnable {} has been terminated due to shutdown", runnable.getClass().getName()));
    }
    System.exit(0);
  }

  @Override
  public <I> void unregisterInterface(@NotNull final Class<I> cls) {
    this.interfaces.remove(cls);
  }

  /**
   * obtains the scheduler service.
   *
   * @return scheduler service.
   */
  @NotNull
  public ListeningScheduledExecutorService getSchedulerService() {
    return this.schedulerService;
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
    this.registerInterface(CommandManager.class, new SimpleCommandManager());
    this.registerInterface(ConsoleCommandSender.class, new SimpleConsoleCommandSender(this.console));
    this.registerInterface(EventManager.class, new SimpleEventManager());
    this.registerInterface(LanguageManager.class, new SimpleLanguageManager(this.serverLanguage));
    this.registerInterface(PackManager.class, new SimplePackManager());
    this.registerInterface(PermissionManager.class, new SimplePermissionManager());
    this.registerInterface(PluginManager.class, new SimplePluginManager());
    this.registerInterface(Scheduler.class, new SimpleScheduler(this.schedulerService));
    this.registerInterface(WorldManager.class, new SimpleWorldManager());
  }
}
