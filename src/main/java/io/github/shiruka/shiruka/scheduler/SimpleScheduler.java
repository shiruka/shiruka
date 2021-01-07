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

import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.Task;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link Scheduler}.
 */
public final class SimpleScheduler implements Scheduler {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("SimpleScheduler");

  /**
   * the already alerted.
   */
  private final Set<Thread> alreadyAlerted = new HashSet<>();

  /**
   * the executor service.
   */
  @NotNull
  private final ScheduledExecutorService executorService;

  /**
   * the running threads.
   */
  private final Map<Thread, Runnable> threadRunnableMap = new HashMap<>();

  /**
   * the threads.
   */
  private final Object2LongMap<Thread> threads = new Object2LongOpenHashMap<>();

  /**
   * ctor.
   *
   * @param executorService the executor service.
   */
  public SimpleScheduler(@NotNull final ScheduledExecutorService executorService) {
    this.executorService = executorService;
    final var mxBean = ManagementFactory.getThreadMXBean();
    this.executorService.scheduleWithFixedDelay(() -> {
      final var current = System.currentTimeMillis();
      synchronized (this.threads) {
        final var threadSet = (Object2LongMap.FastEntrySet<Thread>) this.threads.object2LongEntrySet();
        final var threadIterator = threadSet.fastIterator();
        while (threadIterator.hasNext()) {
          final var entry = threadIterator.next();
          final var diff = current - entry.getLongValue();
          if (diff <= TimeUnit.SECONDS.toMillis(10)) {
            continue;
          }
          final var key = entry.getKey();
          var threadInfo = mxBean.getThreadInfo(key.getId());
          final var state = threadInfo.getThreadState();
          if (this.alreadyAlerted.contains(key) ||
            state != Thread.State.WAITING &&
              state != Thread.State.TIMED_WAITING &&
              state != Thread.State.BLOCKED) {
            continue;
          }
          SimpleScheduler.LOGGER.warn("Following runnable is blocking the scheduler loops: {}",
            this.threadRunnableMap.get(key).getClass().getName());
          threadInfo = mxBean.getThreadInfo(key.getId(), Integer.MAX_VALUE);
          Arrays.stream(threadInfo.getStackTrace())
            .forEach(element -> SimpleScheduler.LOGGER.warn("  {}", element));
          this.alreadyAlerted.add(key);
        }
      }
    }, 10, 10, TimeUnit.MILLISECONDS);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable) {
    return this.schedule(runnable, 0, TimeUnit.MILLISECONDS);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable, final long delay, @NotNull final TimeUnit timeUnit) {
    return this.schedule(runnable, delay, -1, timeUnit);
  }

  @NotNull
  @Override
  public Task schedule(@NotNull final Runnable runnable, final long delay, final long period,
                       @NotNull final TimeUnit timeUnit) {
    final var task = new SyncTask(runnable, delay, period, timeUnit);
    SyncTaskManager.addTask(task);
    return task;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable) {
    return this.scheduleAsync(runnable, 0, TimeUnit.MILLISECONDS);
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable, final long delay, final long period,
                            @NotNull final TimeUnit timeUnit) {
    final var task = new AsyncTask(runnable);
    final Future<?> future;
    if (period > 0) {
      future = this.executorService.scheduleAtFixedRate(this.wrapRunnable(task), delay, period, timeUnit);
    } else if (delay > 0) {
      future = this.executorService.schedule(this.wrapRunnable(task), delay, timeUnit);
    } else {
      future = this.executorService.submit(this.wrapRunnable(task));
    }
    task.setFuture(future);
    return task;
  }

  @NotNull
  @Override
  public Task scheduleAsync(@NotNull final Runnable runnable, final long delay, @NotNull final TimeUnit timeUnit) {
    return this.scheduleAsync(runnable, delay, -1, timeUnit);
  }

  /**
   * wraps the given task into a runnable.
   *
   * @param task the task to wrap.
   *
   * @return wrapped runnable.
   */
  @NotNull
  private Runnable wrapRunnable(@NotNull final AsyncTask task) {
    return () -> {
      final var val = System.currentTimeMillis();
      synchronized (this.threads) {
        this.threadRunnableMap.put(Thread.currentThread(), task.getTask());
        this.threads.put(Thread.currentThread(), val);
      }
      task.run();
      synchronized (this.threads) {
        this.threads.remove(Thread.currentThread(), val);
        this.threadRunnableMap.remove(Thread.currentThread(), task.getTask());
        this.alreadyAlerted.remove(Thread.currentThread());
      }
    };
  }
}
