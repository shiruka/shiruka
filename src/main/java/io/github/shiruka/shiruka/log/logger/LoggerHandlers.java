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
package io.github.shiruka.shiruka.log.logger;

import io.github.shiruka.shiruka.log.LogHandler;
import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.PipelineLogger;
import io.github.shiruka.shiruka.log.pipeline.PipelineLoggerBase;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * a class which contains the handlers that plugins may use to
 * change the output of the logger or their loggers.
 */
public class LoggerHandlers extends PipelineLoggerBase {

  /**
   * the set of handlers that intercept all output.
   */
  private final Set<LogHandler> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

  /**
   * ctor.
   *
   * @param next the next logger in the pipeline
   */
  public LoggerHandlers(@NotNull final PipelineLogger next) {
    super(next);
  }

  /**
   * adds the given handler to {@link LoggerHandlers#handlers}.
   *
   * @param handler the handler to add.
   */
  public final void addHandler(@NotNull final LogHandler handler) {
    this.handlers.add(handler);
  }

  /**
   * obtains the all the handlers that are attached to
   * the output.
   *
   * @return the logger handlers
   */
  @NotNull
  public final Set<LogHandler> getHandlers() {
    return Collections.unmodifiableSet(this.handlers);
  }

  @NotNull
  @Override
  public final LogMessage handle(@NotNull final LogMessage msg) {
    if (this.handlers.stream()
      .allMatch(handler -> handler.handle(msg))) {
      return msg;
    }
    throw new IllegalStateException("Something went wrong when handling loggers.");
  }

  /**
   * remove the given handler from {@link LoggerHandlers#handlers}.
   *
   * @param handler the handler to remove.
   */
  public final void removeHandler(@NotNull final LogHandler handler) {
    this.handlers.remove(handler);
  }
}
