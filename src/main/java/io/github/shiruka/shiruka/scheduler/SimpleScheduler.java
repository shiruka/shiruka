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

import com.google.common.collect.ForwardingCollection;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.ScheduledRunnable;
import io.github.shiruka.api.scheduler.ScheduledTask;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.TaskType;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link Scheduler}.
 */
public final class SimpleScheduler extends ForwardingCollection<ScheduledTask> implements Scheduler {

  /**
   * the pool.
   */
  private static final ServerThreadPool POOL = ServerThreadPool.forSpec(PoolSpec.SCHEDULER);

  /**
   * the task list.
   */
  private final Queue<ScheduledTask> tasks = new ConcurrentLinkedQueue<>();

  @NotNull
  @Override
  public ScheduledTask later(@NotNull final Plugin plugin, final boolean async, final long delay,
                             @NotNull final ScheduledRunnable runnable) {
    final var taskType = async ? TaskType.ASYNC_LATER : TaskType.SYNC_LATER;
    return this.createTask(plugin, runnable, taskType, delay);
  }

  @NotNull
  @Override
  public ScheduledTask repeat(@NotNull final Plugin plugin, final boolean async, final long delay,
                              final long initialInterval, @NotNull final ScheduledRunnable runnable) {
    return this.later(plugin, async, delay, new ScheduledRunnable() {
      @Override
      public void run() {
        final var taskType = async ? TaskType.ASYNC_REPEAT : TaskType.SYNC_REPEAT;
        SimpleScheduler.this.createTask(plugin, runnable, taskType, initialInterval);
      }
    });
  }

  @NotNull
  @Override
  public ScheduledTask run(@NotNull final Plugin plugin, final boolean async,
                           @NotNull final ScheduledRunnable runnable) {
    final var taskType = async ? TaskType.ASYNC_RUN : TaskType.SYNC_RUN;
    return this.createTask(plugin, runnable, taskType, -1);
  }

  /**
   * ticks the tasks.
   */
  public void tick() {
    this.tasks.forEach(Runnable::run);
  }

  @Override
  protected Collection<ScheduledTask> delegate() {
    return Collections.unmodifiableCollection(this.tasks);
  }

  /**
   * creates a new instance of {@link SimpleScheduledTask}.
   *
   * @param plugin the plugin to create.
   * @param runnable the runnable to create.
   * @param taskType the task type to create.
   * @param interval the interval to create.
   *
   * @return a new instance of scheduled task.
   */
  @NotNull
  private ScheduledTask createTask(@NotNull final Plugin plugin, @NotNull final ScheduledRunnable runnable,
                                   @NotNull final TaskType taskType, final long interval) {
    final Executor executor;
    if (taskType.name().contains("ASYNC")) {
      executor = command -> {
        // @todo #0:30m Continue to development here.
      };
    } else {
      executor = command -> {
      };
    }
    final Function<ScheduledTask, Runnable> runner;
    if (taskType.name().contains("REPEAT")) {
      runner = task -> () -> {
        runnable.beforeRun();
        runnable.run();
        runnable.afterAsyncRun();
      };
    } else {
      runner = task -> () -> {
        runnable.beforeRun();
        runnable.run();
        runnable.afterAsyncRun();
        task.cancel();
      };
    }
    final var task = new SimpleScheduledTask(executor, plugin, runnable, runner, taskType, interval);
    while (true) {
      if (this.tasks.add(task)) {
        task.runnable().markSchedule(task);
        return task;
      }
    }
  }

  /**
   * a simple implementation for {@link ScheduledTask}.
   */
  private final class SimpleScheduledTask implements ScheduledTask {

    /**
     * the executor.
     */
    @NotNull
    private final Executor executor;

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
     * the runner.
     */
    @NotNull
    private final Runnable runner;

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
     * the runç
     */
    private long run = 0L;

    /**
     * ctor.
     *
     * @param executor the executor.
     * @param plugin the plugin.
     * @param runnable the runnable.
     * @param runner the runner.
     * @param taskType the task type.
     * @param interval the step.
     */
    public SimpleScheduledTask(@NotNull final Executor executor, @NotNull final Plugin plugin,
                               @NotNull final ScheduledRunnable runnable,
                               @NotNull final Function<ScheduledTask, Runnable> runner,
                               @NotNull final TaskType taskType, final long interval) {
      this.executor = executor;
      this.plugin = plugin;
      this.runnable = runnable;
      this.taskType = taskType;
      this.interval = interval;
      this.runner = runner.apply(this);
    }

    @Override
    public void cancel() {
      SimpleScheduler.this.tasks.remove(this);
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
      switch (this.taskType) {
        case ASYNC_RUN:
        case SYNC_RUN:
          this.executor.execute(this.runner);
          break;
        case ASYNC_LATER:
        case SYNC_LATER:
          if (++this.run >= this.interval) {
            this.executor.execute(this.runner);
          }
          break;
        case ASYNC_REPEAT:
        case SYNC_REPEAT:
          if (++this.run >= this.interval) {
            this.executor.execute(this.runner);
            this.run = 0;
          }
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }
}
