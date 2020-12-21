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

import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.ScheduledRunnable;
import io.github.shiruka.api.scheduler.ScheduledTask;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.TaskType;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link Scheduler}.
 */
public final class SimpleScheduler implements Scheduler {

  /**
   * the pool.
   */
  private static final ServerThreadPool POOL = ServerThreadPool.forSpec(PoolSpec.SCHEDULER);

  @NotNull
  @Override
  public ScheduledTask later(@NotNull final Plugin plugin, final boolean async, final long delay,
                             @NotNull final ScheduledRunnable runnable) {
    final var taskType = async ? TaskType.ASYNC_LATER : TaskType.SYNC_LATER;
    return new SimpleScheduledTask(plugin, runnable, taskType, delay);
  }

  @NotNull
  @Override
  public ScheduledTask repeat(@NotNull final Plugin plugin, final boolean async, final long delay,
                              final long initialInterval, @NotNull final ScheduledRunnable runnable) {
    return this.later(plugin, async, delay, new ScheduledRunnable() {
      @Override
      public void run() {
        final var taskType = async ? TaskType.ASYNC_REPEAT : TaskType.SYNC_REPEAT;
        new SimpleScheduledTask(plugin, runnable, taskType, initialInterval);
      }
    });
  }

  @NotNull
  @Override
  public ScheduledTask run(@NotNull final Plugin plugin, final boolean async,
                           @NotNull final ScheduledRunnable runnable) {
    final var taskType = async ? TaskType.ASYNC_RUN : TaskType.SYNC_RUN;
    return new SimpleScheduledTask(plugin, runnable, taskType, -1);
  }

  /**
   * a simple implementation for {@link ScheduledTask}.
   */
  private static final class SimpleScheduledTask implements ScheduledTask {

    /**
     * the plugin.
     */
    @NotNull
    private final Plugin plugin;

    /**
     * the runnable.
     */
    @NotNull
    private final ScheduledRunnable runnable;

    /**
     * the task type.
     */
    @NotNull
    private final TaskType taskType;

    /**
     * the interval.
     */
    private long interval;

    /**
     * ctor.
     *
     * @param plugin the plugin.
     * @param runnable the runnable.
     * @param taskType the task type.
     * @param interval the step.
     */
    public SimpleScheduledTask(@NotNull final Plugin plugin, @NotNull final ScheduledRunnable runnable,
                               @NotNull final TaskType taskType, final long interval) {
      this.plugin = plugin;
      this.runnable = runnable;
      this.taskType = taskType;
      this.interval = interval;
    }

    @Override
    public void cancel() {
    }

    @Override
    public long interval() {
      return this.interval;
    }

    @NotNull
    @Override
    public Plugin owner() {
      return this.plugin;
    }

    @NotNull
    @Override
    public ScheduledRunnable runnable() {
      return this.runnable;
    }

    @Override
    public void setInterval(final long interval) {
      this.interval = interval;
    }

    @NotNull
    @Override
    public TaskType type() {
      return this.taskType;
    }

    @Override
    public void run() {
    }

    /**
     * initiates the task.
     */
    private void initiate() {
    }
  }
}
