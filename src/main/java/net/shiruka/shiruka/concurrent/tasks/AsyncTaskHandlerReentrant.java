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

package net.shiruka.shiruka.concurrent.tasks;

import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link AsyncTaskHandler} which
 */
public abstract class AsyncTaskHandlerReentrant<R extends Runnable> extends AsyncTaskHandler<R> {

  /**
   * the reentrant count.
   */
  private final AtomicInteger reentrantCount = new AtomicInteger();

  /**
   * ctor.
   *
   * @param threadName the thread name.
   */
  protected AsyncTaskHandlerReentrant(@NotNull final String threadName) {
    super(threadName);
  }

  @Override
  public final void executeTask(@NotNull final R job) {
    this.reentrantCount.incrementAndGet();
    try {
      super.executeTask(job);
    } finally {
      this.reentrantCount.decrementAndGet();
    }
  }

  @Override
  public boolean isNotMainThread() {
    return this.isEntered() || super.isNotMainThread();
  }

  public final boolean isEntered() {
    return this.reentrantCount.get() != 0;
  }
}
