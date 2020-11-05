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

package io.github.shiruka.shiruka;

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.log.Loggers;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class Main {

  /**
   * the program arguments.
   */
  @NotNull
  private final String[] args;

  /**
   * ctor.
   *
   * @param args the program arguments.
   */
  public Main(@NotNull final String[] args) {
    this.args = args.clone();
  }

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) throws Exception {
    new Main(args).exec();
  }

  /**
   * execs the Java program.
   */
  private void exec() throws Exception {
    Loggers.init("Shiru ka");
    final var server = new ShirukaServer();
    Shiruka.setServer(server);
  }
}
