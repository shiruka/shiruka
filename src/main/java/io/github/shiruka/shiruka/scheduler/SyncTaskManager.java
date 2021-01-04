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

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents sync task managers.
 */
public final class SyncTaskManager {

  /**
   * the task list.
   */
  private static final PriorityQueue<SyncTaskHolder> TASKS = new PriorityQueue<>(
    Comparator.comparingLong(o -> o.execution));

  /**
   * ctor.
   */
  private SyncTaskManager() {
  }

  /**
   * adds the given task to the {@link #TASKS}.
   *
   * @param task the task to add.
   */
  public static void addTask(@NotNull final SyncTask task) {
    if (task.getNextExecution() == -1) {
      return;
    }
    synchronized (SyncTaskManager.TASKS) {
      SyncTaskManager.TASKS.add(new SyncTaskHolder(task.getNextExecution(), task));
    }
  }

  /**
   * updates and run all tasks which should be run.
   *
   * @param currentMillis the current millis to update.
   */
  public static void update(final long currentMillis) {
    synchronized (SyncTaskManager.TASKS) {
      while (SyncTaskManager.TASKS.peek() != null &&
        SyncTaskManager.TASKS.peek().execution < currentMillis) {
        final var holder = SyncTaskManager.TASKS.poll();
        if (holder == null) {
          return;
        }
        final var task = holder.task;
        if (task.getNextExecution() == -1) {
          continue;
        }
        task.run();
        if (task.getNextExecution() > currentMillis) {
          SyncTaskManager.TASKS.add(new SyncTaskHolder(task.getNextExecution(), task));
        }
      }
    }
  }

  /**
   * removes the given task from {@link #TASKS}.
   *
   * @param task the task to remove.
   */
  static void removeTask(@NotNull final SyncTask task) {
    synchronized (SyncTaskManager.TASKS) {
      SyncTaskManager.TASKS.remove(new SyncTaskHolder(-1, task));
    }
  }

  /**
   * a class that represents a sync task holders.
   */
  private static final class SyncTaskHolder {

    /**
     * the execution.
     */
    private final long execution;

    /**
     * the task.
     */
    @NotNull
    private final SyncTask task;

    /**
     * ctor.
     *
     * @param execution the execution.
     * @param task the task.
     */
    SyncTaskHolder(final long execution, @NotNull final SyncTask task) {
      this.execution = execution;
      this.task = task;
    }

    /**
     * obtains the execution.
     *
     * @return execution.
     */
    public long getExecution() {
      return this.execution;
    }

    /**
     * obtains the task.
     *
     * @return task.
     */
    @NotNull
    public SyncTask getTask() {
      return this.task;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.task);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || this.getClass() != obj.getClass()) {
        return false;
      }
      final var that = (SyncTaskHolder) obj;
      return Objects.equals(this.task, that.task);
    }
  }
}
