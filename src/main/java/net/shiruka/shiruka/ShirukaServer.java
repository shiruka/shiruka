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

import com.google.common.base.Preconditions;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.identifier.MinecraftIdentifier;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.server.RakNetServer;
import com.whirvis.jraknet.server.RakNetServerListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import net.shiruka.api.Server;
import net.shiruka.api.base.BanList;
import net.shiruka.api.command.CommandManager;
import net.shiruka.api.command.sender.ConsoleCommandSender;
import net.shiruka.api.events.EventManager;
import net.shiruka.api.language.LanguageManager;
import net.shiruka.api.pack.PackManager;
import net.shiruka.api.permission.PermissionManager;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.plugin.PluginManager;
import net.shiruka.api.plugin.SimplePluginManager;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.api.world.WorldManager;
import net.shiruka.shiruka.base.PlayerList;
import net.shiruka.shiruka.command.SimpleCommandManager;
import net.shiruka.shiruka.command.SimpleConsoleCommandSender;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.config.UserCacheConfig;
import net.shiruka.shiruka.config.WhitelistConfig;
import net.shiruka.shiruka.console.ShirukaConsole;
import net.shiruka.shiruka.entities.ShirukaPlayer;
import net.shiruka.shiruka.event.SimpleEventManager;
import net.shiruka.shiruka.language.SimpleLanguageManager;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.network.Protocol;
import net.shiruka.shiruka.pack.SimplePackManager;
import net.shiruka.shiruka.permission.SimplePermissionManager;
import net.shiruka.shiruka.plugin.InternalShirukaPlugin;
import net.shiruka.shiruka.scheduler.SimpleScheduler;
import net.shiruka.shiruka.text.TranslatedTexts;
import net.shiruka.shiruka.util.SystemUtils;
import net.shiruka.shiruka.world.SimpleWorldManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server, RakNetServerListener {

  /**
   * the internal plugin of Shiru ka.
   */
  public static final Plugin INTERNAL_PLUGIN = new InternalShirukaPlugin();

  /**
   * obtains the Shiru ka server's version.
   */
  public static final String VERSION = "1.0.0-SNAPSHOT";

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * the server done.
   */
  private static final String SERVER_DONE = "shiruka.server.done";

  /**
   * the server stoppin exception.
   */
  private static final String SERVER_STOPPING_EXCEPTION = "shiruka.server.stopping_exception";

  /**
   * the player list.
   */
  public final PlayerList playerList = new PlayerList(this);

  /**
   * the command manager.
   */
  private final SimpleCommandManager commandManager = new SimpleCommandManager();

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
   * the event manager.
   */
  private final SimpleEventManager eventManager = new SimpleEventManager();

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
  private final SimplePackManager packManager = new SimplePackManager();

  /**
   * the permission manager.
   */
  private final SimplePermissionManager permissionManager = new SimplePermissionManager();

  /**
   * the plugin manager.
   */
  private final SimplePluginManager pluginManager = new SimplePluginManager();

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the scheduler.
   */
  private final SimpleScheduler scheduler = new SimpleScheduler();

  /**
   * the server thread.
   */
  private final Thread serverThread = Thread.currentThread();

  /**
   * the socket.
   */
  @NotNull
  private final RakNetServer socket;

  /**
   * the start time.
   */
  private final long startTime;

  /**
   * the stop lock.
   */
  private final Object stopLock = new Object();

  /**
   * the tick.
   */
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * the world manager.
   */
  private final SimpleWorldManager worldManager = new SimpleWorldManager();

  /**
   * the has fully shutdown.
   */
  public volatile boolean hasFullyShutdown = false;

  /**
   * the has stopped.
   */
  public boolean hasStopped;

  /**
   * the is stopped.
   */
  public boolean isStopped;

  /**
   * the is restarting.
   */
  private volatile boolean isRestarting = false;

  /**
   * the shutdown thread.
   */
  @Nullable
  private volatile Thread shutdownThread;

  /**
   * ctor.
   *
   * @param startTime the start time.
   * @param console the console.
   * @param serverLanguage the server language.
   * @param socket the socket.
   */
  ShirukaServer(final long startTime, @NotNull final Function<ShirukaServer, ShirukaConsole> console,
                @NotNull final Locale serverLanguage, @NotNull final RakNetServer socket) {
    this.startTime = startTime;
    this.console = console.apply(this);
    this.socket = socket;
    this.consoleCommandSender = new SimpleConsoleCommandSender(this.console);
    this.languageManager = new SimpleLanguageManager(serverLanguage);
  }

  @NotNull
  @Override
  public BanList getBanList(@NotNull final BanList.Type type) {
    if (type == BanList.Type.IP) {
      return this.playerList.ipBanList;
    }
    return this.playerList.profileBanList;
  }

  @NotNull
  @Override
  public <I> I getInterface(@NotNull final Class<I> cls) {
    final var implementation = this.interfaces.get(cls);
    Preconditions.checkArgument(implementation != null,
      "Implementation not found for %s!", cls.toString());
    //noinspection unchecked
    return (I) implementation;
  }

  @NotNull
  @Override
  public Logger getLogger() {
    return ShirukaServer.LOGGER;
  }

  @Override
  public int getMaxPlayers() {
    return this.playerList.maxPlayers;
  }

  @Override
  public void setMaxPlayers(final int maxPlayers) {
    this.playerList.maxPlayers = maxPlayers;
  }

  @NotNull
  @Override
  public Collection<? extends ShirukaPlayer> getOnlinePlayers() {
    return this.playerList.getPlayers();
  }

  @Override
  public boolean isInShutdownState() {
    return !this.running.get();
  }

  @Override
  public boolean isInWhitelist(@NotNull final UUID uniqueId) {
    return WhitelistConfig.isInWhitelist(uniqueId);
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
      return this.hasStopped;
    }
  }

  @Override
  public boolean isWhitelistOn() {
    return ServerConfig.WHITE_LIST.getValue().orElse(false);
  }

  @Override
  public <I> void registerInterface(@NotNull final Class<I> cls, @NotNull final I implementation) {
    this.interfaces.put(cls, implementation);
  }

  @Override
  public void startServer() {
    this.registerImplementations();
    this.getLogger().info(TranslatedTexts.SERVER_STARTING);
    this.packManager.reloadPacks();
    this.running.set(true);
    this.getLogger().info(TranslatedTexts.LOADING_PLUGINS);
    // @todo #1:60m Load plugins here.
    this.getLogger().info(TranslatedTexts.ENABLING_PLUGINS_BEFORE_WORLDS);
    // @todo #1:60m enable plugins which set PluginLoadOrder as STARTUP.
    this.getLogger().info("§eLoading worlds.");
    // this.worldManager.loadAll();
    this.getLogger().info("§eEnabling plugins after the loading worlds.");
    // @todo #1:60m enable plugins which set PluginLoadOrder as POST_WORLD.
    final var consoleThread = new Thread(this.console::start);
    consoleThread.setDaemon(true);
    consoleThread.setUncaughtExceptionHandler((t, e) -> this.getLogger()
      // @todo #1:5m Add language support for console's uncaught exception handler.
      .error("Caught previously unhandled exception :", e));
    consoleThread.start();
    this.tick.nextTick = SystemUtils.getMonotonicMillis();
    this.scheduler.mainThreadHeartbeat(0);
    final var end = System.currentTimeMillis() - this.startTime;
    this.getLogger().info(TranslatedText.get(ShirukaServer.SERVER_DONE, end));
    this.tick.run();
    this.stopServer();
  }

  @Override
  public void stopServer() {
    try {
      this.stop0();
    } catch (final Throwable throwable) {
      this.getLogger().error(TranslatedText.get(ShirukaServer.SERVER_STOPPING_EXCEPTION), throwable);
    }
  }

  @Override
  public <I> void unregisterInterface(@NotNull final Class<I> cls) {
    this.interfaces.remove(cls);
  }

  /**
   * obtains the scheduler.
   *
   * @return scheduler.
   */
  @NotNull
  public SimpleScheduler getScheduler() {
    return this.scheduler;
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
   * obtains the tick.
   *
   * @return tick.
   */
  @NotNull
  public ShirukaTick getTick() {
    return this.tick;
  }

  /**
   * obtains the is stopped.
   *
   * @return is stopped.
   */
  public boolean isStopped() {
    return this.isStopped;
  }

  @Override
  public void onStart(final RakNetServer server) {
    this.startServer();
  }

  @Override
  public void onLogin(final RakNetServer server, final RakNetClientPeer peer) {
    this.tick.pending.enqueue(peer);
  }

  @Override
  public void handleMessage(final RakNetServer server, final RakNetClientPeer peer, final RakNetPacket packet,
                            final int channel) {
    final var address = peer.getAddress();
    if (!this.tick.connectedPlayers.containsKey(address)) {
      return;
    }
    final var handler = this.tick.connectedPlayers.get(address).getPacketHandler();
    if (packet.getId() == 0xfe) {
      packet.buffer().markReaderIndex();
      Protocol.deserialize(handler, packet.buffer());
    }
  }

  @Override
  public void onHandlerException(final RakNetServer server, final InetSocketAddress address,
                                 final Throwable throwable) {
    throwable.printStackTrace();
  }

  /**
   * tries to safe shutdown the server.
   *
   * @param flag the flat to shutdown.
   */
  public void safeShutdown(final boolean flag) {
    this.safeShutdown(flag, false);
  }

  /**
   * tries to safe shutdown the server.
   *
   * @param flag the flat to shutdown.
   * @param isRestarting the is restart to shutdown.
   */
  public void safeShutdown(final boolean flag, final boolean isRestarting) {
    this.running.set(false);
    this.isRestarting = isRestarting;
    if (flag) {
      try {
        this.serverThread.join();
      } catch (final InterruptedException e) {
        this.getLogger().error("Error while shutting down", e);
      }
    }
  }

  /**
   * updates the server ping.
   */
  public void updatePing() {
    final var identifier = (MinecraftIdentifier) this.socket.getIdentifier();
    identifier.setServerName(ServerConfig.DESCRIPTION_MOTD.getValue().orElse(""));
    identifier.setWorldName(ServerConfig.DEFAULT_WORLD_NAME.getValue().orElse("world"));
    identifier.setMaxPlayerCount(this.getMaxPlayers());
    identifier.setOnlinePlayerCount(this.getOnlinePlayers().size());
  }

  /**
   * checks if {@link Thread#currentThread()} is {@link #serverThread}.
   *
   * @return {@code true} if current thread is the main thread.
   */
  private boolean isMainThread() {
    return this.tick.isMainThread();
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
    JiraExceptionCatcher.run(() -> {
      Class.forName("net.shiruka.shiruka.network.PacketRegistry");
      Class.forName("net.shiruka.shiruka.command.SimpleCommandManager");
      Class.forName("net.shiruka.shiruka.text.TranslatedTexts");
    });
  }

  /**
   * stops the server.
   */
  private void stop0() {
    synchronized (this.stopLock) {
      if (this.hasStopped) {
        return;
      }
      this.hasStopped = true;
    }
    this.shutdownThread = Thread.currentThread();
    if (!this.isMainThread()) {
      this.getLogger().info("§eStopping main thread.");
      while (this.serverThread.isAlive()) {
        this.serverThread.stop();
        try {
          Thread.sleep(1);
        } catch (final InterruptedException ignored) {
        }
      }
    }
    this.getLogger().info("§eStopping the server.");
    // @todo #1:15m disable plugins here and wait for async tasks shutdown.
    this.socket.shutdown();
    // @todo #1:15m save all players data here.
    this.getLogger().info("§eSaving worlds.");
    // @todo #1:15m save and close all worlds here.
    if (ServerConfig.SAVE_USER_CACHE_ON_STOP_ONLY.getValue().orElse(false)) {
      this.getLogger().info("§eSaving usercache.json.");
      UserCacheConfig.getInstance().save();
    }
    this.getLogger().info("§eClosing Server");
    try {
      TerminalConsoleAppender.close();
    } catch (final IOException ignored) {
    }
    this.hasFullyShutdown = true;
    System.exit(0);
  }
}
