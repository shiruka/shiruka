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

import io.github.shiruka.api.Server;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.shiruka.log.AaTerminalConsole;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;

/**
 * a class that helps developers to run commands with suggestion support in the Shiru ka's console.
 */
public final class ShirukaConsole extends Thread {

  /**
   * the parser.
   */
  private static final DefaultParser PARSER = new DefaultParser();

  /**
   * the prompt.
   */
  private static final String PROMPT = ">";

  /**
   * the console command completer;
   */
  @NotNull
  private final Completer completer;

  /**
   * the server instance.
   */
  @NotNull
  private final Server server;

  /**
   * ctor.
   *
   * @param server the server.
   * @param completer the completer.
   */
  public ShirukaConsole(@NotNull final Completer completer, @NotNull final Server server) {
    super("Console");
    this.completer = completer;
    this.server = server;
  }

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaConsole(@NotNull final Server server) {
    this(new ConsoleCommandCompleter(server), server);
  }

  /**
   * starts the reading inputs.
   */
  @Override
  public void run() {
    try {
      final var reader = LineReaderBuilder.builder()
        .appName("Shiru ka")
        .terminal(AaTerminalConsole.getTerminal())
        .completer(this.completer)
        .parser(ShirukaConsole.PARSER)
        .variable(LineReader.LIST_MAX, 50)
        .variable(LineReader.HISTORY_FILE, Paths.get(".console_history"))
        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
        .option(LineReader.Option.INSERT_TAB, false)
        .option(LineReader.Option.BRACKETED_PASTE, false)
        .build();
      String line;
      while (!this.server.isInShutdownState()) {
        try {
          line = reader.readLine(ShirukaConsole.PROMPT);
        } catch (final EndOfFileException ignored) {
          continue;
        }
        if (line == null) {
          break;
        }
        Shiruka.getCommandManager().execute(line);
      }
    } catch (final UserInterruptException e) {
      this.server.stopServer();
    } catch (final Exception e) {
      JiraExceptionCatcher.serverException(e);
    }
  }
}
