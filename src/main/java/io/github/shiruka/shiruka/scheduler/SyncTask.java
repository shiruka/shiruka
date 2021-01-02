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

package io.github.shiruka.shiruka.scheduler;

import io.github.shiruka.api.base.Tick;
import java.util.LinkedList;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents sync tasks.
 */
final class SyncTask implements Tick {

  /**
   * the singleton.
   */
  private static final SyncTask SINGLETON = new SyncTask();

  /**
   * the task list.
   */
  private final Queue<Runnable> tasks = new LinkedList<>();

  /**
   * obtains the singleton instance.
   *
   * @return a singleton instance of {@code this}.
   */
  @NotNull
  static SyncTask getInstance() {
    return SyncTask.SINGLETON;
  }

  /**
   * adds the given task to the list.
   *
   * @param task the task to add.
   */
  void add(@NotNull final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.add(task);
    }
  }

  /**
   * removes the given task from the list.
   *
   * @param task the task to remove.
   */
  void remove(@NotNull final Runnable task) {
    synchronized (this.tasks) {
      this.tasks.remove(task);
    }
  }

  @Override
  public void tick() {
    synchronized (this.tasks) {
      Runnable next;
      while ((next = this.tasks.poll()) != null) {
        next.run();
      }
    }
  }
}
