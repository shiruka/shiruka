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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a console command parser using jopt-simple.
 */
final class ShirukaConsoleParser {

  /**
   * the help option spec.
   */
  @NotNull
  public static final OptionSpec<Void> HELP;

  /**
   * the server config file option spec.
   */
  @NotNull
  public static final OptionSpec<File> CONFIG;

  /**
   * the plugins directory option spec.
   */
  @NotNull
  public static final OptionSpec<File> PLUGINS;

  /**
   * the ops file option spec.
   */
  @NotNull
  public static final OptionSpec<File> OPS;

  /**
   * the version option spec.
   */
  @NotNull
  public static final OptionSpec<Void> VERSION;

  /**
   * the debug mode option spec.
   */
  @NotNull
  public static final OptionSpec<Boolean> DEBUG;

  /**
   * the option parser.
   */
  private static final OptionParser PARSER;

  /**
   * a simple java util logger.
   */
  private static final java.util.logging.Logger SIMPLE_LOGGER = java.util.logging.Logger.getLogger("Shiru ka");

  static {
    PARSER = new OptionParser();
    HELP = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("?", "help"), "Show the help")
      .forHelp();
    CONFIG = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("c", "config"), "Server configuration file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("server.yml"))
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
      .defaultsTo(new File("ops.json"))
      .describedAs("Ops file");
    VERSION = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("v", "version"), "Show the Shiru ka's version");
    DEBUG = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("D", "debug"), "Debug mode to use")
      .withOptionalArg()
      .ofType(Boolean.class)
      .defaultsTo(false)
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
  static OptionSet parse(@NotNull final String[] args) {
    try {
      return ShirukaConsoleParser.PARSER.parse(args);
    } catch (final OptionException e) {
      ShirukaConsoleParser.SIMPLE_LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
    }
    return null;
  }

  /**
   * prints the server's version.
   */
  static void printVersion() {
    System.out.println(ShirukaServer.VERSION);
  }

  /**
   * prints the help message.
   */
  static void printHelpOn() {
    try {
      ShirukaConsoleParser.PARSER.printHelpOn(System.out);
    } catch (final IOException ex) {
      ShirukaConsoleParser.SIMPLE_LOGGER.log(Level.SEVERE, null, ex);
    }
  }
}
