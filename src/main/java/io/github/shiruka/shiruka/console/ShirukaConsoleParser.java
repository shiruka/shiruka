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

package io.github.shiruka.shiruka.console;

import io.github.shiruka.shiruka.ShirukaServer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a console command parser using jopt-simple.
 */
public final class ShirukaConsoleParser {

  /**
   * the server config file option spec.
   */
  @NotNull
  public static final OptionSpec<File> CONFIG;

  /**
   * the debug mode option spec.
   */
  @NotNull
  public static final OptionSpec<Boolean> DEBUG;

  /**
   * the help option spec.
   */
  @NotNull
  public static final OptionSpec<Void> HELP;

  /**
   * the ops file option spec.
   */
  @NotNull
  public static final OptionSpec<File> OPS;

  /**
   * the plugins directory option spec.
   */
  @NotNull
  public static final OptionSpec<File> PLUGINS;

  /**
   * the ops file option spec.
   */
  @NotNull
  public static final OptionSpec<File> USER_CACHE;

  /**
   * the version option spec.
   */
  @NotNull
  public static final OptionSpec<Void> VERSION;

  /**
   * the option parser.
   */
  private static final OptionParser PARSER;

  /**
   * a simple java util logger.
   */
  private static final Logger SIMPLE_LOGGER;

  static {
    SIMPLE_LOGGER = Logger.getLogger("Shiru ka");
    PARSER = new OptionParser();
    HELP = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("?", "help"), "Show the help")
      .forHelp();
    CONFIG = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("C", "config"), "Server configuration file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("shiruka.yml"))
      .describedAs("Server configuration file");
    PLUGINS = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("plugins"))
      .describedAs("Plugin directory");
    OPS = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("O", "ops"), "Ops file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("ops.hjson"))
      .describedAs("Ops file");
    USER_CACHE = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("U", "usercache"), "User cache file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("usercache.hjson"))
      .describedAs("User cache file");
    VERSION = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("v", "version"), "Show the Shiru ka's version");
    DEBUG = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("D", "debug"), "Debug mode to use")
      .withOptionalArg()
      .ofType(Boolean.class)
      .defaultsTo(true)
      .describedAs("Debug mode");
  }

  /**
   * ctor.
   */
  private ShirukaConsoleParser() {
  }

  /**
   * parses the given arguments into a {@link OptionSet} instance.
   *
   * @param args the args to parse.
   *
   * @return a parsed option set instance.
   */
  @Nullable
  public static OptionSet parse(@NotNull final String[] args) {
    try {
      return ShirukaConsoleParser.PARSER.parse(args);
    } catch (final OptionException e) {
      ShirukaConsoleParser.SIMPLE_LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
    }
    return null;
  }

  /**
   * prints the help message.
   */
  public static void printHelpOn() {
    try {
      ShirukaConsoleParser.PARSER.printHelpOn(System.out);
    } catch (final IOException ex) {
      ShirukaConsoleParser.SIMPLE_LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  /**
   * prints the server's version.
   */
  public static void printVersion() {
    System.out.println(ShirukaServer.VERSION);
  }
}
