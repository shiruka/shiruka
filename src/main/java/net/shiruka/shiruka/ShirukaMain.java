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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.whirvis.jraknet.identifier.MinecraftIdentifier;
import com.whirvis.jraknet.server.RakNetServer;
import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.Shiruka;
import net.shiruka.api.util.ThrowableRunnable;
import net.shiruka.shiruka.config.IpBanConfig;
import net.shiruka.shiruka.config.OpsConfig;
import net.shiruka.shiruka.config.ProfileBanConfig;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.config.UserCacheConfig;
import net.shiruka.shiruka.config.WhitelistConfig;
import net.shiruka.shiruka.console.ShirukaConsole;
import net.shiruka.shiruka.console.ShirukaConsoleParser;
import net.shiruka.shiruka.language.Languages;
import net.shiruka.shiruka.log.ForwardLogHandler;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.util.AsyncCatcher;
import net.shiruka.shiruka.util.ShirukaShutdownThread;
import net.shiruka.shiruka.util.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.io.IoBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
@Log4j2
public final class ShirukaMain {

  /**
   * the async executor.
   */
  public static final ThreadPoolExecutor ASYNC_EXECUTOR = new ThreadPoolExecutor(
    0, 2, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(),
    new ThreadFactoryBuilder()
      .setNameFormat("Shiru ka Async Task Handler Thread - %1$d")
      .build());

  /**
   * the protocol version of the Minecraft game.
   */
  public static final short MINECRAFT_PROTOCOL_VERSION = 428;

  /**
   * the version of the Minecraft game.
   */
  public static final String MINECRAFT_VERSION = "1.16.210";

  /**
   * the start time.
   */
  public static final AtomicLong START_TIME = new AtomicLong();

  /**
   * the working directory as a string.
   */
  private static final String HOME = System.getProperty("user.dir");

  /**
   * the Path directory to the working dir.
   */
  public static final Path HOME_PATH = Paths.get(ShirukaMain.HOME);

  /**
   * the server runnable.
   */
  private static final ThrowableRunnable SERVER_RUNNABLE = () -> {
    final var socket = ShirukaMain.createSocket();
    final var server = new ShirukaServer(ShirukaConsole::new, socket);
    AsyncCatcher.server = server;
    Shiruka.setServer(server);
    Runtime.getRuntime().addShutdownHook(new ShirukaShutdownThread(server));
    socket.addListener(server);
    socket.start();
  };

  /**
   * the ip bans file.
   */
  @NotNull
  public static File ipBans = new File("ip_bans.json");

  /**
   * the ops file.
   */
  @NotNull
  public static File ops = new File("ops.json");

  /**
   * the players directory.
   */
  @NotNull
  public static File players = new File("players");

  /**
   * the plugins directory.
   */
  @NotNull
  public static File plugins = new File("plugins");

  /**
   * the profile bans file.
   */
  @NotNull
  public static File profileBans = new File("profile_bans.json");

  /**
   * the server config file.
   */
  @NotNull
  public static File serverConfig = new File("shiruka.yml");

  /**
   * the server locale.
   */
  @NotNull
  public static Locale serverLocale = Locale.ENGLISH;

  /**
   * the user cache file.
   */
  @NotNull
  public static File userCache = new File("user_cache.json");

  /**
   * the whitelist file.
   */
  @NotNull
  public static File whitelist = new File("whitelist.json");

  /**
   * ctor.
   */
  private ShirukaMain() {
  }

  /**
   * initiates the file.
   *
   * @param file the file to initiate.
   * @param holder the holder to initiate.
   */
  public static void config(@NotNull final File file, @NotNull final Class<? extends ConfigHolder> holder) {
    //noinspection UnstableApiUsage
    ConfigLoader.builder()
      .setFolderPath(file.getAbsoluteFile().getParentFile())
      .setFileName(com.google.common.io.Files.getNameWithoutExtension(file.getName()))
      .setPathHolder(holder)
      .setConfigType(SystemUtils.getType(file))
      .build()
      .load(true);
  }

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) {
    ShirukaMain.START_TIME.set(System.currentTimeMillis());
    final var parsed = ShirukaConsoleParser.parse(args);
    if (parsed == null || parsed.has(ShirukaConsoleParser.HELP)) {
      ShirukaConsoleParser.printHelpOn();
      return;
    }
    if (parsed.has(ShirukaConsoleParser.VERSION)) {
      ShirukaConsoleParser.printVersion();
      return;
    }
    if (parsed.has(ShirukaConsoleParser.DEBUG) &&
      parsed.valueOf(ShirukaConsoleParser.DEBUG)) {
      final var context = (LoggerContext) LogManager.getContext(false);
      context.getConfiguration()
        .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        .setLevel(Level.DEBUG);
      context.updateLoggers();
    }
    final var here = ShirukaMain.HOME;
    if (here.contains("!") || here.contains("+")) {
      ShirukaMain.log.warn("§cCannot run server in a directory with ! or + in the pathname.");
      ShirukaMain.log.warn("§cPlease rename the affected folders and try again.");
      return;
    }
    if (System.getProperty("jdk.nio.maxCachedBufferSize") == null) {
      System.setProperty("jdk.nio.maxCachedBufferSize", "262144");
    }
    System.setProperty("library.jansi.version", "Shiru ka");
    SystemUtils.startTimerHack();
    final var global = java.util.logging.Logger.getLogger("");
    global.setUseParentHandlers(false);
    Stream.of(global.getHandlers())
      .forEach(global::removeHandler);
    global.addHandler(new ForwardLogHandler());
    final var rootLogger = LogManager.getRootLogger();
    System.setOut(IoBuilder.forLogger(rootLogger).setLevel(Level.INFO).buildPrintStream());
    System.setErr(IoBuilder.forLogger(rootLogger).setLevel(Level.WARN).buildPrintStream());
    JiraExceptionCatcher.run(() -> {
      ShirukaMain.loadFilesAndDirectories(parsed);
      ShirukaMain.serverLocale = Languages.startSequence();
      final var thread = new Thread(ShirukaMain.SERVER_RUNNABLE, "Server thread");
      thread.setUncaughtExceptionHandler((t, e) -> JiraExceptionCatcher.serverException(e));
      thread.setPriority(Thread.NORM_PRIORITY + 2);
      thread.start();
    });
  }

  /**
   * creates a new server socket from the server config values.
   *
   * @return a newly created server socket.
   */
  @NotNull
  private static RakNetServer createSocket() {
    final var ip = ServerConfig.ip;
    final var port = ServerConfig.port;
    final var gameMode = ServerConfig.gameMode;
    final var maxPlayers = ServerConfig.maxPlayers;
    final var motd = ServerConfig.motd;
    final var worldName = ServerConfig.defaultWorldName;
    final var identifier = new MinecraftIdentifier(motd, ShirukaMain.MINECRAFT_PROTOCOL_VERSION,
      ShirukaMain.MINECRAFT_VERSION, 0, maxPlayers, 0L, worldName, gameMode);
    final var socket = new RakNetServer(new InetSocketAddress(ip, port), maxPlayers, identifier);
    identifier.setServerGloballyUniqueId(socket.getGloballyUniqueId());
    return socket;
  }

  /**
   * creates and returns the server file/d.
   *
   * @param file the file to create.
   * @param directory the directory to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private static File createsServerFile(@NotNull final File file, final boolean directory) throws IOException {
    if (directory) {
      ShirukaMain.log.debug("§7Checking for {} directory.", file.getName());
      if (!Files.exists(file.toPath())) {
        ShirukaMain.log.debug("§7Directory {} not present, creating one for you.", file.getName());
        Files.createDirectory(file.toPath());
      }
      return file;
    }
    ShirukaMain.log.debug("§7Checking for {} file.", file.getName());
    if (!Files.exists(file.toPath())) {
      ShirukaMain.log.debug("§7File {} not present, creating one for you.", file.getName());
      Files.createFile(file.toPath());
    }
    return file;
  }

  /**
   * creates and returns the server file/d.
   *
   * @param options the options to create.
   * @param spec the spec to create.
   * @param directory the directory to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private static File createsServerFile(@NotNull final OptionSet options, @NotNull final OptionSpec<File> spec,
                                        final boolean directory) throws IOException {
    return ShirukaMain.createsServerFile(
      Objects.requireNonNull(options.valueOf(spec), "Something went wrong!"), directory);
  }

  /**
   * creates and returns the server file.
   *
   * @param options the options to create.
   * @param spec the spec to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private static File createsServerFile(@NotNull final OptionSet options, @NotNull final OptionSpec<File> spec)
    throws IOException {
    return ShirukaMain.createsServerFile(options, spec, false);
  }

  /**
   * loads all files and directories.
   *
   * @param options the options to load.
   */
  private static void loadFilesAndDirectories(@NotNull final OptionSet options) throws Exception {
    ShirukaMain.plugins = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.PLUGINS, true);
    ShirukaMain.players = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.PLAYERS, true);
    ShirukaMain.serverConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.CONFIG);
    ShirukaMain.ops = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.OPS);
    ShirukaMain.userCache = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.USER_CACHE);
    ShirukaMain.ipBans = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.IP_BANS);
    ShirukaMain.profileBans = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.PROFILE_BANS);
    ShirukaMain.whitelist = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.WHITE_LIST);
    ShirukaMain.config(ShirukaMain.serverConfig, ServerConfig.class);
    ShirukaMain.config(ShirukaMain.ops, OpsConfig.class);
    ShirukaMain.config(ShirukaMain.userCache, UserCacheConfig.class);
    ShirukaMain.config(ShirukaMain.ipBans, IpBanConfig.class);
    ShirukaMain.config(ShirukaMain.profileBans, ProfileBanConfig.class);
    ShirukaMain.config(ShirukaMain.whitelist, WhitelistConfig.class);
  }
}
