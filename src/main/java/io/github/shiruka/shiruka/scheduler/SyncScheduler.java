package io.github.shiruka.shiruka.scheduler;

import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.ScheduledTask;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.Task;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import java.util.Comparator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents sync schedulers.
 */
public final class SyncScheduler implements Scheduler.Async {

  /**
   * the pending tasks.
   */
  private final PriorityQueue<SyncTask> pending = new ObjectArrayPriorityQueue<>(10, Comparator
    .comparingLong(SyncTask::nextRun)
    .thenComparingLong(SyncTask::createdAt));

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

  /**
   * a class that represents sync scheduled tasks.
   */
  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class SyncTask implements ScheduledTask {

    /**
     * the created at.
     */
    private final long createdAt = System.nanoTime();

    /**
     * the task.
     */
    @NotNull
    private final Task task;

    /**
     * the next run.
     */
    private long nextRun;

    @Override
    public void cancel() {
    }

    @NotNull
    @Override
    public Status status() {
      return null;
    }
  }
}
