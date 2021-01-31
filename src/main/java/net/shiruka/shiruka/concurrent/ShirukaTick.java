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

package net.shiruka.shiruka.concurrent;

import java.util.concurrent.TimeUnit;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.util.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents the server heartbeat pulse called "tick" which occurs every 1/20th of a second.
 */
public final class ShirukaTick implements Runnable {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("ShirukaTick");

  /**
   * the amount of time taken by a single tick.
   */
  private static final long TICK_NANOS = TimeUnit.SECONDS.toNanos(1) / 20;

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the ticks.
   */
  private int ticks;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaTick(@NotNull final ShirukaServer server) {
    this.server = server;
  }

  /**
   * starts the heartbeat.
   */
  @Override
  public void run() {
    while (this.server.isRunning()) {
      try {
        final var start = SystemUtils.getMonotonicNanos();
        this.doTick();
        final var end = SystemUtils.getMonotonicNanos();
        final var elapsed = end - start;
        final var tick = (float) Math.min(20, 1000000000 / Math.max(1000000, elapsed));
        System.out.println(tick);
        final var waitTime = TimeUnit.NANOSECONDS.toMillis(ShirukaTick.TICK_NANOS - elapsed);
        if (waitTime < 0) {
          ShirukaTick.LOGGER.debug("Server running behind " +
            -waitTime + "ms, skipped " + -waitTime / ShirukaTick.TICK_NANOS + " ticks");
        } else {
          Thread.sleep(waitTime);
        }
      } catch (final InterruptedException e) {
        break;
      } catch (final Exception e) {
        JiraExceptionCatcher.serverException(e);
        break;
      }
    }
  }

  /**
   * does the tick operations.
   */
  private void doTick() {
    this.server.getScheduler().mainThreadHeartbeat(++this.ticks);
  }
}
