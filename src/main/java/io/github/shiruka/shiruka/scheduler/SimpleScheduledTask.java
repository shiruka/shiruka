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
import io.github.shiruka.api.scheduler.TaskType;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link ScheduledTask}.
 */
public final class SimpleScheduledTask implements ScheduledTask {

  /**
   * the pool for async.
   */
  private static final ServerThreadPool POOL = ServerThreadPool.forSpec(PoolSpec.SCHEDULER);

  /**
   * the executor.
   */
  @NotNull
  private final Executor executor;

  /**
   * the on close.
   */
  @NotNull
  private final Consumer<SimpleScheduledTask> onClose;

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
   * the run.
   */
  private long run = 0L;

  /**
   * ctor.
   *
   * @param onClose the on close.
   * @param plugin the plugin.
   * @param runnable the runnable.
   * @param type the task type.
   * @param interval the step.
   */
  SimpleScheduledTask(@NotNull final Consumer<SimpleScheduledTask> onClose, @NotNull final Plugin plugin,
                      @NotNull final ScheduledRunnable runnable, @NotNull final TaskType type,
                      final long interval) {
    this.onClose = onClose;
    this.plugin = plugin;
    this.runnable = runnable;
    this.taskType = type;
    this.interval = interval;
    if (type.isRepeat()) {
      this.runner = () -> {
        runnable.beforeRun();
        runnable.run();
        runnable.afterRun();
      };
    } else {
      this.runner = () -> {
        runnable.beforeRun();
        runnable.run();
        runnable.afterRun();
        this.cancel();
      };
    }
    if (type.isAsync()) {
      this.executor = SimpleScheduledTask.POOL;
    } else {
      this.executor = SyncTask.getInstance()::add;
    }
  }

  @Override
  public void cancel() {
    this.onClose.accept(this);
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

  /**
   * obtains the real command.
   *
   * @return real command.
   */
  @NotNull
  public Runnable getRunner() {
    return this.runner;
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
