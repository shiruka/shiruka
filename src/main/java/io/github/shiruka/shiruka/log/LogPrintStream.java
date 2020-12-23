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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * a simple {@link PrintStream} implementation that helps to cover {@link System#out} and {@link System#err}.
 */
public final class LogPrintStream extends PrintStream {

  /**
   * ctor.
   *
   * @param message the message.
   */
  public LogPrintStream(@NotNull final Consumer<String> message) {
    super(new OutputStream() {

      /**
       * the memory.
       */
      @NotNull
      private String memory = "";

      @Override
      public void write(final int b) {
        final var bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        this.memory = this.memory + new String(bytes);
        if (this.memory.endsWith("\n")) {
          this.memory = this.memory.substring(0, this.memory.length() - 1);
          this.flush();
        }
      }

      @Override
      public void flush() {
        message.accept(this.memory);
        this.memory = "";
      }
    });
  }
}
