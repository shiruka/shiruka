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

package net.shiruka.shiruka.util;

import io.github.portlek.configs.ConfigType;
import io.github.portlek.configs.json.JsonType;
import io.github.portlek.configs.yaml.YamlType;
import java.io.File;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for system.
 */
@Log4j2
public final class SystemUtils {

  /**
   * the config type error format.
   */
  private static final String CONFIG_TYPE_ERROR_FORMAT = "The file %s has not a valid ConfigType instance.";

  /**
   * the exception message.
   */
  private static final String EXCEPTION_MESSAGE = "Caught previously unhandled exception :";

  /**
   * the interrupted message.
   */
  private static final String INTERRUPTED = "Timer hack interrupted, that really should not happen!";

  /**
   * ctor.
   */
  private SystemUtils() {
  }

  /**
   * obtains the monotonic millis.
   *
   * @return monotonic millis.
   */
  public static long getMonotonicMillis() {
    return System.nanoTime() / 1000000L;
  }

  /**
   * gets the config type of the given file.
   *
   * @param file the file to get.
   *
   * @return config type instance.
   */
  @NotNull
  public static ConfigType getType(@NotNull final File file) {
    final var name = file.getName();
    if (name.contains(".yaml") ||
      name.contains(".yml")) {
      return YamlType.get();
    }
    if (name.contains(".json")) {
      return JsonType.get();
    }
    throw new IllegalStateException(String.format(SystemUtils.CONFIG_TYPE_ERROR_FORMAT, file.getName()));
  }

  /**
   * starts the timer hack.
   */
  public static void startTimerHack() {
    final var thread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(2147483647L);
        } catch (final InterruptedException interruptedexception) {
          SystemUtils.log.warn(SystemUtils.INTERRUPTED);
          return;
        }
      }
    }, "Timer Hack");
    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler((t, e) ->
      SystemUtils.log.error(SystemUtils.EXCEPTION_MESSAGE, e));
    thread.start();
  }
}
