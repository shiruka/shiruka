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

package net.shiruka.shiruka;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.shiruka.api.Server;
import net.shiruka.api.base.BanList;
import net.shiruka.api.base.GameMode;
import net.shiruka.api.command.CommandManager;
import net.shiruka.api.command.sender.ConsoleCommandSender;
import net.shiruka.api.entity.Player;
import net.shiruka.api.events.EventManager;
import net.shiruka.api.language.LanguageManager;
import net.shiruka.api.pack.PackManager;
import net.shiruka.api.pack.PackManifest;
import net.shiruka.api.permission.PermissionManager;
import net.shiruka.api.plugin.PluginManager;
import net.shiruka.api.plugin.SimplePluginManager;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.server.ServerDescription;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.api.world.WorldManager;
import net.shiruka.shiruka.command.SimpleCommandManager;
import net.shiruka.shiruka.command.SimpleConsoleCommandSender;
import net.shiruka.shiruka.concurrent.tasks.ShirukaAsyncTaskHandler;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.console.ShirukaConsole;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.event.SimpleEventManager;
import net.shiruka.shiruka.language.SimpleLanguageManager;
import net.shiruka.shiruka.network.impl.ShirukaServerListener;
import net.shiruka.shiruka.network.server.ServerListener;
import net.shiruka.shiruka.network.server.ServerSocket;
import net.shiruka.shiruka.network.util.Constants;
import net.shiruka.shiruka.network.util.Misc;
import net.shiruka.shiruka.pack.SimplePackManager;
import net.shiruka.shiruka.pack.loader.RplDirectory;
import net.shiruka.shiruka.pack.loader.RplZip;
import net.shiruka.shiruka.pack.pack.ResourcePack;
import net.shiruka.shiruka.permission.SimplePermissionManager;
import net.shiruka.shiruka.plugin.InternalShirukaPlugin;
import net.shiruka.shiruka.scheduler.SimpleScheduler;
import net.shiruka.shiruka.world.SimpleWorldManager;
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
  private final ServerDescription description;

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
   * @param console the console.
   * @param serverLanguage the server language.
   * @param socket the socket.
   */
  ShirukaServer(@NotNull final Function<Server, ShirukaConsole> console, @NotNull final Locale serverLanguage,
                @NotNull final Function<ServerListener, ServerSocket> socket) throws ExecutionException,
    InterruptedException {
    this.console = console.apply(this);
    this.socket = socket.apply(new ShirukaServerListener(this));
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
    this.description = this.createDefaultDescription();
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

  @NotNull
  @Override
  public Collection<? extends Player> getOnlinePlayers() {
    return this.players.values();
  }

  @Override
  public int getPlayerCount() {
    return this.players.size();
  }

  @NotNull
  @Override
  public CompletableFuture<ServerDescription> getServerDescription(final boolean forceUpdate) {
    if (forceUpdate) {
      return this.updateDescription();
    }
    return CompletableFuture.completedFuture(this.description);
  }

  @Override
  public boolean isInShutdownState() {
    return !this.running.get();
  }

  @Override
  public boolean isPrimaryThread() {
    final var current = Thread.currentThread();
    return current.equals(this.serverThread) || current.equals(this.shutdownThread);
  }

  @Override
  public boolean isRunning() {
    return this.running.get();
  }

  @Override
  public boolean isStopping() {
    synchronized (this.stopLock) {
      return this.isStopped;
    }
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
   * creates a default server description instance from the server config values.
   *
   * @return a newly created server description instance.
   */
  @NotNull
  private ServerDescription createDefaultDescription() {
    final var gameMode = GameMode.fromType(ServerConfig.DESCRIPTION_GAME_MODE.getValue().orElse("survival"))
      .orElse(GameMode.SURVIVAL);
    final var ipv4Port = ServerConfig.DESCRIPTION_IPV4_PORT.getValue().orElse(19132);
    final var ipv6Port = ServerConfig.DESCRIPTION_IPV6_PORT.getValue().orElse(19133);
    final var maxPlayers = ServerConfig.DESCRIPTION_MAX_PLAYERS.getValue().orElse(10);
    final var motd = ServerConfig.DESCRIPTION_MOTD.getValue().orElse("");
    final var subMotd = ServerConfig.DESCRIPTION_SUB_MOTD.getValue().orElse("");
    final var extras = ServerConfig.DESCRIPTION_EXTRAS.getValue().orElse(Collections.emptyList())
      .toArray(new String[0]);
    return new ServerDescription(gameMode, ipv4Port, ipv6Port, maxPlayers, Constants.MINECRAFT_PROTOCOL_VERSION,
      this.socket.getUniqueId(), Constants.MINECRAFT_VERSION, motd, ServerDescription.Edition.MCPE, extras,
      this.players.size(), subMotd);
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

  /**
   * updates the server description and returns it.
   *
   * @return updated server description.
   */
  @NotNull
  private CompletableFuture<ServerDescription> updateDescription() {
    return CompletableFuture.supplyAsync(() -> {
      final var motd = ServerConfig.DESCRIPTION_MOTD.getValue().orElse("");
      final var subMotd = ServerConfig.DESCRIPTION_SUB_MOTD.getValue().orElse("");
      final var extras = ServerConfig.DESCRIPTION_EXTRAS.getValue().orElse(Collections.emptyList())
        .toArray(new String[0]);
      this.description.setDescription(motd);
      this.description.setExtras(extras);
      this.description.setPlayerCount(this.players.size());
      this.description.setSubDescription(subMotd);
      return this.description;
    });
  }
}
