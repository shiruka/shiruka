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

import io.github.shiruka.api.scheduler.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class that represents sync tasks.
 */
public final class SyncTask implements Task, Runnable {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("SyncTask");

  /**
   * the task.
   */
  @NotNull
  private final Runnable task;

  /**
   * the complete handler list.
   */
  @Nullable
  private List<Runnable> completeHandlers;

  /**
   * the exception handler.
   */
  @Nullable
  private Predicate<Exception> exceptionHandler;

  /**
   * the next execution.
   */
  private long nextExecution;

  /**
   * the period.
   */
  private long period;

  /**
   * ctor.
   *
   * @param task the task.
   * @param delay the delay.
   * @param period the period.
   * @param unit the unit.
   */
  public SyncTask(@NotNull final Runnable task, final long delay,
                  final long period, @NotNull final TimeUnit unit) {
    this.task = task;
    this.period = period >= 0 ? unit.toMillis(period) : -1;
    this.nextExecution = delay >= 0 ? System.currentTimeMillis() + unit.toMillis(delay) : -1;
  }

  @Override
  public void cancel() {
    this.period = -1;
    this.nextExecution = -1;
    this.fireCompleteHandlers();
    SyncTaskManager.removeTask(this);
  }

  @Override
  public void onComplete(@NotNull final Runnable command) {
    if (this.completeHandlers == null) {
      this.completeHandlers = new ArrayList<>();
    }
    this.completeHandlers.add(command);
  }

  @Override
  public void onException(@NotNull final Predicate<Exception> command) {
    this.exceptionHandler = command;
  }

  /**
   * obtains the next execution.
   *
   * @return next execution.
   */
  public long getNextExecution() {
    return this.nextExecution;
  }

  /**
   * obtains the task.
   *
   * @return task.
   */
  @NotNull
  public Runnable getTask() {
    return this.task;
  }

  @Override
  public void run() {
    try {
      this.task.run();
    } catch (final Exception e) {
      if (this.exceptionHandler != null) {
        if (!this.exceptionHandler.test(e)) {
          this.cancel();
        }
      } else {
        SyncTask.LOGGER.warn("Error in executing task: ", e);
      }
    }
    if (this.period > 0) {
      this.nextExecution = System.currentTimeMillis() + this.period;
    } else {
      this.cancel();
    }
  }

  /**
   * fires the complete handlers.
   */
  private void fireCompleteHandlers() {
    if (this.completeHandlers == null) {
      return;
    }
    this.completeHandlers.forEach(Runnable::run);
    this.completeHandlers = null;
  }
}
