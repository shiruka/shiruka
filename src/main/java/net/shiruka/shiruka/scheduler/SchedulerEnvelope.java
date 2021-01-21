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

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.scheduler.Task;
import net.shiruka.api.scheduler.TaskWorker;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Scheduler}.
 */
public abstract class SchedulerEnvelope implements ShirukaScheduler {

  /**
   * the pending jobs.
   */
  final PriorityQueue<ShirukaTask> pending = new PriorityQueue<>(10, Comparator
    .comparingLong(ShirukaTask::getNextRun)
    .thenComparingInt(ShirukaTask::getTaskId));

  /**
   * the runners.
   */
  final ConcurrentHashMap<Integer, ShirukaTask> runners = new ConcurrentHashMap<>();

  /**
   * the delegate.
   */
  @NotNull
  private final ShirukaScheduler delegate;

  /**
   * the current tick.
   */
  volatile int currentTick = -1;

  /**
   * ctor.
   *
   * @param delegate the delegate.
   */
  protected SchedulerEnvelope(@NotNull final ShirukaScheduler delegate) {
    this.delegate = delegate;
  }

  @Override
  public void cancelTask(final int taskId) {
    this.delegate.cancelTask(taskId);
  }

  @Override
  public void cancelTasks(@NotNull final Plugin plugin) {
    this.delegate.cancelTasks(plugin);
  }

  @NotNull
  @Override
  public List<TaskWorker> getActiveWorkers() {
    return this.delegate.getActiveWorkers();
  }

  @NotNull
  @Override
  public List<Task> getPendingTasks() {
    return this.delegate.getPendingTasks();
  }

  @Override
  public boolean isCurrentlyRunning(final int taskId) {
    return this.delegate.isCurrentlyRunning(taskId);
  }

  @Override
  public boolean isQueued(final int taskId) {
    return this.isCurrentlyRunning(taskId);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return this.delegate.schedule(plugin, job);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                       @NotNull final TimeUnit timeUnit) {
    return this.delegate.schedule(plugin, job, delay, timeUnit);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                       final long period, @NotNull final TimeUnit timeUnit) {
    return this.delegate.schedule(plugin, job, delay, period, timeUnit);
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return this.delegate.scheduleAsync(plugin, job);
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                            final long period, @NotNull final TimeUnit timeUnit) {
    return this.delegate.scheduleAsync(plugin, job, delay, period, timeUnit);
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                            @NotNull final TimeUnit timeUnit) {
    return this.delegate.scheduleAsync(plugin, job, delay, timeUnit);
  }

  @Override
  public void mainThreadHeartbeat(final int currentTick) {
    this.delegate.mainThreadHeartbeat(currentTick);
  }
}
