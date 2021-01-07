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

import io.github.shiruka.api.scheduler.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class that represents async scheduled tasks.
 */
public final class AsyncTask implements Task, Runnable {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("AsyncTask");

  /**
   * the task.
   */
  @NotNull
  private final Runnable task;

  /**
   * the complete handlers.
   */
  @Nullable
  private List<Runnable> completeHandlers;

  /**
   * the exception handler.
   */
  @Nullable
  private Predicate<Exception> exceptionHandler;

  /**
   * the future.
   */
  @Nullable
  private Future<?> future;

  /**
   * ctor.
   *
   * @param task the task.
   */
  public AsyncTask(@NotNull final Runnable task) {
    this.task = task;
  }

  @Override
  public void cancel() {
    Optional.ofNullable(this.future)
      .ifPresent(fut -> fut.cancel(true));
  }

  @Override
  public void onComplete(@NotNull final Runnable command) {
    if (this.completeHandlers == null) {
      this.completeHandlers = new ArrayList<>();
    }
    this.completeHandlers.add(command);
  }

  @Override
  public void onException(@NotNull final Predicate<Exception> command) {
    this.exceptionHandler = command;
  }

  /**
   * obtains the task.
   *
   * @return task.
   */
  @NotNull
  public Runnable getTask() {
    return this.task;
  }

  /**
   * checks if the task is done.
   *
   * @return {@code true} if the task is done.
   */
  public boolean isDone() {
    return this.future != null && this.future.isDone();
  }

  @Override
  public void run() {
    try {
      this.task.run();
    } catch (final Exception e) {
      if (this.exceptionHandler == null) {
        AsyncTask.LOGGER.error("No exception handler given!", e);
        return;
      }
      if (this.exceptionHandler.test(e)) {
        return;
      }
      this.fireCompleteHandlers();
      this.cancel();
    }
  }

  /**
   * sets the future of this task.
   *
   * @param future the future to set.
   */
  void setFuture(@NotNull final Future<?> future) {
    this.future = future;
  }

  /**
   * fires the complete handlers.
   */
  private void fireCompleteHandlers() {
    if (this.completeHandlers == null) {
      return;
    }
    this.completeHandlers.forEach(Runnable::run);
    this.completeHandlers = null;
  }
}
