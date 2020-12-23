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
package io.github.shiruka.shiruka.log.pipeline;

import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.LogPrintStream;
import io.github.shiruka.shiruka.log.ShirukaLoggers;
import java.io.OutputStream;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;
import org.jetbrains.annotations.NotNull;

/**
 * a default logger which is the final logger in the pipeline.
 */
public final class DefaultLogger extends PipelineLoggerBase {

  /**
   * a simple implementation to cover {@link System#err}.
   */
  private static final LogPrintStream ERR = new LogPrintStream(Loggers::error);

  /**
   * a simple implementation to cover {@link System#out}.
   */
  private static final LogPrintStream OUT = new LogPrintStream(Loggers::log);

  /**
   * the underlying stream to which output is passed
   */
  @NotNull
  private final AnsiPrintStream stream;

  /**
   * ctor.
   */
  public DefaultLogger() {
    super();
    this.stream = AnsiConsole.out();
    System.setOut(DefaultLogger.OUT);
    System.setErr(DefaultLogger.ERR);
  }

  @Override
  public void debug(@NotNull final LogMessage msg) {
    this.print(msg.format(1));
  }

  @Override
  public void error(@NotNull final LogMessage msg) {
    this.print(msg.format(1));
  }

  @Override
  public void log(@NotNull final LogMessage msg) {
    this.print(msg.format(1));
  }

  @NotNull
  @Override
  public OutputStream out() {
    return this.stream;
  }

  @Override
  public void success(@NotNull final LogMessage msg) {
    this.print(msg.format(1));
  }

  @Override
  public void warn(@NotNull final LogMessage msg) {
    this.print(msg.format(1));
  }

  @NotNull
  @Override
  public LogMessage handle(@NotNull final LogMessage msg) {
    throw new UnsupportedOperationException();
  }

  /**
   * prints the given message.
   *
   * @param message the message to print.
   */
  private void print(@NotNull final String message) {
    final var console = ShirukaLoggers.getConsole();
    if (console.isPresent()) {
      console.get().getReader().printAbove(message);
    } else {
      this.stream.println(message);
    }
  }
}
