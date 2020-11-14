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

import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.misc.Loggers;
import java.io.File;
import java.util.Objects;
import joptsimple.OptionSet;
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
      System.out.println("Please rename the affected folders and try again.");
      return;
    }
    System.setProperty("library.jansi.version", "Shiruka");
    JiraExceptionCatcher.run(() ->
      new ShirukaMain(parsed)
        .exec());
  }

  /**
   * initiates the Shiru ka server.
   */
  private void exec() {
    Loggers.init(ShirukaMain.LOGGER);
    ShirukaMain.LOGGER.info("Shiru ka is starting...");
    final File serverConfig;
    if (this.options.has(ShirukaConsoleParser.getConfig())) {
      serverConfig = Objects.requireNonNull(this.options.valueOf(ShirukaConsoleParser.getConfig()),
        "The parsed option set has not server config path value!");
    } else {
      serverConfig = new File("shiruka.yml");
    }
    new ServerConfig(serverConfig);
  }
}
