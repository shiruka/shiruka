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
package io.github.shiruka.shiruka.log;

import io.github.shiruka.shiruka.log.logger.LoggerHandlers;
import io.github.shiruka.shiruka.log.pipeline.*;
import java.io.IOException;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * a logger interface.
 */
public interface PipelineLogger {

  /**
   * an empty logger.
   */
  PipelineLogger EMPTY = new Empty();

  /**
   * initialization code.
   *
   * @param verbose whether to enable verbose.
   *
   * @return a new logger.
   *
   * @throws IOException pass down exception.
   * @throws IllegalStateException when the file has not match.
   */
  @NotNull
  static PipelineLogger init(final boolean verbose) throws IOException {
    final ColorizedLogger colorized = new ColorizedLogger(new DefaultLogger());
    return FileLogger.init(new LoggerHandlers(verbose
      ? new DebugLogger(colorized)
      : new NoDebugLogger(colorized)));
  }

  /**
   * logs a message.
   *
   * @param msg the message.
   */
  void debug(@NotNull LogMessage msg);

  /**
   * logs a message.
   *
   * @param msg the message.
   */
  void error(@NotNull LogMessage msg);

  /**
   * handles normal messages.
   *
   * @param msg the message.
   *
   * @return the same message.
   */
  @NotNull
  LogMessage handle(@NotNull LogMessage msg);

  /**
   * logs a message.
   *
   * @param msg the message.
   */
  void log(@NotNull LogMessage msg);

  /**
   * obtains the next logger in the pipeline.
   *
   * @return the next logger.
   */
  @NotNull
  PipelineLogger next();

  /**
   * obtains the underlying output.
   *
   * @return the output.
   */
  @NotNull
  OutputStream out();

  /**
   * logs a message.
   *
   * @param msg the message.
   */
  void success(@NotNull LogMessage msg);

  /**
   * logs a message.
   *
   * @param msg the message.
   */
  void warn(@NotNull LogMessage msg);

  /**
   * a class which mocks the {@link PipelineLogger} class.
   */
  final class Empty implements PipelineLogger {

    @Override
    public void debug(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void error(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public LogMessage handle(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void log(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PipelineLogger next() {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public OutputStream out() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void success(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void warn(@NotNull final LogMessage msg) {
      throw new UnsupportedOperationException();
    }
  }
}
