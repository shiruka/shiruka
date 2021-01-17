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

package io.github.shiruka.shiruka.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * a class that contains utility methods for system.
 */
public final class SystemUtils {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("SystemUtils");

  /**
   * ctor.
   */
  private SystemUtils() {
  }

  /**
   * starts the timer hack.
   */
  public static void startTimerHack() {
    final var exceptionMessage = "Caught previously unhandled exception :";
    final var thread = new Thread("Timer Hack") {
      @Override
      public void run() {
        while (true) {
          try {
            Thread.sleep(2147483647L);
          } catch (final InterruptedException interruptedexception) {
            SystemUtils.LOGGER.warn("Timer hack interrupted, that really should not happen!");
            return;
          }
        }
      }
    };
    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler((t, e) ->
      SystemUtils.LOGGER.error(exceptionMessage, e));
    thread.start();
  }
}
