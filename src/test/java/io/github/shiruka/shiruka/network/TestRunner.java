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
package io.github.shiruka.shiruka.network;

import io.github.shiruka.shiruka.network.server.NetServerSocket;
import io.github.shiruka.shiruka.network.util.Misc;
import java.io.File;
import java.util.Arrays;

/**
 * a class that tests the project in a simple way.
 */
public final class TestRunner {

  /**
   * main method of the Java to start the program.
   *
   * @param args the program arguments.
   *
   * @throws Exception if somethings going wrong about the logger file.
   */
  public static void main(final String[] args) throws Exception {
    new TestRunner().exec();
  }

  /**
   * execs the program.
   *
   * @throws Exception if somethings going wrong about the logger file.
   */
  private void exec() throws Exception {
    final var logs = Misc.HOME_PATH.resolve("logs").toFile().listFiles();
    if (logs != null) {
      Arrays.stream(logs).forEach(File::delete);
    }
    NetServerSocket.init(TestServerListener.INSTANCE);
    while (true) {
      Thread.sleep(10L);
    }
  }
}
