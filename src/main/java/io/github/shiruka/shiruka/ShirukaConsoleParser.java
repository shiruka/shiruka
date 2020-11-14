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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a console command parser using jopt-simple.
 */
final class ShirukaConsoleParser {

  /**
   * a simple java util logger.
   */
  static final java.util.logging.Logger SIMPLE_LOGGER = java.util.logging.Logger.getLogger("Shiru ka");

  /**
   * the option parser.
   */
  private static final OptionParser PARSER = new OptionParser();

  /**
   * ctor.
   */
  private ShirukaConsoleParser() {
  }

  /**
   * parses the options from the given arguments.
   *
   * @param args the args to parse.
   *
   * @return the parsed options.
   */
  @Nullable
  static OptionSet parseOptions(@NotNull final String[] args) {
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("?", "help"), "Show the help");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("c", "config"), "Properties file to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("server.properties"))
      .describedAs("Properties file");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("plugins"))
      .describedAs("Plugin directory");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("h", "host", "server-ip"), "Host to listen on")
      .withRequiredArg()
      .ofType(String.class)
      .describedAs("Hostname or IP");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("W", "world-dir", "universe", "world-container"), "World container")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("."))
      .describedAs("Directory containing worlds");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("w", "world", "level-name"), "World name")
      .withRequiredArg()
      .ofType(String.class)
      .describedAs("World name");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("p", "port", "server-port"), "Port to listen on")
      .withRequiredArg()
      .ofType(Integer.class)
      .describedAs("Port");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("o", "online-mode"), "Whether to use online authentication")
      .withRequiredArg()
      .ofType(Boolean.class)
      .describedAs("Authentication");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("s", "size", "max-players"), "Maximum amount of players")
      .withRequiredArg()
      .ofType(Integer.class)
      .describedAs("Server size");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
      .withRequiredArg()
      .ofType(SimpleDateFormat.class)
      .describedAs("Log date format");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("log-pattern"), "Specfies the log filename pattern")
      .withRequiredArg()
      .ofType(String.class)
      .defaultsTo("server.log")
      .describedAs("Log filename");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
      .withRequiredArg()
      .ofType(Integer.class)
      .defaultsTo(0)
      .describedAs("Max log size");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("log-count"), "Specified how many log files to cycle through")
      .withRequiredArg()
      .ofType(Integer.class)
      .defaultsTo(1)
      .describedAs("Log count");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("log-append"), "Whether to append to the log file")
      .withRequiredArg()
      .ofType(Boolean.class)
      .defaultsTo(true)
      .describedAs("Log append");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("log-strip-color"), "Strips color codes from log file");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("b", "bukkit-settings"), "File for bukkit settings")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("bukkit.yml"))
      .describedAs("Yml file");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("C", "commands-settings"), "File for command settings")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("commands.yml"))
      .describedAs("Yml file");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("forceUpgrade"), "Whether to force a world upgrade");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("eraseCache"), "Whether to force cache erase during world upgrade");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("nogui"), "Disables the graphical console");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("nojline"), "Disables jline and emulates the vanilla console");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("noconsole"), "Disables the console");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("v", "version"), "Show the CraftBukkit Version");
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("demo"), "Demo mode");
    // Spigot Start
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("S", "spigot-settings"), "File for spigot settings")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("spigot.yml"))
      .describedAs("Yml file");
    // Spigot End
    // Paper Start
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("paper", "paper-settings"), "File for paper settings")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("paper.yml"))
      .describedAs("Yml file");
    // Paper end
    // Paper start
    ShirukaConsoleParser.PARSER.acceptsAll(Collections.singletonList("server-name"), "Name of the server")
      .withRequiredArg()
      .ofType(String.class)
      .defaultsTo("Unknown Server")
      .describedAs("Name");
    // Paper end
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
  static void printHelpOn() {
    try {
      ShirukaConsoleParser.PARSER.printHelpOn(System.out);
    } catch (final IOException ex) {
      ShirukaConsoleParser.SIMPLE_LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  /**
   * prints the server's version.
   */
  static void printVersion() {
    System.out.println(ShirukaServer.VERSION);
  }
}
