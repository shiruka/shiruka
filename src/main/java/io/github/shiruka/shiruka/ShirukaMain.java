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
import io.github.shiruka.shiruka.config.OpsConfig;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.config.UserCacheConfig;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.console.ShirukaConsoleParser;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.misc.Loggers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the global logger.
   */
  private static final Logger LOGGER = (Logger) LogManager.getLogger("Shiru ka");

  /**
   * the parsed options.
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
    if (!parsed.has(ShirukaConsoleParser.DEBUG) ||
      !parsed.valueOf(ShirukaConsoleParser.DEBUG)) {
      ShirukaMain.LOGGER.setLevel(Level.INFO);
    }
    final var here = new File(".").getAbsolutePath();
    if (here.contains("!") || here.contains("+")) {
      ShirukaMain.LOGGER.warn("Cannot run server in a directory with ! or + in the pathname.");
      ShirukaMain.LOGGER.warn("Please rename the affected folders and try again.");
      return;
    }
    System.setProperty("library.jansi.version", "Shiruka");
    JiraExceptionCatcher.run(() ->
      new ShirukaMain(parsed)
        .exec());
  }

  /**
   * initiates the Shiru ka server.
   *
   * @throws IOException if something went wrong when creating files.
   */
  private void exec() throws IOException {
    Loggers.init(ShirukaMain.LOGGER);
    ShirukaMain.LOGGER.info("Shiru ka is starting.");
    ServerConfig.init(this.createsServerFile(ShirukaConsoleParser.CONFIG));
    this.createsServerFile(ShirukaConsoleParser.PLUGINS, true);
    OpsConfig.init(this.createsServerFile(ShirukaConsoleParser.OPS));
    UserCacheConfig.init(this.createsServerFile(ShirukaConsoleParser.USER_CACHE));
    final var server = new ShirukaServer();
    Shiruka.initServer(server);
    final var console = new ShirukaConsole(server);
    console.start();
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
    final var file = Objects.requireNonNull(this.options.valueOf(spec), "Something went wrong!");
    if (directory) {
      ShirukaMain.LOGGER.debug("Checking for {} directory.", file.getName());
      if (!Files.exists(file.toPath())) {
        ShirukaMain.LOGGER.debug("Directory {} not present, creating one for you.", file.getName());
        Files.createDirectory(file.toPath());
      }
    } else {
      ShirukaMain.LOGGER.debug("Checking for {} file.", file.getName());
      if (!Files.exists(file.toPath())) {
        ShirukaMain.LOGGER.debug("File {} not present, creating one for you.", file.getName());
        Files.createFile(file.toPath());
      }
    }
    return file;
  }
}
