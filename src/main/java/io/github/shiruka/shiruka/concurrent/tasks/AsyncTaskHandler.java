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

package io.github.shiruka.shiruka.concurrent.tasks;

import com.google.common.collect.Queues;
import io.github.shiruka.api.text.TranslatedText;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Mailbox} which handles async tasks.
 *
 * @param <R> type of the runnable.
 */
public abstract class AsyncTaskHandler<R extends Runnable> implements Mailbox<R>, Executor {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("AsyncTaskHandler");

  /**
   * the jobs.
   */
  private final Queue<R> jobs = Queues.newConcurrentLinkedQueue();

  /**
   * the lock.
   */
  private final AtomicInteger lock = new AtomicInteger();

  /**
   * the thread name.
   */
  @NotNull
  private final String threadName;

  /**
   * ctor.
   *
   * @param threadName the thread name.
   */
  protected AsyncTaskHandler(@NotNull final String threadName) {
    this.threadName = threadName;
  }

  /**
   * waits for jobs.
   */
  private static void waitForJobs() {
    Thread.yield();
    LockSupport.parkNanos("waiting for jobs", 100000L);
  }

  @Override
  public final void addJob(@NotNull final R job) {
    this.jobs.add(job);
    LockSupport.unpark(this.getThread());
  }

  @NotNull
  @Override
  public final String getThreadName() {
    return this.threadName;
  }

  /**
   * awaits the jobs.
   *
   * @param supplier the supplier to check if the next job should execute.
   */
  public final void awaitJobs(@NotNull final BooleanSupplier supplier) {
    this.lock.incrementAndGet();
    try {
      while (!supplier.getAsBoolean()) {
        if (!this.executeNext()) {
          AsyncTaskHandler.waitForJobs();
        }
      }
    } finally {
      this.lock.decrementAndGet();
    }
  }

  @Override
  public final void execute(@NotNull final Runnable command) {
    if (this.isNotMainThread()) {
      this.addJob(this.postToMainThread(command));
    } else {
      command.run();
    }
  }

  /**
   * executes the all jobs.
   */
  public final void executeAll() {
    while (this.executeNext()) {
    }
  }

  /**
   * executes the given {@code job} as sync.
   *
   * @param job the job to execute.
   */
  public final void executeSync(@NotNull final Runnable job) {
    if (!this.isMainThread()) {
      this.submitAsync(job).join();
    } else {
      job.run();
    }
  }

  /**
   * checks if the thread is the main thread.
   *
   * @return {@code true} if the thread is the main thread.
   */
  public final boolean isMainThread() {
    return Thread.currentThread() == this.getThread();
  }

  /**
   * schedules the given {@code job}.
   *
   * @param job the job to schedule.
   */
  public final void scheduleOnMain(@NotNull final Runnable job) {
    this.addJob(this.postToMainThread(job));
  }

  /**
   * submits the given {@code job}.
   *
   * @param job the job to submit.
   *
   * @return completed future.
   */
  @NotNull
  public final CompletableFuture<Void> submit(@NotNull final Runnable job) {
    if (this.isNotMainThread()) {
      return this.submitAsync(job);
    } else {
      job.run();
      return CompletableFuture.completedFuture(null);
    }
  }

  /**
   * checks if the job can execute.
   *
   * @param job the job to check.
   *
   * @return {@code true} if the job can execute.
   */
  public abstract boolean canExecute(@NotNull R job);

  public boolean executeNext() {
    final var job = this.jobs.peek();
    if (job == null) {
      return false;
    }
    if (this.lock.get() == 0 &&
      !this.canExecute(job)) {
      return false;
    }
    this.executeTask(this.jobs.remove());
    return true;
  }

  public void executeTask(@NotNull final R job) {
    try {
      job.run();
    } catch (final Exception exception) {
      if (exception.getCause() instanceof ThreadDeath) {
        throw exception;
      }
      AsyncTaskHandler.LOGGER.fatal(TranslatedText.get("shiruka.concurrent.tasks.task_handler.execute_task.error",
        this.threadName), exception);
    }
  }

  /**
   * obtains the thread.
   *
   * @return thread.
   */
  @NotNull
  public abstract Thread getThread();

  /**
   * checks if the thread is not the main thread.
   *
   * @return {@code true} if the thread is not the main thread.
   */
  public boolean isNotMainThread() {
    return !this.isMainThread();
  }

  /**
   * posts the given {@code job} into the main thread to be done.
   *
   * @param job the job to post.
   *
   * @return posted job.
   */
  public abstract R postToMainThread(@NotNull Runnable job);

  /**
   * executes the given {@code job} with completable future.
   *
   * @param job the job to execute.
   *
   * @return completed future.
   */
  @NotNull
  private CompletableFuture<Void> submitAsync(@NotNull final Runnable job) {
    return CompletableFuture.supplyAsync(() -> {
      job.run();
      return null;
    }, this);
  }
}
