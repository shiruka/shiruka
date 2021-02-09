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

import org.jetbrains.annotations.NotNull;

/**
 * a class that handles asynchronous tasks.
 *
 * @param <T> type of the task.
 */
public abstract class AsyncTaskHandlerReentrant<T extends Runnable> extends AsyncTaskHandler<T> {

  /**
   * the depth.
   */
  private int depth;

  /**
   * ctor.
   *
   * @param threadName the thread name.
   */
  protected AsyncTaskHandlerReentrant(@NotNull final String threadName) {
    super(threadName);
  }

  @Override
  protected final void executeTask(@NotNull final T task) {
    ++this.depth;
    try {
      super.executeTask(task);
    } finally {
      --this.depth;
    }
  }

  @Override
  protected boolean isNotMainThread() {
    return this.isEntered() || super.isNotMainThread();
  }

  /**
   * checks if the task entered.
   *
   * @return {@code true} if the task entered.
   */
  protected final boolean isEntered() {
    return this.depth != 0;
  }
}
