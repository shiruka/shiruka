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

import lombok.extern.log4j.Log4j2;

/**
 * a class that contains utility methods for system.
 */
@Log4j2
public final class SystemUtils {

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
            SystemUtils.log.warn("Timer hack interrupted, that really should not happen!");
            return;
          }
        }
      }
    };
    thread.setDaemon(true);
    thread.setUncaughtExceptionHandler((t, e) ->
      SystemUtils.log.error(exceptionMessage, e));
    thread.start();
  }
}
