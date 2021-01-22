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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.scheduler.Task;
import net.shiruka.api.scheduler.TaskWorker;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link Scheduler}.
 */
public final class SimpleScheduler implements ShirukaScheduler {

  /**
   * the id counter.
   */
  private final AtomicInteger idCounter = new AtomicInteger();

  @Override
  public void cancelTask(final int taskId) {
  }

  @Override
  public void cancelTasks(@NotNull final Plugin plugin) {
  }

  @NotNull
  @Override
  public List<TaskWorker> getActiveWorkers() {
    return null;
  }

  @NotNull
  @Override
  public List<Task> getPendingTasks() {
    return null;
  }

  @Override
  public boolean isCurrentlyRunning(final int taskId) {
    return false;
  }

  @Override
  public boolean isQueued(final int taskId) {
    return false;
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return null;
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                       @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                       final long period, @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return null;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                            final long period, @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                            @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @Override
  public void mainThreadHeartbeat(final int currentTick) {
  }

  @Override
  public void parsePending() {
  }
}
