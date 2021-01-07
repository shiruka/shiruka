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

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.config.OpsConfig;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.config.UserCacheConfig;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.ShirukaConsoleParser;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.network.server.NetServerSocket;
import io.github.shiruka.shiruka.world.WorldType;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Objects;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("ShirukaMain");

  /**
   * the parsed arguments.
   */
  @NotNull
  private final OptionSet options;

  /**
   * ctor.
   *
   * @param options the options.
   */
  private ShirukaMain(@NotNull final OptionSet options) {
    this.options = options;
  }

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) {
    if (System.getProperty("jdk.nio.maxCachedBufferSize") == null) {
      System.setProperty("jdk.nio.maxCachedBufferSize", "262144");
    }
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
    System.setProperty("library.jansi.version", "Shiru ka");
    final var main = new ShirukaMain(parsed);
    JiraExceptionCatcher.run(main::exec);
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
   * @param spec the spec to create.
   * @param directory the directory to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private File createsServerFile(@NotNull final OptionSpec<File> spec, final boolean directory) throws IOException {
    return ShirukaMain.createsServerFile(
      Objects.requireNonNull(this.options.valueOf(spec), "Something went wrong!"), directory);
  }

  /**
   * creates and returns the server file.
   *
   * @param spec the spec to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private File createsServerFile(@NotNull final OptionSpec<File> spec)
    throws IOException {
    return this.createsServerFile(spec, false);
  }

  /**
   * initiates the Shiru ka server.
   *
   * @throws IOException if something went wrong when creating files.
   */
  private void exec() throws IOException {
    ShirukaMain.LOGGER.info("§eShiru ka is starting.");
    final var start = System.currentTimeMillis();
    ServerConfig.init(this.createsServerFile(ShirukaConsoleParser.CONFIG));
    this.createsServerFile(ShirukaConsoleParser.PLUGINS, true);
    OpsConfig.init(this.createsServerFile(ShirukaConsoleParser.OPS));
    UserCacheConfig.init(this.createsServerFile(ShirukaConsoleParser.USER_CACHE));
    ServerThreadPool.init();
    final var ip = ServerConfig.ADDRESS_IP.getValue().orElseThrow();
    final var port = ServerConfig.ADDRESS_PORT.getValue().orElseThrow();
    final var maxPlayer = ServerConfig.MAX_PLAYERS.getValue().orElseThrow();
    final var description = ServerConfig.DESCRIPTION.getValue().orElseThrow();
    final var worldType = ServerConfig.WORLD_TYPE.getValue().orElseThrow();
    final var server = new ShirukaServer(description, WorldType.fromType(worldType).create(),
      listener -> NetServerSocket.init(new InetSocketAddress(ip, port), listener, maxPlayer),
      ShirukaConsole::new);
    Shiruka.setServer(server);
    server.startServer(start);
  }
}
