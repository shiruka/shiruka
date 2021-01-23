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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.*;
import net.shiruka.api.Shiruka;
import net.shiruka.api.events.server.exception.ServerSchedulerException;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

/**
 * a simple async implementation for {@link Scheduler}.
 */
public final class SimpleAsyncScheduler extends SimpleScheduler {

  /**
   * the executor.
   */
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(),
    new ThreadFactoryBuilder().setNameFormat("Simple Scheduler Thread - %1$d").build());

  /**
   * the management.
   */
  private final Executor management = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
    .setNameFormat("Simple Async Scheduler Management Thread").build());

  /**
   * ctor.
   */
  public SimpleAsyncScheduler() {
    super(true);
  }

  @Override
  public void cancelTask(final int taskId) {
    this.management.execute(() -> this.removeTask(taskId));
  }

  @Override
  public void cancelTasks(@NotNull final Plugin plugin) {
    this.parsePending();
    for (final var iterator = this.pending.iterator(); iterator.hasNext(); ) {
      final var task = iterator.next();
      if (task.getTaskId() != -1 && plugin.equals(task.getOwner())) {
        task.cancel0();
        iterator.remove();
      }
    }
  }

  @Override
  public void mainThreadHeartbeat(final int currentTick) {
    this.currentTick = currentTick;
    this.management.execute(() -> this.runTasks(currentTick));
  }

  /**
   * executes the given {@code task}.
   *
   * @param task the task to execute.
   *
   * @return {@code true} if the task was executed successfully.
   */
  private boolean executeTask(@NotNull final ShirukaTask task) {
    if (task.getPeriod() < ShirukaTask.PERIOD_NO_REPEATING) {
      return false;
    }
    this.runners.put(task.getTaskId(), task);
    this.executor.execute(() -> {
      try {
        task.run();
      } catch (final RuntimeException e) {
        Shiruka.getEventManager().serverException(new ServerSchedulerException(e, task)).callEvent();
        throw e;
      } catch (final Throwable t) {
        Shiruka.getEventManager().serverException(new ServerSchedulerException(t, task)).callEvent();
      }
    });
    return true;
  }

  /**
   * removes the given {@code taskId}.
   *
   * @param taskId the task id to remove.
   */
  private synchronized void removeTask(final int taskId) {
    this.parsePending();
    this.pending.removeIf(task -> {
      if (task.getTaskId() != taskId) {
        return false;
      }
      task.cancel0();
      return true;
    });
  }

  /**
   * runs the tasks.
   *
   * @param currentTick the current tick to run.
   */
  private synchronized void runTasks(final int currentTick) {
    this.parsePending();
    while (!this.pending.isEmpty() && this.pending.peek().getNextRun() <= currentTick) {
      final var task = this.pending.remove();
      if (!this.executeTask(task)) {
        this.parsePending();
        continue;
      }
      final var period = task.getPeriod();
      if (period > 0) {
        task.setNextRun(currentTick + period);
        this.temp.add(task);
      }
      this.parsePending();
    }
    this.pending.addAll(this.temp);
    this.temp.clear();
  }
}
