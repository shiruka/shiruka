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

import io.github.shiruka.api.misc.ThrowableRunnable;
import io.github.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;

/**
 * catches exceptions in the server process that are
 * unrecoverable and pastes a link for creating an issue
 * with details pulled from the environment.
 */
public final class JiraExceptionCatcher {

  /**
   * ctor.
   */
  private JiraExceptionCatcher() {
  }

  /**
   * runs and catches the exceptions and uses {@link #serverException(Exception)} to report it.
   *
   * @param run the runnable to run.
   */
  public static void run(@NotNull final ThrowableRunnable run) {
    try {
      run.call();
    } catch (final Exception e) {
      JiraExceptionCatcher.serverException(e);
    }
  }

  /**
   * catches the given exception and reformats it.
   *
   * @param e the exception to catch.
   */
  public static void serverException(@NotNull final Exception e) {
    final var url = "https://github.com/shiruka/shiruka/issues/new";
    final var environment = "Shiru ka Version: " + ShirukaServer.VERSION + "\n" +
      "Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")\n" +
      "System Architecture: " + System.getProperty("os.arch") + "\n" +
      "Java Version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")";
    final var o = System.err;
    o.println();
    o.println("Unhandled Exception occurred while starting the server.");
    o.println("This was not intended to happen.");
    o.println("Please report this at " + url);
    o.println();
    o.println("SYSTEM INFO:");
    o.println("============");
    o.println(environment);
    o.println();
    o.println("STACKTRACE");
    o.println("==========");
    e.printStackTrace();
  }
}
