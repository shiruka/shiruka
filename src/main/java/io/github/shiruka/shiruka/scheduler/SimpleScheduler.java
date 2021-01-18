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
import io.github.shiruka.api.scheduler.TaskWorker;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public final class SimpleScheduler implements Scheduler {

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
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Runnable runnable) {
    return null;
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Runnable runnable, final long delay,
                       @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Plugin plugin, @NotNull final Runnable runnable, final long delay,
                       final long period, @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Runnable runnable) {
    return null;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Runnable runnable, final long delay,
                            final long period, @NotNull final TimeUnit timeUnit) {
    return null;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Runnable runnable, final long delay,
                            @NotNull final TimeUnit timeUnit) {
    return null;
  }
}
