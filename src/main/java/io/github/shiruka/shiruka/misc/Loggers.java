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

package io.github.shiruka.shiruka.misc;

import java.util.Optional;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains some instance.
 */
public final class Loggers {

  /**
   * the logger instance.
   */
  @Nullable
  private static Logger global;

  /**
   * ctor.
   */
  private Loggers() {
  }

  /**
   * obtains the global {@link Logger} instance.
   *
   * @return the global {@link Logger} instance.
   */
  @NotNull
  public static Optional<Logger> getLogger() {
    return Optional.ofNullable(Loggers.global);
  }

  /**
   * initiates the global logger.
   *
   * @param name the name to init the global logger.
   *
   * @return the initialized logger instance.
   */
  @NotNull
  public static Logger init(@NotNull final String name) {
    return Loggers.init(LogManager.getLogger(name));
  }

  /**
   * sets the global logger instance.
   *
   * @param logger the logger to set.
   *
   * @return the initialized logger instance.
   */
  @NotNull
  public static Logger init(@NotNull final Logger logger) {
    Loggers.global = logger;
    return Loggers.global;
  }

  /**
   * runs your code with the logger.
   * if the global logger not found runs the given {@link Runnable}.
   *
   * @param consumer the consumer to run.
   * @param notExist the not exist to run if the global logger not exist.
   */
  public static void useLogger(@NotNull final Consumer<Logger> consumer, @NotNull final Runnable notExist) {
    Loggers.getLogger().ifPresentOrElse(consumer, notExist);
  }

  /**
   * runs your code with the logger.
   *
   * @param consumer the consumer to run.
   */
  public static void useLogger(@NotNull final Consumer<Logger> consumer) {
    Loggers.getLogger().ifPresent(consumer);
  }
}
