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

import java.io.IOException;
import java.util.logging.Level;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * a simple java util logger.
   */
  static final java.util.logging.Logger SIMPLE_LOGGER = java.util.logging.Logger.getLogger("Shiru ka");

  /**
   * the global logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Shiru ka");

  /**
   * the database of fragments.
   */
  private static final String FRAGMENTS_DATABASE =
    "https://raw.githubusercontent.com/shiruka/fragments/master/fragments.json";

  @NotNull
  private final OptionSet options;

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
    final var options = ShirukaConsoleParser.parseOptions(args);
    if (options == null || options.has("?")) {
      try {
        ShirukaConsoleParser.getPARSER().printHelpOn(System.out);
      } catch (final IOException ex) {
        ShirukaMain.SIMPLE_LOGGER.log(Level.SEVERE, null, ex);
      }
      return;
    }
    if (options.has("v")) {
      System.out.println();
      return;
    }
    new ShirukaMain(options)
      .exec();
//    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
//    System.setProperty("log4j.skipJansi", "false");
//    Loggers.init(ShirukaMain.LOGGER);
//    JiraExceptionCatcher.run(() ->
//      new ShirukaMain(args).exec());
  }

  /**
   * initiates the Shiru ka server.
   */
  private void exec() {
//    ShirukaMain.LOGGER.info("Shiru ka is starting...");
//    final var server = new ShirukaServer();
//    NetServerSocket.init(new ShirukaSocketListener(server));
//    ShirukaMain.LOGGER.info("Server configuration file is preparing...");
//    // TODO initiate server configuration file.
//    Shiruka.initServer(server);
//    final var console = new ShirukaConsole(server);
//    console.start();
//    LogManager.shutdown();
  }
}
