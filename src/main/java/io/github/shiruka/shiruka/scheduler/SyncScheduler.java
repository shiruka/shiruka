package io.github.shiruka.shiruka.scheduler;

import com.google.common.base.Preconditions;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.event.events.server.ServerExceptionEvent;
import io.github.shiruka.api.exception.ServerSchedulerException;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.ScheduledTask;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.Task;
import io.github.shiruka.shiruka.server.ShirukaServer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents sync schedulers.
 */
public final class SyncScheduler implements Scheduler.Async {

  /**
   * the start id.
   */
  private static final int START_ID = 1;

  /**
   * the increment id function.
   */
  private static final IntUnaryOperator INCREMENT_IDS = previous -> {
    if (previous == Integer.MAX_VALUE) {
      return SyncScheduler.START_ID;
    }
    return previous + 1;
  };

  /**
   * the ids.
   */
  private final AtomicInteger ids = new AtomicInteger(SyncScheduler.START_ID);

  /**
   * the pending tasks.
   */
  private final PriorityQueue<SyncTask> pending = new PriorityQueue<>(10, Comparator
    .<SyncTask>comparingLong(SyncTask::nextRun)
    .thenComparingLong(SyncTask::createdAt));

  /**
   * the running tasks.
   */
  private final ConcurrentHashMap<Integer, ScheduledTask> runners = new ConcurrentHashMap<>();

  /**
   * the temporary task list.
   */
  private final List<SyncTask> temp = new ArrayList<>();

  /**
   * the currently running task.
   */
  private volatile SyncTask currentTask = null;

  /**
   * the current tick.
   */
  private volatile int currentTick = -1;

  /**
   * the head task.
   */
  private SyncTask head = new SyncTask(Task.syncBuilder()
    .withPlugin(ShirukaServer.INTERNAL_PLUGIN)
    .withJob(scheduledTask -> {
    })
    .withName("Head")
    .build());

  @Override
  public void cancelTasks(@NotNull final Plugin.Container plugin) {
  }

  @Override
  public void cancelTasks(final long taskId) {
  }

  @NotNull
  @Override
  public ScheduledTask execute(@NotNull final Task task) {
    return new SyncTask(task);
  }

  @Override
  public void heartbeat(final int currentTick) {
    this.currentTick = currentTick;
    while (this.isReady(currentTick)) {
      final var remove = this.pending.remove();
      final var task = remove.task();
      if (task.interval() < SyncTask.NO_REPEATING) {
        if (task.isSync()) {
          this.runners.remove(remove.id, remove);
        }
        this.parsePending();
        continue;
      }
      final var plugin = task.plugin();
      if (task.isSync()) {
        this.currentTask = remove;
        try {
          remove.run();
        } catch (final Throwable throwable) {
          final var name = plugin.description().fullName();
          final var msg = String.format(
            "Task #%s for %s generated an exception",
            remove.id(),
            name);
          plugin.logger().warn(msg, throwable);
          Shiruka.eventManager().call(new ServerExceptionEvent(new ServerSchedulerException(msg, throwable, remove)));
        } finally {
          this.currentTask = null;
        }
        this.parsePending();
      } else {
        plugin.logger().fatal("Unexpected Async Task in the Sync Scheduler. Report this to Shiru ka");
      }
      final var period = task.interval();
      if (period > 0L) {
        remove.nextRun(currentTick + period);
        this.temp.add(remove);
      } else if (task.isSync()) {
        this.runners.remove(remove.id());
      }
    }
    this.pending.addAll(this.temp);
    this.temp.clear();
  }

  /**
   * checks if the current tick is ready to run a task.
   *
   * @param currentTick the current tick to check.
   *
   * @return {@code true} if the current tick is ready to run a task.
   */
  private boolean isReady(final int currentTick) {
    return !this.pending.isEmpty() && this.pending.peek().nextRun() <= currentTick;
  }

  /**
   * calculates the next id.
   *
   * @return next id.
   */
  private int nextId() {
    Preconditions.checkState(this.runners.size() < Integer.MAX_VALUE,
      "There are already %d tasks scheduled! Cannot schedule more.".formatted(Integer.MAX_VALUE));
    int id;
    do {
      id = this.ids.updateAndGet(SyncScheduler.INCREMENT_IDS);
    }
    while (this.runners.containsKey(id));
    return id;
  }

  /**
   * parses the pending tasks.
   */
  private void parsePending() {
    var head = this.head;
    var task = head.next();
    var lastTask = head;
    for (; task != null; task = (lastTask = task).next()) {
      if (task.id() == -1) {
        task.run();
      } else if (task.task().interval() >= SyncTask.NO_REPEATING) {
        this.pending.add(task);
        this.runners.put(task.id(), task);
      }
    }
    for (task = head; task != lastTask; task = head) {
      head = task.next();
      task.next(null);
    }
    this.head = lastTask;
  }

  /**
   * a class that represents sync scheduled tasks.
   */
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class SyncTask implements ScheduledTask, Runnable {

    /**
     * the no repeating.
     */
    public static final int NO_REPEATING = -1;

    /**
     * the created at.
     */
    @Getter
    private final long createdAt = System.nanoTime();

    /**
     * the task.
     */
    @Getter
    @NotNull
    private final Task task;

    /**
     * the id.
     */
    @Getter
    private int id;

    /**
     * the next.
     */
    @Setter
    @Getter
    private volatile SyncTask next;

    /**
     * the next run.
     */
    @Setter
    @Getter
    private long nextRun;

    @Override
    public void cancel() {
    }

    @Override
    public void run() {
      this.task().job().accept(this);
    }
  }
}
