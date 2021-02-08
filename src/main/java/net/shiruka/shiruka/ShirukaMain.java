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

import com.whirvis.jraknet.identifier.MinecraftIdentifier;
import com.whirvis.jraknet.server.RakNetServer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.shiruka.api.Shiruka;
import net.shiruka.api.util.ThrowableRunnable;
import net.shiruka.shiruka.config.*;
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.io.IoBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the protocol version of the Minecraft game.
   */
  public static final short MINECRAFT_PROTOCOL_VERSION = 422;

  /**
   * the version of the Minecraft game.
   */
  public static final String MINECRAFT_VERSION = "1.16.201";

  /**
   * the working directory as a string
   */
  private static final String HOME = System.getProperty("user.dir");

  /**
   * the Path directory to the working dir
   */
  public static final Path HOME_PATH = Paths.get(ShirukaMain.HOME);

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("ShirukaMain");

  /**
   * the server locale.
   */
  @NotNull
  private static Locale SERVER_LOCALE = Locale.ENGLISH;

  /**
   * the server runnable.
   */
  private static final ThrowableRunnable SERVER_RUNNABLE = () -> {
    final var startTime = System.currentTimeMillis();
    final var socket = ShirukaMain.createSocket();
    final var server = new ShirukaServer(startTime, ShirukaConsole::new, ShirukaMain.SERVER_LOCALE, socket);
    AsyncCatcher.server = server;
    Runtime.getRuntime().addShutdownHook(new ShirukaShutdownThread(server));
    Shiruka.setServer(server);
    socket.addListener(server);
    socket.start();
  };

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) {
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
    final var here = new File(".").getAbsolutePath();
    if (here.contains("!") || here.contains("+")) {
      ShirukaMain.LOGGER.warn("§cCannot run server in a directory with ! or + in the pathname.");
      ShirukaMain.LOGGER.warn("§cPlease rename the affected folders and try again.");
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
      ShirukaMain.payloadClasses();
      ShirukaMain.loadFilesAndDirectories(parsed);
      ShirukaMain.SERVER_LOCALE = Languages.startSequence();
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
    final var ip = ServerConfig.ADDRESS_IP.getValue()
      .orElseThrow(() -> new IllegalStateException("\"ip\" not found in the server config!"));
    final var port = ServerConfig.PORT.getValue()
      .orElseThrow(() -> new IllegalStateException("\"port\" not found in the server config!"));
    final var maxPlayer = ServerConfig.DESCRIPTION_MAX_PLAYERS.getValue()
      .orElseThrow(() -> new IllegalStateException("\"max-players\" not found in the server config!"));
    final var socket = new RakNetServer(new InetSocketAddress(ip, port), maxPlayer);
    final var gameMode = ServerConfig.DESCRIPTION_GAME_MODE.getValue().orElse("Survival");
    final var maxPlayers = ServerConfig.DESCRIPTION_MAX_PLAYERS.getValue().orElse(10);
    final var motd = ServerConfig.DESCRIPTION_MOTD.getValue().orElse("");
    final var worldName = ServerConfig.DEFAULT_WORLD_NAME.getValue().orElse("world");
    final var identifier = new MinecraftIdentifier(motd, ShirukaMain.MINECRAFT_PROTOCOL_VERSION,
      ShirukaMain.MINECRAFT_VERSION, 0, maxPlayers, socket.getGloballyUniqueId(), worldName, gameMode);
    socket.setIdentifier(identifier);
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
      ShirukaMain.LOGGER.debug("§7Checking for {} directory.", file.getName());
      if (!Files.exists(file.toPath())) {
        ShirukaMain.LOGGER.debug("§7Directory {} not present, creating one for you.", file.getName());
        Files.createDirectory(file.toPath());
      }
    } else {
      ShirukaMain.LOGGER.debug("§7Checking for {} file.", file.getName());
      if (!Files.exists(file.toPath())) {
        ShirukaMain.LOGGER.debug("§7File {} not present, creating one for you.", file.getName());
        Files.createFile(file.toPath());
      }
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
   */
  private static void loadFilesAndDirectories(@NotNull final OptionSet options) throws Exception {
    ShirukaMain.createsServerFile(options, ShirukaConsoleParser.PLUGINS, true);
    final var serverConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.CONFIG);
    final var opsConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.OPS);
    final var userCacheConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.USER_CACHE);
    final var ipBanConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.IP_BANS);
    final var profileBanConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.PROFILE_BANS);
    final var whitelistConfig = ShirukaMain.createsServerFile(options, ShirukaConsoleParser.WHITE_LIST);
    ServerConfig.init(serverConfig);
    OpsConfig.init(opsConfig);
    UserCacheConfig.init(userCacheConfig);
    IpBanConfig.init(ipBanConfig);
    ProfileBanConfig.init(profileBanConfig);
    WhitelistConfig.init(whitelistConfig);
  }

  /**
   * payloads the some of classes.
   */
  private static void payloadClasses() throws Exception {
    Class.forName("net.shiruka.shiruka.network.PacketRegistry");
  }
}
