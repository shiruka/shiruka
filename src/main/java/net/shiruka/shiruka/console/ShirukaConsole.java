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

import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;

/**
 * a class that helps developers to run commands with suggestion support in the Shiru ka's console.
 */
@RequiredArgsConstructor
public final class ShirukaConsole extends SimpleTerminalConsole {

  /**
   * the parser.
   */
  private static final DefaultParser PARSER = new DefaultParser();

  /**
   * the console command completer.
   */
  @NotNull
  private final Completer completer;

  /**
   * the server instance.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaConsole(@NotNull final ShirukaServer server) {
    this(new ConsoleCommandCompleter(server), server);
  }

  @Override
  protected boolean isRunning() {
    return !this.server.isStopped() && this.server.isRunning();
  }

  @Override
  protected void runCommand(@NotNull final String command) {
    this.server.getTick().getCommandQueue().enqueue(command);
  }

  @Override
  protected void shutdown() {
    this.server.safeShutdown(false);
  }

  @Override
  protected LineReader buildReader(final LineReaderBuilder builder) {
    return super.buildReader(builder
      .appName("Shiru ka")
      .completer(this.completer)
      .parser(ShirukaConsole.PARSER)
      .variable(LineReader.HISTORY_FILE, Paths.get(".console_history")));
  }
}
