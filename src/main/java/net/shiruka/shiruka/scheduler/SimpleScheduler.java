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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.shiruka.api.Shiruka;
import net.shiruka.api.events.server.exception.ServerSchedulerException;
import net.shiruka.api.plugin.IllegalPluginAccessException;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Scheduler;
import net.shiruka.api.scheduler.Task;
import net.shiruka.api.scheduler.TaskWorker;
import net.shiruka.shiruka.ShirukaServer;
import org.cactoos.map.MapEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link Scheduler}.
 */
public class SimpleScheduler implements ShirukaScheduler {

  /**
   * the pending tasks.
   */
  final PriorityQueue<ShirukaTask> pending = new PriorityQueue<>(10, Comparator
    .comparingLong(ShirukaTask::getNextRun)
    .thenComparingInt(ShirukaTask::getTaskId));

  /**
   * the runners.
   */
  final ConcurrentHashMap<Integer, ShirukaTask> runners = new ConcurrentHashMap<>();

  /**
   * the temp.
   */
  final List<ShirukaTask> temp = new ArrayList<>();

  /**
   * the async scheduler.
   */
  @NotNull
  private final SimpleScheduler asyncScheduler;

  /**
   * the id counter.
   */
  private final AtomicInteger idCounter = new AtomicInteger();

  /**
   * the is async scheduler.
   */
  private final boolean isAsyncScheduler;

  /**
   * the current tick.
   */
  volatile int currentTick = -1;

  /**
   * the sync task that is currently running on the main thread.
   */
  @Nullable
  private volatile ShirukaTask currentTask;

  /**
   * the head of the tasks.
   */
  private volatile ShirukaTask head = new ShirukaTask();

  /**
   * tail of a linked-list.
   */
  private final AtomicReference<ShirukaTask> tail = new AtomicReference<>(this.head);

  /**
   * ctor.
   */
  public SimpleScheduler() {
    this(false);
  }

  /**
   * ctor.
   *
   * @param isAsync the is async.
   */
  public SimpleScheduler(final boolean isAsync) {
    this.isAsyncScheduler = isAsync;
    if (isAsync) {
      this.asyncScheduler = this;
    } else {
      this.asyncScheduler = new SimpleAsyncScheduler();
    }
  }

  /**
   * handles the given parameters and gives a map entry instance which has period as key and delay as value.
   *
   * @param plugin the plugin to handle.
   * @param delay the delay to handle.
   * @param period the period to handle.
   *
   * @return a new map entry instance which has period as key and delay as value.
   */
  @NotNull
  private static Map.Entry<Long, Long> handle0(@NotNull final Plugin plugin, final long delay, final long period) {
    if (!plugin.isEnabled()) {
      throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
    }
    final long finalDelay;
    if (delay < 0L) {
      finalDelay = 0;
    } else {
      finalDelay = delay;
    }
    final long finalPeriod;
    if (period == ShirukaTask.PERIOD_ERROR) {
      finalPeriod = 1L;
    } else if (period < ShirukaTask.PERIOD_NO_REPEATING) {
      finalPeriod = ShirukaTask.PERIOD_NO_REPEATING;
    } else {
      finalPeriod = period;
    }
    return new MapEntry<>(finalPeriod, finalDelay);
  }

  @NotNull
  @Override
  public final <T> Future<T> callSyncMethod(@NotNull final Plugin plugin, @NotNull final Callable<T> task) {
    if (!plugin.isEnabled()) {
      throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
    }
    final var future = new ShirukaFuture<>(task, plugin, this.nextId());
    this.handle(future, 0L);
    return future;
  }

  @Override
  public void cancelTask(final int taskId) {
    if (taskId <= 0) {
      return;
    }
    if (!this.isAsyncScheduler) {
      this.asyncScheduler.cancelTask(taskId);
    }
    var task = this.runners.get(taskId);
    if (task != null) {
      task.cancel0();
    }
    task = new ShirukaTask(
      new Consumer<>() {
        @Override
        public void accept(@NotNull final ShirukaTask task) {
          if (!this.check(SimpleScheduler.this.temp)) {
            this.check(SimpleScheduler.this.pending);
          }
        }

        private boolean check(@NotNull final Iterable<ShirukaTask> collection) {
          final var tasks = collection.iterator();
          while (tasks.hasNext()) {
            final var task = tasks.next();
            if (task.getTaskId() == taskId) {
              task.cancel0();
              tasks.remove();
              if (task.isSync()) {
                SimpleScheduler.this.runners.remove(taskId);
              }
              return true;
            }
          }
          return false;
        }
      });
    this.handle(task, 0L);
    for (var taskPending = this.head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
      if (taskPending == task) {
        return;
      }
      if (taskPending.getTaskId() == taskId) {
        taskPending.cancel0();
      }
    }
  }

  @Override
  public void cancelTasks(@NotNull final Plugin plugin) {
    if (!this.isAsyncScheduler) {
      this.asyncScheduler.cancelTasks(plugin);
    }
    final var task = new ShirukaTask(
      new Consumer<ShirukaTask>() {
        @Override
        public void accept(@NotNull final ShirukaTask task) {
          this.check(SimpleScheduler.this.pending);
          this.check(SimpleScheduler.this.temp);
        }

        void check(@NotNull final Iterable<ShirukaTask> collection) {
          final var tasks = collection.iterator();
          while (tasks.hasNext()) {
            final var task = tasks.next();
            if (plugin.equals(task.getOwner())) {
              task.cancel0();
              tasks.remove();
              if (task.isSync()) {
                SimpleScheduler.this.runners.remove(task.getTaskId());
              }
            }
          }
        }
      }) {{
    }};
    this.handle(task, 0L);
    for (var taskPending = this.head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
      if (taskPending == task) {
        break;
      }
      if (taskPending.getTaskId() != -1 && plugin.equals(taskPending.getOwner())) {
        taskPending.cancel0();
      }
    }
    this.runners.values().stream()
      .filter(runner -> plugin.equals(runner.getOwner()))
      .forEach(ShirukaTask::cancel0);
  }

  @NotNull
  @Override
  public final List<TaskWorker> getActiveWorkers() {
    if (!this.isAsyncScheduler) {
      return this.asyncScheduler.getActiveWorkers();
    }
    final var workers = new ArrayList<TaskWorker>();
    this.runners.values().stream()
      .filter(taskObj -> !taskObj.isSync())
      .map(taskObj -> (ShirukaAsyncTask) taskObj)
      .forEach(task -> {
        synchronized (task.getWorkers()) {
          workers.addAll(task.getWorkers());
        }
      });
    return workers;
  }

  @NotNull
  @Override
  public final List<Task> getPendingTasks() {
    final var truePending = Stream.iterate(this.head.getNext(), Objects::nonNull, ShirukaTask::getNext)
      .filter(task -> task.getTaskId() != -1)
      .collect(Collectors.toCollection(ArrayList::new));
    final List<Task> pending = this.runners.values().stream()
      .filter(task -> task.getPeriod() >= ShirukaTask.PERIOD_NO_REPEATING)
      .collect(Collectors.toCollection(ArrayList::new));
    truePending.stream()
      .filter(task -> task.getPeriod() >= ShirukaTask.PERIOD_NO_REPEATING && !pending.contains(task))
      .forEach(pending::add);
    if (!this.isAsyncScheduler) {
      pending.addAll(this.asyncScheduler.getPendingTasks());
    }
    return pending;
  }

  @Override
  public final boolean isCurrentlyRunning(final int taskId) {
    if (!this.isAsyncScheduler && this.asyncScheduler.isCurrentlyRunning(taskId)) {
      return true;
    }
    final var task = this.runners.get(taskId);
    if (task == null) {
      return false;
    }
    if (task.isSync()) {
      return task == this.currentTask;
    }
    final var asyncTask = (ShirukaAsyncTask) task;
    synchronized (asyncTask.getWorkers()) {
      return !asyncTask.getWorkers().isEmpty();
    }
  }

  @Override
  public final boolean isQueued(final int taskId) {
    if (taskId <= 0) {
      return false;
    }
    if (!this.isAsyncScheduler && this.asyncScheduler.isQueued(taskId)) {
      return true;
    }
    for (var task = this.head.getNext(); task != null; task = task.getNext()) {
      if (task.getTaskId() == taskId) {
        return task.getPeriod() >= ShirukaTask.PERIOD_NO_REPEATING;
      }
    }
    final var task = this.runners.get(taskId);
    return task != null && task.getPeriod() >= ShirukaTask.PERIOD_NO_REPEATING;
  }

  @NotNull
  @Override
  public final Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return this.schedule(plugin, job, 0L);
  }

  @NotNull
  @Override
  public final Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay) {
    return this.schedule(plugin, job, delay, ShirukaTask.PERIOD_NO_REPEATING);
  }

  @NotNull
  @Override
  public final Task schedule(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                             final long period) {
    final var entry = SimpleScheduler.handle0(plugin, delay, period);
    final var task = new ShirukaTask(this.nextId(), job::accept, plugin, entry.getKey());
    return this.handle(task, entry.getValue());
  }

  @NotNull
  @Override
  public final Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job) {
    return this.scheduleAsync(plugin, job, 0L);
  }

  @NotNull
  @Override
  public final Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay) {
    return this.scheduleAsync(plugin, job, delay, ShirukaTask.PERIOD_NO_REPEATING);
  }

  @NotNull
  @Override
  public final Task scheduleAsync(@NotNull final Plugin plugin, @NotNull final Consumer<Task> job, final long delay,
                                  final long period) {
    final var entry = SimpleScheduler.handle0(plugin, delay, period);
    final var task = new ShirukaAsyncTask(this.nextId(), job::accept, plugin, entry.getKey(),
      this.asyncScheduler.runners);
    return this.handle(task, entry.getValue());
  }

  @Override
  public void mainThreadHeartbeat(final int currentTick) {
    if (!this.isAsyncScheduler) {
      this.asyncScheduler.mainThreadHeartbeat(currentTick);
    }
    this.currentTick = currentTick;
    final var temp = this.temp;
    this.parsePending();
    while (this.isReady(currentTick)) {
      final var task = this.pending.remove();
      if (task.getPeriod() < ShirukaTask.PERIOD_NO_REPEATING) {
        if (task.isSync()) {
          this.runners.remove(task.getTaskId(), task);
        }
        this.parsePending();
        continue;
      }
      if (task.isSync()) {
        this.currentTask = task;
        try {
          task.run();
        } catch (final Throwable throwable) {
          final var msg = String.format(
            "Task #%s for %s generated an exception",
            task.getTaskId(),
            Objects.requireNonNull(task.getOwner(), "task's owner")
              .getDescription().getFullName());
          if (task.getOwner() == ShirukaServer.INTERNAL_PLUGIN) {
            ShirukaServer.LOGGER.error(msg, throwable);
          } else {
            task.getOwner().getLogger().warn(msg, throwable);
          }
          Shiruka.getEventManager().serverException(new ServerSchedulerException(msg, throwable, task))
            .callEvent();
        } finally {
          this.currentTask = null;
        }
        this.parsePending();
      } else {
        Objects.requireNonNull(task.getOwner(), "task's owner")
          .getLogger().fatal("Unexpected Async Task in the Sync Scheduler. Report this to Paper");
      }
      final var period = task.getPeriod();
      if (period > 0) {
        task.setNextRun(currentTick + period);
        temp.add(task);
      } else if (task.isSync()) {
        this.runners.remove(task.getTaskId());
      }
    }
    this.pending.addAll(temp);
    temp.clear();
  }

  @Override
  public final void parsePending() {
    if (!this.isAsyncScheduler) {
    }
    var head = this.head;
    var task = head.getNext();
    var lastTask = head;
    for (; task != null; task = (lastTask = task).getNext()) {
      if (task.getTaskId() == -1) {
        task.run();
        continue;
      }
      if (task.getPeriod() < ShirukaTask.PERIOD_NO_REPEATING) {
        continue;
      }
      this.pending.add(task);
      this.runners.put(task.getTaskId(), task);
    }
    for (task = head; task != lastTask; task = head) {
      head = Objects.requireNonNull(task, "task").getNext();
      task.setNext(null);
    }
    this.head = lastTask;
    if (!this.isAsyncScheduler) {
    }
  }

  protected final void addTask(@NotNull final ShirukaTask task) {
    final var tail = this.tail;
    var tailTask = tail.get();
    while (!tail.compareAndSet(tailTask, task)) {
      tailTask = tail.get();
    }
    tailTask.setNext(task);
  }

  @NotNull
  protected final ShirukaTask handle(@NotNull final ShirukaTask task, final long delay) {
    if (!this.isAsyncScheduler && !task.isSync()) {
      this.asyncScheduler.handle(task, delay);
      return task;
    }
    task.setNextRun(this.currentTick + delay);
    this.addTask(task);
    return task;
  }

  /**
   * checks if its ready to poll from {@link #pending}.
   *
   * @param currentTick the current tick to check.
   *
   * @return {@code true} if {@link #pending} is ready to poll.
   */
  private boolean isReady(final int currentTick) {
    return !this.pending.isEmpty() && this.pending.peek().getNextRun() <= currentTick;
  }

  /**
   * obtains the next task is.
   *
   * @return next task id.
   */
  private int nextId() {
    return this.idCounter.incrementAndGet();
  }
}
