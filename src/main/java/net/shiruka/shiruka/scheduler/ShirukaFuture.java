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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.scheduler.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Task} which is future.
 *
 * @param <T> type of the future object.
 */
public final class ShirukaFuture<T> extends ShirukaTask implements Future<T> {

  /**
   * the callable.
   */
  @NotNull
  private final Callable<T> callable;

  /**
   * the exception.
   */
  @Nullable
  private Exception exception = null;

  /**
   * the value.
   */
  @Nullable
  private T value;

  /**
   * ctor.
   *
   * @param callable the callable.
   * @param plugin the plugin.
   * @param id the id.
   */
  ShirukaFuture(@NotNull final Callable<T> callable, @NotNull final Plugin plugin, final int id) {
    super(id, null, plugin, ShirukaTask.PERIOD_NO_REPEATING);
    this.callable = callable;
  }

  @Override
  public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
    if (this.getPeriod() != ShirukaTask.PERIOD_NO_REPEATING) {
      return false;
    }
    this.setPeriod(ShirukaTask.PERIOD_CANCEL);
    return true;
  }

  @Override
  public boolean isDone() {
    final var period = this.getPeriod();
    return period != ShirukaTask.PERIOD_NO_REPEATING && period != ShirukaTask.PERIOD_PROCESS_FOR_FUTURE;
  }

  @Override
  public T get() throws CancellationException, InterruptedException, ExecutionException {
    try {
      return this.get(0, TimeUnit.MILLISECONDS);
    } catch (final TimeoutException e) {
      throw new Error(e);
    }
  }

  @Override
  public synchronized T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
    TimeoutException {
    var tempTimeout = unit.toMillis(timeout);
    var period = this.getPeriod();
    var timestamp = tempTimeout > 0 ? System.currentTimeMillis() : 0L;
    while (true) {
      if (period == ShirukaTask.PERIOD_NO_REPEATING || period == ShirukaTask.PERIOD_PROCESS_FOR_FUTURE) {
        this.wait(tempTimeout);
        period = this.getPeriod();
        if (period == ShirukaTask.PERIOD_NO_REPEATING || period == ShirukaTask.PERIOD_PROCESS_FOR_FUTURE) {
          if (tempTimeout == 0L) {
            continue;
          }
          tempTimeout += timestamp - (timestamp = System.currentTimeMillis());
          if (tempTimeout > 0) {
            continue;
          }
          throw new TimeoutException();
        }
      }
      if (period == ShirukaTask.PERIOD_CANCEL) {
        throw new CancellationException();
      }
      if (period == ShirukaTask.PERIOD_DONE_FOR_FUTURE) {
        if (this.exception == null) {
          return this.value;
        }
        throw new ExecutionException(this.exception);
      }
      throw new IllegalStateException(String.format("Expected %d to %d, got %d",
        ShirukaTask.PERIOD_NO_REPEATING, ShirukaTask.PERIOD_DONE_FOR_FUTURE, period));
    }
  }

  @Override
  public void run() {
    synchronized (this) {
      if (this.getPeriod() == ShirukaTask.PERIOD_CANCEL) {
        return;
      }
      this.setPeriod(ShirukaTask.PERIOD_PROCESS_FOR_FUTURE);
    }
    try {
      this.value = this.callable.call();
    } catch (final Exception e) {
      this.exception = e;
    } finally {
      synchronized (this) {
        this.setPeriod(ShirukaTask.PERIOD_DONE_FOR_FUTURE);
        this.notifyAll();
      }
    }
  }

  @Override
  synchronized void cancel0() {
    if (this.getPeriod() != ShirukaTask.PERIOD_NO_REPEATING) {
      return;
    }
    this.setPeriod(ShirukaTask.PERIOD_CANCEL);
    this.notifyAll();
  }
}
