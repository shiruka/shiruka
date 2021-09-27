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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents sync schedulers.
 */
public class SyncScheduler implements Scheduler.Async {

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
   * the pending tasks.
   */
  protected final PriorityQueue<SyncTask> pending = new PriorityQueue<>(10, Comparator
    .<SyncTask>comparingLong(SyncTask::nextRun)
    .thenComparingLong(SyncTask::createdAt));

  /**
   * the running tasks.
   */
  protected final ConcurrentHashMap<Integer, SyncTask> runners = new ConcurrentHashMap<>();

  /**
   * the ids.
   */
  private final AtomicInteger ids = new AtomicInteger(SyncScheduler.START_ID);

  /**
   * the temporary task list.
   */
  private final List<SyncTask> temp = new ArrayList<>();

  /**
   * the current tick.
   */
  protected volatile int currentTick = -1;

  /**
   * the currently running task.
   */
  @Nullable
  private volatile SyncTask currentTask = null;

  /**
   * the head task.
   */
  @NotNull
  private SyncTask head = new SyncTask(this.newBuilder()
    .withPlugin(ShirukaServer.getInternalPlugin())
    .withJob(scheduledTask -> {
    })
    .withName("Head")
    .build());

  /**
   * the tail.
   */
  private final AtomicReference<SyncTask> tail = new AtomicReference<>(this.head);

  @Override
  public void cancelTask(final int taskId) {
    if (taskId <= 0) {
      return;
    }
    var task = this.runners.get(taskId);
    if (task != null) {
      task.cancel0();
    }
    task = new SyncTask(Task.syncBuilder()
      .withPlugin(ShirukaServer.getInternalPlugin())
      .withJob(new Consumer<>() {
        @Override
        public void accept(final ScheduledTask scheduledTask) {
          if (!this.check(SyncScheduler.this.temp)) {
            this.check(SyncScheduler.this.pending);
          }
        }

        private boolean check(@NotNull final Iterable<SyncTask> collection) {
          final var tasks = collection.iterator();
          while (tasks.hasNext()) {
            final var task = tasks.next();
            if (task.id() == taskId) {
              task.cancel0();
              tasks.remove();
              if (task.task().isSync()) {
                SyncScheduler.this.runners.remove(taskId);
              }
              return true;
            }
          }
          return false;
        }
      })
      .withName("Head")
      .build());
    this.handle(task, 0L);
    for (var taskPending = this.head.next(); taskPending != null; taskPending = taskPending.next()) {
      if (taskPending == task) {
        return;
      }
      if (taskPending.id() == taskId) {
        taskPending.cancel0();
      }
    }
  }

  @Override
  public void cancelTasks(@NotNull final Plugin.Container plugin) {
    final var task = new SyncTask(Task.syncBuilder()
      .withPlugin(ShirukaServer.getInternalPlugin())
      .withJob(new Consumer<>() {
        @Override
        public void accept(final ScheduledTask scheduledTask) {
          this.check(SyncScheduler.this.pending);
          this.check(SyncScheduler.this.temp);
        }

        private void check(@NotNull final Iterable<SyncTask> collection) {
          final var tasks = collection.iterator();
          while (tasks.hasNext()) {
            final var task = tasks.next();
            if (task.task().plugin().equals(plugin)) {
              task.cancel0();
              tasks.remove();
              if (task.task().isSync()) {
                SyncScheduler.this.runners.remove(task.id());
              }
            }
          }
        }
      })
      .withName("Head")
      .build());
    this.handle(task, 0L);
    for (var taskPending = this.head.next(); taskPending != null; taskPending = taskPending.next()) {
      if (taskPending == task) {
        break;
      }
      if (taskPending.id() != -1 && taskPending.task().plugin().equals(plugin)) {
        taskPending.cancel0();
      }
    }
    for (final var runner : this.runners.values()) {
      if (runner.task().plugin().equals(plugin)) {
        runner.cancel0();
      }
    }
  }

  @NotNull
  @Override
  public final ScheduledTask execute(@NotNull final Task task) {
    Preconditions.checkState(task.plugin().enabled(), "Plugin attempted to register task while disabled!");
    final var interval = task.interval();
    final long period;
    if (interval == SyncTask.ERROR) {
      period = interval;
    } else if (interval < SyncTask.NO_REPEATING) {
      period = SyncTask.NO_REPEATING;
    } else {
      period = interval;
    }
    return this.handle(new SyncTask(task, this.nextId(), period), task.delay());
  }

  @Override
  public void heartbeat(final int currentTick) {
    this.currentTick = currentTick;
    while (this.isReady(currentTick)) {
      final var remove = this.pending.remove();
      final var task = remove.task();
      if (remove.period() < SyncTask.NO_REPEATING) {
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
      final var period = remove.period();
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
   * adds the given task to the tail.
   *
   * @param task the task to add.
   */
  protected final void addTask(@NotNull final SyncTask task) {
    var tailTask = this.tail.get();
    while (!this.tail.compareAndSet(tailTask, task)) {
      tailTask = this.tail.get();
    }
    tailTask.next(task);
  }

  /**
   * handles the given task with a delay.
   *
   * @param task the task the handle.
   * @param delay the delay to handle.
   *
   * @return handled task.
   */
  @NotNull
  protected final SyncTask handle(@NotNull final SyncTask task, final long delay) {
    task.nextRun(this.currentTick + Math.max(delay, 0L));
    this.addTask(task);
    return task;
  }

  /**
   * parses the pending tasks.
   */
  protected final void parsePending() {
    var head = this.head;
    var task = head.next();
    var lastTask = head;
    for (; task != null; task = (lastTask = task).next()) {
      if (task.id() == -1) {
        task.run();
      } else if (task.period() >= SyncTask.NO_REPEATING) {
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
   * a class that represents sync scheduled tasks.
   */
  @Accessors(fluent = true)
  public static class SyncTask implements ScheduledTask, Runnable {

    /**
     * the cancel.
     */
    public static final int CANCEL = -2;

    /**
     * the done for future.
     */
    public static final int DONE_FOR_FUTURE = -4;

    /**
     * the error.
     */
    public static final int ERROR = 0;

    /**
     * the no repeating.
     */
    public static final int NO_REPEATING = -1;

    /**
     * the process for future.
     */
    public static final int PROCESS_FOR_FUTURE = -3;

    /**
     * the created at.
     */
    @Getter
    private final long createdAt = System.nanoTime();

    /**
     * the id.
     */
    @Getter
    private final int id;

    /**
     * the task.
     */
    @Getter
    @NotNull
    private final Task task;

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

    /**
     * the period.
     */
    @Getter
    private long period;

    /**
     * ctor.
     *
     * @param task the task.
     * @param id the id.
     * @param period the period.
     */
    protected SyncTask(@NotNull final Task task, final int id, final long period) {
      this.task = task;
      this.period = period;
      this.id = id;
    }

    /**
     * ctor.
     *
     * @param task the task.
     * @param id the id.
     */
    protected SyncTask(@NotNull final Task task, final int id) {
      this(task, id, task.interval());
    }

    /**
     * ctor.
     *
     * @param task the task.
     */
    private SyncTask(@NotNull final Task task) {
      this(task, SyncTask.NO_REPEATING);
    }

    @Override
    public final void cancel() {
      Shiruka.syncScheduler().cancelTask(this.id);
    }

    @Override
    public void run() {
      this.task().job().accept(this);
    }

    /**
     * cancels the task.
     */
    protected void cancel0() {
      this.period = SyncTask.CANCEL;
    }
  }
}
