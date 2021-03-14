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

package net.shiruka.shiruka.misc;

import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.cactoos.Scalar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents waitable operations.
 *
 * @param <T> type of the end vlaue.
 */
@RequiredArgsConstructor
public final class Waitable<T> implements Runnable {

  /**
   * the supplier.
   */
  @NotNull
  private final Scalar<T> scalar;

  /**
   * the cache.
   */
  @Nullable
  private T cache;

  /**
   * the status.
   */
  @NotNull
  private Status status = Status.WAITING;

  /**
   * the throwable.
   */
  @Nullable
  private Throwable throwable;

  /**
   * obtains the value when the calculation done.
   *
   * @return value.
   *
   * @throws InterruptedException when something went wrong waiting the calculation.
   * @throws ExecutionException when something went wrong when calculating the result.
   */
  @Nullable
  public synchronized T get() throws InterruptedException, ExecutionException {
    while (this.status != Status.FINISHED) {
      this.wait();
    }
    if (this.throwable != null) {
      throw new ExecutionException(this.throwable);
    }
    return this.cache;
  }

  @Override
  public void run() {
    synchronized (this) {
      if (this.status != Status.WAITING) {
        throw new IllegalStateException("Invalid state " + this.status);
      }
      this.status = Status.RUNNING;
    }
    try {
      this.cache = this.scalar.value();
    } catch (final Throwable t) {
      this.throwable = t;
    } finally {
      synchronized (this) {
        this.status = Status.FINISHED;
        this.notifyAll();
      }
    }
  }

  /**
   * an enum class that represents status of the calculation.
   */
  private enum Status {
    /**
     * the waiting.
     */
    WAITING,
    /**
     * the running.
     */
    RUNNING,
    /**
     * the finished.
     */
    FINISHED,
  }
}
