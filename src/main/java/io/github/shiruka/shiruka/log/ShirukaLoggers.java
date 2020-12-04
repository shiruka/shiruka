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

import io.github.shiruka.api.log.Logger;
import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.shiruka.log.logger.InfoLogger;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains some instance.
 */
public final class ShirukaLoggers {

  /**
   * ctor.
   */
  private ShirukaLoggers() {
  }

  /**
   * creates a new {@link Logger} instance.
   *
   * @param name the name to create.
   * @param debug the debug mode to create.
   *
   * @return a new {@link Logger} instance.
   *
   * @throws IOException pass down exception.
   */
  @NotNull
  public static Logger create(@NotNull final String name, final boolean debug) throws IOException {
    return InfoLogger.get(PipelineLogger.init(debug), name);
  }

  /**
   * creates a new {@link Logger} instance.
   *
   * @param name the name to create.
   *
   * @return a new {@link Logger} instance.
   *
   * @throws IOException pass down exception.
   */
  @NotNull
  public static Logger create(@NotNull final String name) throws IOException {
    return ShirukaLoggers.create(name, true);
  }

  /**
   * initiates the global logger.
   *
   * @param name the name to init the global logger.
   *
   * @return the initialized logger instance.
   *
   * @throws IOException pass down exception.
   */
  @NotNull
  public static Logger init(@NotNull final String name) throws IOException {
    return ShirukaLoggers.init(name, true);
  }

  /**
   * initiates the global logger.
   *
   * @param name the name to init the global logger.
   * @param debug the debug mode to init.
   *
   * @return the initialized logger instance.
   *
   * @throws IOException pass down exception.
   */
  @NotNull
  public static Logger init(@NotNull final String name, final boolean debug) throws IOException {
    return Loggers.init(ShirukaLoggers.create(name, debug));
  }
}
