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

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.security.auth.callback.ConfirmationCallback;
import lombok.Getter;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.TaskWorker;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ShirukaTask} which is async.
 */
public final class ShirukaAsyncTask extends ShirukaTask {

  /**
   * the runners.
   */
  @NotNull
  private final IntConsumer removeFunction;

  /**
   * the workers.
   */
  @Getter
  private final LinkedList<TaskWorker> workers = new LinkedList<>();

  /**
   * ctor.
   *
   * @param id the id.
   * @param task the task.
   * @param plugin the plugin.
   * @param delay the delay.
   * @param removeFunction the runners.
   */
  ShirukaAsyncTask(final int id, @NotNull final Consumer<ShirukaTask> task, @NotNull final Plugin plugin,
                   final long delay, @NotNull final IntConsumer removeFunction) {
    super(id, task, plugin, delay);
    this.removeFunction = removeFunction;
  }

  @Override
  public boolean isSync() {
    return false;
  }

  @Override
  public void run() {
    final var thread = Thread.currentThread();
    synchronized (this.workers) {
      if (this.getPeriod() == ConfirmationCallback.CANCEL) {
        return;
      }
      this.workers.add(new TaskWorker() {
        @NotNull
        @Override
        public Plugin getOwner() {
          return Objects.requireNonNull(ShirukaAsyncTask.this.getOwner(), "async task's owner");
        }

        @Override
        public int getTaskId() {
          return ShirukaAsyncTask.this.getTaskId();
        }

        @NotNull
        @Override
        public Thread getThread() {
          return thread;
        }
      });
    }
    Throwable thrown = null;
    try {
      super.run();
    } catch (final Throwable t) {
      thrown = t;
      Objects.requireNonNull(this.getOwner(), "task's owner").getLogger().warn(String.format(
        "Plugin %s generated an exception while executing task %s",
        this.getOwner().getDescription().getFullName(),
        this.getTaskId()),
        thrown);
    } finally {
      synchronized (this.workers) {
        try {
          final var workers = this.workers.iterator();
          var removed = false;
          while (workers.hasNext()) {
            if (workers.next().getThread() == thread) {
              workers.remove();
              removed = true;
              break;
            }
          }
          if (!removed) {
            throw new IllegalStateException(String.format(
              "Unable to remove worker %s on task %s for %s",
              thread.getName(),
              this.getTaskId(),
              Objects.requireNonNull(this.getOwner(), "task's owner")
                .getDescription().getFullName()),
              thrown);
          }
        } finally {
          if (this.getPeriod() < 0 && this.workers.isEmpty()) {
            this.removeFunction.accept(this.getTaskId());
          }
        }
      }
    }
  }

  @Override
  void cancel0() {
    synchronized (this.workers) {
      this.setPeriod(ShirukaTask.PERIOD_CANCEL);
      if (this.workers.isEmpty()) {
        this.removeFunction.accept(this.getTaskId());
      }
    }
  }
}
