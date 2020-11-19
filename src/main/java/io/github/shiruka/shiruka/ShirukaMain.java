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

import io.github.shiruka.shiruka.config.OpsConfig;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.misc.Loggers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the global logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Shiru ka");

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
    if (parsed == null || parsed.has(ShirukaConsoleParser.getHelp())) {
      ShirukaConsoleParser.printHelpOn();
      return;
    }
    if (parsed.has(ShirukaConsoleParser.getVersion())) {
      ShirukaConsoleParser.printVersion();
      return;
    }
    final var here = new File(".").getAbsolutePath();
    if (here.contains("!") || here.contains("+")) {
      System.err.println("Cannot run server in a directory with ! or + in the pathname.");
      System.err.println("Please rename the affected folders and try again.");
      return;
    }
    System.setProperty("library.jansi.version", "Shiruka");
    JiraExceptionCatcher.run(() ->
      new ShirukaMain(parsed)
        .exec());
  }

  /**
   * creates directory and do some logs.
   *
   * @param dir the directory to create.
   *
   * @throws IOException if something went wrong when creating the directory.
   */
  private static void createDirectory(@NotNull final File dir) throws IOException {
    if (!Files.exists(dir.toPath())) {
      ShirukaMain.LOGGER.warn("Directory {} not present", dir.getName());
      ShirukaMain.LOGGER.info("Creating one for you... ");
      Files.createDirectory(dir.toPath());
    }
  }

  /**
   * creates files and do some logs.
   *
   * @param file the file to create.
   *
   * @throws IOException if something went wrong when creating the file.
   */
  private static void createFile(@NotNull final File file) throws IOException {
    if (!Files.exists(file.toPath())) {
      ShirukaMain.LOGGER.warn("File {} not present", file.getName());
      ShirukaMain.LOGGER.info("Creating one for you... ");
      Files.createFile(file.toPath());
    }
  }

  /**
   * initiates the Shiru ka server.
   *
   * @throws IOException if something went wrong when creating files.
   */
  private void exec() throws IOException {
    Loggers.init(ShirukaMain.LOGGER);
    ShirukaMain.LOGGER.info("Shiru ka is starting...");
    ServerConfig.init(this.createsServerFile(ShirukaConsoleParser.getConfig(),
      "The parsed option set has not server config path value!", "shiruka.yml"));
    this.createsServerFile(ShirukaConsoleParser.getPlugins(),
      "The parsed options et has not plugins directory value!", "plugins", true);
    OpsConfig.init(this.createsServerFile(ShirukaConsoleParser.getOps(),
      "The parsed options et has not ops file value!", "ops.json"));
  }

  /**
   * creates and returns the server file.
   *
   * @param spec the spec to create.
   * @param error the error to print.
   * @param defaultName the default name of the file.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private File createsServerFile(@NotNull final OptionSpec<File> spec, @NotNull final String error,
                                 @NotNull final String defaultName) throws IOException {
    return this.createsServerFile(spec, error, defaultName, false);
  }

  /**
   * creates and returns the server file/d.
   *
   * @param spec the spec to create.
   * @param error the error to print.
   * @param defaultName the default name of the file.
   * @param directory the directory to create.
   *
   * @return a server file instance.
   *
   * @throws IOException if something went wrong when the creating the file.
   */
  @NotNull
  private File createsServerFile(@NotNull final OptionSpec<File> spec, @NotNull final String error,
                                 @NotNull final String defaultName, final boolean directory) throws IOException {
    final File file;
    if (this.options.has(spec)) {
      file = Objects.requireNonNull(this.options.valueOf(spec), error);
    } else {
      file = new File(defaultName);
    }
    ShirukaMain.LOGGER.info("Checking for server files: {}", file.getName());
    if (directory) {
      ShirukaMain.createDirectory(file);
    } else {
      ShirukaMain.createFile(file);
    }
    return file;
  }
}
