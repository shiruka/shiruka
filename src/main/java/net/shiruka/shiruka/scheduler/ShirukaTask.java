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

package net.shiruka.shiruka.scheduler;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Task;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Task} which is sync.
 */
public class ShirukaTask implements Task, Runnable {

  /**
   * the period of cancel.
   */
  public static final int PERIOD_CANCEL = -2;

  /**
   * the period of done for future.
   */
  public static final int PERIOD_DONE_FOR_FUTURE = -4;

  /**
   * the period of error.
   */
  public static final int PERIOD_ERROR = 0;

  /**
   * the period of no repeating.
   */
  public static final int PERIOD_NO_REPEATING = -1;

  /**
   * the period of process for future.
   */
  public static final int PERIOD_PROCESS_FOR_FUTURE = -3;

  /**
   * the owner.
   */
  @Nullable
  @Getter
  private final Plugin owner;

  /**
   * the job.
   */
  @Nullable
  @Getter
  private final Consumer<ShirukaTask> task;

  /**
   * the task id.
   */
  @Getter
  private final int taskId;

  /**
   * the next task.
   */
  @Nullable
  @Getter
  @Setter
  private volatile ShirukaTask next;

  /**
   * the next run.
   */
  @Getter
  @Setter
  private long nextRun;

  /**
   * the period.
   */
  @Getter
  @Setter
  private volatile long period;

  /**
   * ctor.
   *
   * @param taskId the task id.
   * @param task the job.
   * @param owner the owner.
   * @param period the period.
   */
  public ShirukaTask(final int taskId, @Nullable final Consumer<ShirukaTask> task, @Nullable final Plugin owner,
                     final long period) {
    this.taskId = taskId;
    this.task = task;
    this.owner = owner;
    this.period = period;
  }

  /**
   * ctor.
   *
   * @param taskId the task id.
   * @param task the job.
   */
  public ShirukaTask(final int taskId, @Nullable final Consumer<ShirukaTask> task) {
    this.taskId = taskId;
    this.task = task;
    this.owner = ShirukaServer.INTERNAL_PLUGIN;
    this.period = ShirukaTask.PERIOD_NO_REPEATING;
  }

  /**
   * ctor.
   *
   * @param task the task.
   */
  ShirukaTask(@NotNull final Consumer<ShirukaTask> task) {
    this(ShirukaTask.PERIOD_NO_REPEATING, task, null, ShirukaTask.PERIOD_NO_REPEATING);
  }

  /**
   * ctor.
   */
  ShirukaTask() {
    this(ShirukaTask.PERIOD_NO_REPEATING, null, null, ShirukaTask.PERIOD_NO_REPEATING);
  }

  @Override
  public final void cancel() {
    Shiruka.getScheduler().cancelTask(this.taskId);
  }

  @Override
  public final boolean isCancelled() {
    return this.period == ShirukaTask.PERIOD_CANCEL;
  }

  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public void run() {
    if (this.task != null) {
      this.task.accept(this);
    }
  }

  /**
   * sets the status to cancelled, synchronizing when required.
   */
  void cancel0() {
    this.setPeriod(ShirukaTask.PERIOD_CANCEL);
  }
}
