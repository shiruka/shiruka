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

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.NullTimingHandler;
import co.aikar.timings.Timing;
import java.util.function.Consumer;
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
   * the id.
   */
  private final int id;

  /**
   * the job.
   */
  @Nullable
  private final Consumer<Task> job;

  /**
   * the owner.
   */
  @Nullable
  private final Plugin owner;

  /**
   * the timings.
   */
  @NotNull
  public Timing timings;

  /**
   * the period.
   */
  private volatile long period;

  /**
   * ctor.
   *
   * @param id the id.
   * @param job the job.
   * @param owner the owner.
   * @param period the period.
   */
  public ShirukaTask(final int id, @Nullable final Consumer<Task> job, @Nullable final Plugin owner,
                     final long period) {
    this.id = id;
    this.job = job;
    this.owner = owner;
    this.period = period;
    if (job != null) {
      this.timings = MinecraftTimings.getPluginTaskTimings(this, period);
    } else {
      this.timings = NullTimingHandler.NULL;
    }
  }

  /**
   * ctor.
   *
   * @param id the id.
   * @param job the job.
   * @param jobName the job name.
   */
  public ShirukaTask(final int id, @Nullable final Consumer<Task> job, @NotNull final String jobName) {
    this.id = id;
    this.job = job;
    this.owner = ShirukaServer.INTERNAL_PLUGIN;
    this.period = ShirukaTask.PERIOD_NO_REPEATING;
    this.timings = MinecraftTimings.getInternalTaskName(jobName);
  }

  /**
   * ctor.
   *
   * @param task the task.
   */
  ShirukaTask(@NotNull final Consumer<Task> task) {
    this(ShirukaTask.PERIOD_NO_REPEATING, task, null, ShirukaTask.PERIOD_NO_REPEATING);
  }

  /**
   * ctor.
   */
  ShirukaTask() {
    this(ShirukaTask.PERIOD_NO_REPEATING, null, null, ShirukaTask.PERIOD_NO_REPEATING);
  }

  /**
   * obtains the job.
   *
   * @return job.
   */
  @Nullable
  public final Consumer<Task> getJob() {
    return this.job;
  }

  @Override
  public void cancel() {
  }

  @Nullable
  @Override
  public Plugin getOwner() {
    return this.owner;
  }

  @Override
  public int getTaskId() {
    return this.id;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isSync() {
    return true;
  }

  /**
   * obtains the period.
   *
   * @return period.
   */
  public long getPeriod() {
    return this.period;
  }

  /**
   * sets the period.
   *
   * @param period the period to set.
   */
  public void setPeriod(final long period) {
    this.period = period;
  }

  @Override
  public void run() {
  }
}
