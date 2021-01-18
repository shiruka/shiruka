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

package io.github.shiruka.shiruka.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents forwarding log handlers.
 */
public final class ForwardLogHandler extends ConsoleHandler {

  /**
   * the cached loggers.
   */
  private final Map<String, Logger> cachedLoggers = new ConcurrentHashMap<>();

  @Override
  public void flush() {
  }

  @Override
  public void publish(@NotNull final LogRecord record) {
    final var logger = this.getLogger(String.valueOf(record.getLoggerName()));
    final var exception = record.getThrown();
    final var level = record.getLevel();
    final var message = this.getFormatter().formatMessage(record);
    if (level == Level.SEVERE) {
      logger.error(message, exception);
    } else if (level == Level.WARNING) {
      logger.warn(message, exception);
    } else if (level == Level.INFO) {
      logger.info(message, exception);
    } else if (level == Level.CONFIG) {
      logger.debug(message, exception);
    } else {
      logger.trace(message, exception);
    }
  }

  @Override
  public void close() throws SecurityException {
  }

  @NotNull
  private Logger getLogger(@NotNull final String name) {
    var logger = this.cachedLoggers.get(name);
    if (logger == null) {
      logger = LogManager.getLogger(name);
      this.cachedLoggers.put(name, logger);
    }
    return logger;
  }
}
