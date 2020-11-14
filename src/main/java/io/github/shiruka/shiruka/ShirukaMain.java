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

import com.beust.jcommander.JCommander;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.shiruka.console.ShirukaConsole;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import io.github.shiruka.shiruka.misc.Loggers;
import io.github.shiruka.shiruka.network.impl.ShirukaSocketListener;
import io.github.shiruka.shiruka.network.server.NetServerSocket;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import java.util.Locale;
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
   * the database of fragments.
   */
  private static final String FRAGMENTS_DATABASE =
    "https://raw.githubusercontent.com/shiruka/fragments/master/fragments.json";

  /**
   * Shiru ka's program arguments.
   */
  private static final ShirukaCommander COMMANDER = new ShirukaCommander();

  /**
   * the program arguments.
   */
  @NotNull
  private final String[] args;

  /**
   * ctor.
   *
   * @param args the program arguments.
   */
  private ShirukaMain(@NotNull final String[] args) {
    this.args = args.clone();
  }

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) {
    Locale.setDefault(Locale.ENGLISH);
    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    System.setProperty("log4j.skipJansi", "false");
    Loggers.init(ShirukaMain.LOGGER);
    final var commander = JCommander.newBuilder()
      .programName("Shiru ka")
      .args(args)
      .addObject(ShirukaMain.COMMANDER)
      .build();
    if (ShirukaMain.COMMANDER.help) {
      commander.usage();
      return;
    }
    JiraExceptionCatcher.run(() ->
      new ShirukaMain(args).exec());
  }

  /**
   * initiates the Shiru ka server.
   */
  private void exec() {
    ShirukaMain.LOGGER.info("Shiru ka is starting...");
    final var server = new ShirukaServer();
    NetServerSocket.init(new ShirukaSocketListener(server));
    ShirukaMain.LOGGER.info("Server configuration file is preparing...");
    // TODO initiate server configuration file.
    Shiruka.initServer(server);
    final var console = new ShirukaConsole(server);
    console.start();
    LogManager.shutdown();
  }
}
