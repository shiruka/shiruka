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

import io.github.shiruka.shiruka.log.logger.InfoLogger;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
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
   * creates a new {@link Logger} instance.
   *
   * @param name the name to create.
   * @param debug the debug mode to create.
   *
   * @return a new {@link Logger} instance.
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
   */
  @NotNull
  public static Logger create(@NotNull final String name) throws IOException {
    return Loggers.create(name, true);
  }

  /**
   * logs the given string to the global logger in debug mode.
   *
   * @param msg the string to log.
   */
  public static void debug(@NotNull final String msg) {
    Loggers.useLogger(logger -> logger.debug(msg));
  }

  /**
   * logs the given string to the global logger in debug mode.
   *
   * @param msg the string to log.
   * @param params the params to format the given message.
   */
  public static void debug(@NotNull final String msg, @NotNull final Object... params) {
    Loggers.debug(String.format(msg, params));
  }

  /**
   * logs the given string to the global logger in error mode.
   *
   * @param msg the string to log.
   */
  public static void error(@NotNull final String msg) {
    Loggers.useLogger(logger -> logger.error(msg));
  }

  /**
   * logs the given string to the global logger in error mode.
   *
   * @param msg the string to log.
   * @param params the params to format the given message.
   */
  public static void error(@NotNull final String msg, @NotNull final Object... params) {
    Loggers.error(String.format(msg, params));
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
  public static Logger init(@NotNull final String name) throws Exception {
    return Loggers.init(name, true);
  }

  /**
   * initiates the global logger.
   *
   * @param name the name to init the global logger.
   * @param debug the debug mode to init.
   *
   * @return the initialized logger instance.
   */
  @NotNull
  public static Logger init(@NotNull final String name, final boolean debug) throws Exception {
    return Loggers.init(Loggers.create(name, debug));
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
   * logs the given string to the global logger in log mode.
   *
   * @param msg the string to log.
   */
  public static void log(@NotNull final String msg) {
    Loggers.useLogger(logger -> logger.log(msg));
  }

  /**
   * logs the given string to the global logger in log mode.
   *
   * @param msg the string to log.
   * @param params the params to format the given message.
   */
  public static void log(@NotNull final String msg, @NotNull final Object params) {
    Loggers.log(String.format(msg, params));
  }

  /**
   * logs the given string to the global logger in success mode.
   *
   * @param msg the string to log.
   */
  public static void success(@NotNull final String msg) {
    Loggers.useLogger(logger -> logger.success(msg));
  }

  /**
   * logs the given string to the global logger in success mode.
   *
   * @param msg the string to log..
   * @param params the params to format the given message.
   */
  public static void success(@NotNull final String msg, @NotNull final Object params) {
    Loggers.success(String.format(msg, params));
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

  /**
   * logs the given string to the global logger in warn mode.
   *
   * @param msg the string to log.
   */
  public static void warn(@NotNull final String msg) {
    Loggers.useLogger(logger -> logger.warn(msg));
  }

  /**
   * logs the given string to the global logger in warn mode.
   *
   * @param msg the string to log.
   * @param params the params to format the given message.
   */
  public static void warn(@NotNull final String msg, @NotNull final Object... params) {
    Loggers.warn(String.format(msg, params));
  }
}
