package io.github.shiruka.shiruka.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.event.events.server.ServerExceptionEvent;
import io.github.shiruka.api.exception.ServerSchedulerException;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents async schedulers.
 */
public final class AsyncScheduler extends SyncScheduler {

  /**
   * the executor.
   */
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4, Integer.MAX_VALUE, 30L, TimeUnit.SECONDS, new SynchronousQueue<>(),
    new ThreadFactoryBuilder().setNameFormat("Shiru ka Scheduler Thread - %1$d").build());

  /**
   * the management.
   */
  private final Executor management = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
    .setNameFormat("Shiru ka Async Scheduler Management Thread").build());

  /**
   * the temp.
   */
  private final List<SyncScheduler.SyncTask> temp = new ArrayList<>();

  /**
   * ctor.
   */
  public AsyncScheduler() {
    this.executor.allowCoreThreadTimeOut(true);
    this.executor.prestartAllCoreThreads();
  }

  @Override
  public void cancelTask(final int taskId) {
    this.management.execute(() -> this.removeTask(taskId));
  }

  @Override
  public void cancelTasks(@NotNull final Plugin.Container plugin) {
    this.parsePending();
    final var iterator = this.pending.iterator();
    while (iterator.hasNext()) {
      final var task = iterator.next();
      if (task.id() != -1 && task.task().plugin().equals(plugin)) {
        task.cancel0();
        iterator.remove();
      }
    }
  }

  @Override
  public void heartbeat(final int currentTick) {
    this.currentTick = currentTick;
    this.management.execute(() -> this.runTasks(currentTick));
  }

  /**
   * executes the given task.
   *
   * @param task the task to execute.
   *
   * @return {@code true} if the task executed successfully.
   */
  private boolean executeTask(@NotNull final SyncScheduler.SyncTask task) {
    if (task.period() < SyncScheduler.SyncTask.NO_REPEATING) {
      return false;
    }
    this.runners.put(task.id(), task);
    this.executor.execute(() -> {
      try {
        task.run();
      } catch (final RuntimeException e) {
        Shiruka.eventManager().call(new ServerExceptionEvent(new ServerSchedulerException(e, task)));
        throw e;
      } catch (final Throwable t) {
        Shiruka.eventManager().call(new ServerExceptionEvent(new ServerSchedulerException(t, task)));
      }
    });
    return true;
  }

  /**
   * removes the task.
   *
   * @param taskId the task id to remove.
   */
  private synchronized void removeTask(final int taskId) {
    this.parsePending();
    this.pending.removeIf(task -> {
      if (task.id() == taskId) {
        task.cancel0();
        return true;
      }
      return false;
    });
  }

  /**
   * the runs the pending tasks.
   *
   * @param currentTick the current tick to run.
   */
  private synchronized void runTasks(final int currentTick) {
    this.parsePending();
    while (!this.pending.isEmpty() && this.pending.peek().nextRun() <= currentTick) {
      final var task = this.pending.remove();
      if (this.executeTask(task)) {
        final var period = task.period();
        if (period > 0) {
          task.nextRun(currentTick + period);
          this.temp.add(task);
        }
      }
      this.parsePending();
    }
    this.pending.addAll(this.temp);
    this.temp.clear();
  }

  /**
   * a class that represents async scheduled tasks.
   */
  @Accessors(fluent = true)
  public static final class AsyncTask extends SyncScheduler.SyncTask {

    /**
     * the runners.
     */
    @NotNull
    private final Map<Integer, SyncScheduler.SyncTask> runners;

    /**
     * the workers.
     */
    private final LinkedList<Task.Worker> workers = new LinkedList<>();

    /**
     * ctor.
     *
     * @param task the tas.
     * @param id the id.
     * @param runners the runners.
     */
    public AsyncTask(@NotNull final Task task, final int id,
                     @NotNull final Map<Integer, SyncScheduler.SyncTask> runners) {
      super(task, id);
      this.runners = runners;
    }

    @Override
    public void run() {
      final var thread = Thread.currentThread();
      final var nameBefore = thread.getName();
      final var plugin = this.task().plugin();
      final var description = plugin.description();
      thread.setName(nameBefore + " - " + description.name());
      try {
        synchronized (this.workers) {
          if (this.period() == SyncScheduler.SyncTask.CANCEL) {
            return;
          }
          this.workers.add(new Task.Worker(this.id(), plugin, thread));
        }
        Throwable thrown = null;
        final var fullName = description.fullName();
        try {
          super.run();
        } catch (final Throwable t) {
          thrown = t;
          plugin.logger().warn(
            "Plugin %s generated an exception while executing task %s"
              .formatted(fullName, this.id()),
            thrown);
        } finally {
          synchronized (this.workers) {
            try {
              final var workers = this.workers.iterator();
              var removed = false;
              while (workers.hasNext()) {
                if (workers.next().thread() == thread) {
                  workers.remove();
                  removed = true;
                  break;
                }
              }
              if (!removed) {
                throw new IllegalStateException(
                  String.format(
                    "Unable to remove worker %s on task %s for %s",
                    thread.getName(),
                    this.id(),
                    fullName),
                  thrown);
              }
            } finally {
              if (this.period() < 0 && this.workers.isEmpty()) {
                this.runners.remove(this.id());
              }
            }
          }
        }
      } finally {
        thread.setName(nameBefore);
      }
    }

    @Override
    protected void cancel0() {
      synchronized (this.workers) {
        super.cancel0();
        if (this.workers.isEmpty()) {
          this.runners.remove(this.id());
        }
      }
    }
  }
}
