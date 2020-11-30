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

import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

/**
 * a class which represents a message sent to the logger that
 * passes through the logging framework.
 */
public interface LogMessage {

  /**
   * appends all of the components and the message.
   *
   * @param start the start of the component index to add.
   *
   * @return the string.
   */
  @NotNull
  String format(int start);

  /**
   * additional info pertaining to the log message that
   * will be appended when it is printed.
   *
   * @return the additional info
   */
  @NotNull
  String[] getComponents();

  /**
   * obtains the source logger, or the logger which was
   * used to issue the log message to the framework.
   *
   * @return the source logger
   */
  @NotNull
  Logger getLogger();

  /**
   * the actual message to be logged.
   *
   * @return the message
   */
  @NotNull
  String getMessage();

  /**
   * sets the actual message.
   *
   * @param message the new message.
   */
  void setMessage(@NotNull String message);

  /**
   * obtains the time at which this message was created.
   *
   * @return the time
   */
  @NotNull
  ZonedDateTime getTime();
}
