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

import com.google.common.base.Preconditions;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that helps to catch asynchronous.
 */
public final class AsyncCatcher {

  /**
   * the enable.
   */
  public static boolean enabled = true;

  /**
   * the server.
   */
  @Nullable
  public static ShirukaServer server;

  /**
   * the shutting down.
   */
  public static boolean shuttingDown = false;

  /**
   * ctor.
   */
  private AsyncCatcher() {
  }

  /**
   * catches and throws an exception with the given {@code reason}.
   *
   * @param reason the reason to catch.
   */
  public static void catchAsync(@NotNull final String reason) {
    Preconditions.checkState(AsyncCatcher.caught(), "Asynchronous %s!", reason);
  }

  /**
   * catches and throws an exception.
   */
  public static void catchAsync() {
    AsyncCatcher.catchAsync("operations forbidden here");
  }

  /**
   * checks if the current thread is not the server thread.
   *
   * @return {@code true} if the current thread is not the server thread.
   */
  private static boolean caught() {
    if (AsyncCatcher.server == null) {
      return false;
    }
    return !AsyncCatcher.enabled || Thread.currentThread() == AsyncCatcher.server.getServerThread();
  }
}
