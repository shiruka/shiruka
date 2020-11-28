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

import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * represents the server logger, whether or not there is a physical logger on the working machine.
 */
public interface Logger {

  /**
   * logs the given string as a debug message to the logger.
   *
   * @param msg the string to log.
   */
  void debug(@NotNull String msg);

  /**
   * logs the given string to the logger indicative of an error.
   *
   * @param msg the string to log.
   */
  void error(@NotNull String msg);

  /**
   * obtains the name of this logger.
   *
   * @return the name.
   */
  @NotNull
  String getName();

  /**
   * obtains the raw output that the underlying logger logs messages.
   *
   * @return the output stream.
   */
  @NotNull
  OutputStream getOutputStream();

  /**
   * logs the given string to the logger without any color formatting.
   *
   * @param msg the string to log.
   */
  void log(@NotNull String msg);

  /**
   * logs the given string to the logger indicative of a successful operation.
   *
   * @param msg the string to log.
   */
  void success(@NotNull String msg);

  /**
   * logs the given string to the logger indicative of a warning.
   *
   * @param msg the string to log.
   */
  void warn(@NotNull String msg);
}
