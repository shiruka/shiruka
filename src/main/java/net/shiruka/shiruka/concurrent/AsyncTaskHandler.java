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

package net.shiruka.shiruka.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import net.shiruka.api.text.TranslatedText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that handles asynchronous tasks.
 *
 * @param <T> type of the task.
 */
public abstract class AsyncTaskHandler<T extends Runnable> implements Executor {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * the tasks.
   */
  private final Queue<T> tasks = new ConcurrentLinkedQueue<>();

  /**
   * the thread name.
   */
  @NotNull
  private final String threadName;

  /**
   * the blocking count.
   */
  private int blockingCount;

  /**
   * ctor.
   *
   * @param threadName the thread name.
   */
  protected AsyncTaskHandler(@NotNull final String threadName) {
    this.threadName = threadName;
  }

  /**
   * waits for tasks.
   */
  protected static void waitForTasks() {
    Thread.yield();
    LockSupport.parkNanos("waiting for tasks", 100000L);
  }

  /**
   * adds the given {@code task} to {@link #tasks}.
   *
   * @param task the task to add.
   */
  public final void addTask(@NotNull final T task) {
    this.tasks.add(task);
    LockSupport.unpark(this.getThread());
  }

  /**
   * awaits the tasks.
   *
   * @param supplier the supplier to await.
   */
  public final void awaitTasks(@NotNull final BooleanSupplier supplier) {
    ++this.blockingCount;
    try {
      while (!supplier.getAsBoolean()) {
        if (!this.executeNext()) {
          AsyncTaskHandler.waitForTasks();
        }
      }
    } finally {
      --this.blockingCount;
    }
  }

  /**
   * executes the given {@code runnable}.
   *
   * @param command the command) to execute.
   */
  @Override
  public final void execute(@NotNull final Runnable command) {
    if (this.isNotMainThread()) {
      this.addTask(this.postToMainThread(command));
    } else {
      command.run();
    }
  }

  /**
   * executes all the tasks.
   */
  public final void executeAll() {
    while (this.executeNext()) {
    }
  }

  /**
   * obtains the thread name.
   *
   * @return thread name.
   */
  @NotNull
  public final String getThreadName() {
    return this.threadName;
  }

  /**
   * checks if {@link Thread#currentThread()} is the main thread.
   *
   * @return {@code true} if the current thread is the main thread.
   */
  public final boolean isMainThread() {
    return Thread.currentThread() == this.getThread();
  }

  /**
   * checks if the given {@code task} can execute.
   *
   * @param task the task to check.
   *
   * @return {@code true} if the given task can execute.
   */
  protected abstract boolean canExecute(@NotNull T task);

  protected boolean executeNext() {
    final var task = this.tasks.peek();
    if (task == null) {
      return false;
    }
    if (this.blockingCount == 0 && !this.canExecute(task)) {
      return false;
    }
    this.executeTask(this.tasks.remove());
    return true;
  }

  /**
   * executes the given {@code task}.
   *
   * @param task the task to execute.
   */
  protected void executeTask(@NotNull final T task) {
    try {
      task.run();
    } catch (final Exception exception) {
      if (exception.getCause() instanceof ThreadDeath) {
        throw exception;
      }
      AsyncTaskHandler.LOGGER.fatal(TranslatedText.get("shiruka.server.task_error", this.threadName), exception);
    }
  }

  /**
   * obtains the thread.
   *
   * @return thread.
   */
  @NotNull
  protected abstract Thread getThread();

  /**
   * checks if {@link Thread#currentThread()} is not the main thread.
   *
   * @return {@code true} if the current thread is not the main thread.
   *
   * @see #isMainThread()
   */
  protected boolean isNotMainThread() {
    return !this.isMainThread();
  }

  /**
   * posts the given {@code runnable} to the main thread.
   *
   * @param runnable the runnable to post.
   *
   * @return converts the given {@code runnable} into the {@link T}.
   */
  @NotNull
  protected abstract T postToMainThread(@NotNull Runnable runnable);
}
