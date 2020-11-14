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
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
      .withRequiredArg()
      .ofType(File.class)
      .defaultsTo(new File("plugins"))
      .describedAs("Plugin directory");
    ShirukaConsoleParser.PARSER.acceptsAll(Arrays.asList("v", "version"), "Show the Shiru ka's version");
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
