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

package io.github.shiruka.shiruka.scheduler;

import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.Task;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents logged schedulers.
 */
public final class LoggedScheduler implements Scheduler {

  /**
   * the task thrown message.
   */
  private static final String A_TASK_THROWN_A_EXCEPTION = "A task thrown a Exception!";

  /**
   * the closed exception message.
   */
  private static final String CLOSED_EXCEPTION = "This LoggedScheduler has been cleaned and closed. No new Tasks can be scheduled!";

  /**
   * the delegate.
   */
  @Nullable
  private final Scheduler delegate;

  /**
   * the logger.
   */
  @NotNull
  private final Logger logger;

  /**
   * the running tasks.
   */
  private final Set<Task> runningTasks = Collections.synchronizedSet(new HashSet<>());

  /**
   * ctor.
   *
   * @param logger the logger.
   * @param delegate the delegate.
   */
  public LoggedScheduler(@NotNull final Logger logger, @Nullable final Scheduler delegate) {
    this.logger = logger;
    this.delegate = delegate;
  }

  /**
   * ctor.
   *
   * @param plugin the plugin.
   * @param delegate the delegate.
   */
  public LoggedScheduler(@NotNull final Plugin plugin, @Nullable final Scheduler delegate) {
    this(plugin.getLogger(), delegate);
  }

  /**
   * cleans up.
   */
  public void cleanup() {
    new ArrayList<>(this.runningTasks).forEach(Task::cancel);
    this.runningTasks.clear();
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable) {
    return this.prepare(this.getDelegate().schedule(runnable));
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable, final long delay, @NotNull final TimeUnit timeUnit) {
    return this.prepare(this.getDelegate().schedule(runnable, delay, timeUnit));
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable, final long delay, final long period,
                       @NotNull final TimeUnit timeUnit) {
    return this.prepare(this.getDelegate().schedule(runnable, delay, period, timeUnit));
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable) {
    return this.prepare(this.getDelegate().scheduleAsync(runnable));
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable, final long delay, final long period,
                            @NotNull final TimeUnit timeUnit) {
    return this.prepare(this.getDelegate().scheduleAsync(runnable, delay, period, timeUnit));
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable, final long delay, @NotNull final TimeUnit timeUnit) {
    return this.prepare(this.getDelegate().scheduleAsync(runnable, delay, timeUnit));
  }

  /**
   * obtains the delegate.
   *
   * @return delegate.
   *
   * @throws NullPointerException if the scheduler is closed.
   */
  @NotNull
  private Scheduler getDelegate() {
    return Objects.requireNonNull(this.delegate, LoggedScheduler.CLOSED_EXCEPTION);
  }

  /**
   * prepares the given task.
   *
   * @param task the task to prepare.
   *
   * @return the task itself.
   */
  @NotNull
  private Task prepare(@NotNull final Task task) {
    task.onException(e -> {
      this.logger.warn(LoggedScheduler.A_TASK_THROWN_A_EXCEPTION, e);
      return true;
    });
    task.onComplete(() -> this.runningTasks.remove(task));
    this.runningTasks.add(task);
    return task;
  }
}
