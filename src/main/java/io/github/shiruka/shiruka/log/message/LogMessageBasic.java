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
package io.github.shiruka.shiruka.log.message;

import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.Logger;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents a message sent by a logger.
 */
public final class LogMessageBasic implements LogMessage {

  /**
   * the components to be added to the output.
   */
  @NotNull
  private final String[] components;

  /**
   * the logger that sent the message.
   */
  @NotNull
  private final Logger source;

  /**
   * the time at which this log message was created.
   */
  @NotNull
  private final ZonedDateTime time;

  /**
   * the message that was passed to the logger.
   */
  @NotNull
  private volatile String message;

  /**
   * ctor.
   *
   * @param source the logger that created the message
   * @param components additional related info
   * @param message the message that was passed
   * @param time the time the message was created
   */
  public LogMessageBasic(@NotNull final Logger source, @NotNull final String[] components,
                         @NotNull final String message, @NotNull final ZonedDateTime time) {
    this.source = source;
    this.components = components.clone();
    this.time = time;
    this.message = message;
  }

  @NotNull
  @Override
  public String format(final int start) {
    return IntStream.range(start, this.components.length)
      .mapToObj(i -> this.components[i] + ' ')
      .collect(Collectors.joining("", "", this.message));
  }

  @NotNull
  @Override
  public String[] getComponents() {
    return this.components.clone();
  }

  @NotNull
  @Override
  public Logger getLogger() {
    return this.source;
  }

  @NotNull
  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public void setMessage(@NotNull final String message) {
    this.message = message;
  }

  @NotNull
  @Override
  public ZonedDateTime getTime() {
    return this.time;
  }
}
