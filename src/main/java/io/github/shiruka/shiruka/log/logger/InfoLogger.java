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

import static java.time.temporal.ChronoField.*;
import io.github.shiruka.api.log.Logger;
import io.github.shiruka.shiruka.log.Levels;
import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.PipelineLogger;
import io.github.shiruka.shiruka.log.message.LogMessageBasic;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * not part of the pipeline, this logger is the surface
 * level logger that is given to plugins and appends the
 * information such as date and time and the logger name to
 * the message logged to the logger.
 */
public final class InfoLogger extends LoggerHandlers implements Logger {

  public static final String LOG_PREFIX_FORMAT = "[%s]";

  /**
   * the logger cache.
   */
  private static final Map<String, InfoLogger> CACHE = new ConcurrentHashMap<>();

  /**
   * the date, using MMM DD YYYY.
   */
  private static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
    .appendText(MONTH_OF_YEAR, TextStyle.SHORT)
    .appendLiteral(' ')
    .appendValue(DAY_OF_MONTH, 2)
    .appendLiteral(' ')
    .appendValue(YEAR, 4)
    .toFormatter();

  /**
   * the time, using standard 24-hour format.
   */
  private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
    .appendValue(HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(MINUTE_OF_HOUR, 2)
    .appendLiteral(':')
    .appendValue(SECOND_OF_MINUTE, 2)
    .toFormatter();

  /**
   * the name of this logger.
   */
  @NotNull
  private final String name;

  /**
   * the top logger in the pipeline.
   */
  @NotNull
  private final PipelineLogger next;

  /**
   * ctor.
   *
   * @param next the next logger in the pipeline.
   * @param name the name.
   */
  private InfoLogger(@NotNull final PipelineLogger next, @NotNull final String name) {
    super(PipelineLogger.EMPTY);
    this.next = next;
    this.name = name;
  }

  /**
   * gets a logger from the cache.
   *
   * @param next the next logger.
   * @param name the name of the logger from the cache.
   *
   * @return a cached logger, or a new one.
   */
  @NotNull
  public static Logger get(@NotNull final PipelineLogger next, @NotNull final String name) {
    return InfoLogger.CACHE.computeIfAbsent(name, k -> new InfoLogger(next, name));
  }

  @Override
  public void debug(@NotNull final String msg) {
    this.next.debug(this.handle(Levels.DEBUG, msg));
  }

  @Override
  public void error(@NotNull final String msg) {
    this.next.error(this.handle(Levels.ERROR, msg));
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @NotNull
  @Override
  public OutputStream getOutputStream() {
    return this.next.out();
  }

  @Override
  public void log(@NotNull final String msg) {
    this.next.log(this.handle(Levels.INFO, msg));
  }

  @Override
  public void success(@NotNull final String msg) {
    this.next.success(this.handle(Levels.INFO, msg));
  }

  @Override
  public void warn(@NotNull final String msg) {
    this.next.warn(this.handle(Levels.WARN, msg));
  }

  /**
   * handles normal messages.
   */
  @NotNull
  private LogMessage handle(@NotNull final String level, @NotNull final String s) {
    final var time = ZonedDateTime.now();
    final var components = new String[]{
      time.format(InfoLogger.DATE_FORMAT),
      time.format(InfoLogger.TIME_FORMAT),
      String.format(InfoLogger.LOG_PREFIX_FORMAT, level)
    };
    return super.handle(new LogMessageBasic(this, components, s, time));
  }
}
