package io.github.shiruka.shiruka.scheduler;

import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.scheduler.Scheduler;
import io.github.shiruka.api.scheduler.Task;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents sync schedulers.
 */
public final class SyncScheduler implements Scheduler.Async {

  @Override
  public void cancelTasks(@NotNull final Plugin.Container plugin) {
  }

  @Override
  public void execute(@NotNull final Task task) {
  }
}
