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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.TerminalBuilder;

/**
 * a class that helps developers to run commands with suggestion support in the Shiru ka's console.
 */
public final class ShirukaConsole {

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
  public void start() {
    AnsiConsole.systemInstall();
    final Parser parser = new DefaultParser();
    try (final var terminal = TerminalBuilder.builder()
      .name("Shiru ka")
      .encoding(StandardCharsets.UTF_8)
      .build()) {
      final var reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(new ConsoleCommandCompleter(this.server))
        .parser(parser)
        .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
        .build();
      String line;
      while (!this.server.isInShutdownState()) {
        line = reader.readLine(">");
        this.server.runCommand(line);
      }
    } catch (final UserInterruptException e) {
//      JiraExceptionCatcher.serverException(e);
    } catch (final IOException e) {
//      JiraExceptionCatcher.serverException(e);
    } finally {
      AnsiConsole.systemUninstall();
    }
  }
}
