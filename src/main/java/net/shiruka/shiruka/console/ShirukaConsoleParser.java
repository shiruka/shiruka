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

package net.shiruka.shiruka.console;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.shiruka.shiruka.ShirukaServer;
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
   * the ip bans file option spec.
   */
  @NotNull
  public static final OptionSpec<File> IP_BANS;

  /**
   * the ops file option spec.
   */
  @NotNull
  public static final OptionSpec<File> OPS;

  /**
   * the players file option spec.
   */
  @NotNull
  public static final OptionSpec<File> PLAYERS;

  /**
   * the plugins directory option spec.
   */
  @NotNull
  public static final OptionSpec<File> PLUGINS;

  /**
   * the profile bans file option spec.
   */
  @NotNull
  public static final OptionSpec<File> PROFILE_BANS;

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
   * the whitelist file option spec.
   */
  @NotNull
  public static final OptionSpec<File> WHITE_LIST;

  /**
   * the option parser.
   */
  @NotNull
  private static final OptionParser PARSER;

  /**
   * a simple java util logger.
   */
  @NotNull
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
      .defaultsTo(new File("shiruka.yaml"))
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
    USER_CACHE = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("UC", "usercache"), "User cache file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("user_cache.json"))
      .describedAs("User cache file");
    VERSION = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("V", "version"), "Show the Shiru ka's version");
    DEBUG = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("D", "debug"), "Debug mode to use")
      .withOptionalArg()
      .ofType(Boolean.class)
      .defaultsTo(true)
      .describedAs("Debug mode");
    IP_BANS = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("IB", "ipbans"), "Ip bans file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("ip_bans.json"))
      .describedAs("Ip bans file");
    PROFILE_BANS = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("PB", "profilebans"), "Profile bans file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("profile_bans.json"))
      .describedAs("Profile bans file");
    WHITE_LIST = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("W", "whitelist"), "Whitelist file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("whitelist.json"))
      .describedAs("Whitelist file");
    PLAYERS = ShirukaConsoleParser.PARSER
      .acceptsAll(Arrays.asList("PL", "plugins"), "Players directory to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("players"))
      .describedAs("Players directory");
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
