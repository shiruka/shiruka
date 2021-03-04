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

import lombok.RequiredArgsConstructor;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;

/**
 * a class that tries to shutdown Shiru ka server.
 */
@RequiredArgsConstructor
public final class ShirukaShutdownThread extends Thread {

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  @Override
  public void run() {
    try {
      this.server.safeShutdown(false, false);
      for (var index = 1000; index > 0 && !this.server.isStopping(); index -= 100) {
        Thread.sleep(100L);
      }
      if (this.server.isStopping()) {
        while (!this.server.hasFullyShutdown) {
          Thread.sleep(1000L);
        }
        return;
      }
      AsyncCatcher.enabled = false;
      AsyncCatcher.shuttingDown = true;
      this.server.getTick().setForceTicks(true);
      this.server.stopServer();
      while (!this.server.hasFullyShutdown) {
        Thread.sleep(1000L);
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }
}
