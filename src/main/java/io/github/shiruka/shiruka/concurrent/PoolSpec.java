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
package io.github.shiruka.shiruka.concurrent;

import io.github.shiruka.api.log.Loggers;
import java.io.PrintStream;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

/**
 * specifier for creating a block of threads used for
 * managing the server thread pool.
 */
public final class PoolSpec implements ThreadFactory, ForkJoinPool.ForkJoinWorkerThreadFactory,
  Thread.UncaughtExceptionHandler {

  /**
   * the thread for worlds.
   */
  public static final PoolSpec CHUNKS = new PoolSpec("Shiru ka - Chunks", 4, true);

  /**
   * the thread for entities.
   */
  public static final PoolSpec ENTITIES = new PoolSpec("Shiru ka - Entities", 3, false);

  /**
   * the thread for players.
   */
  public static final PoolSpec PLAYERS = new PoolSpec("Shiru ka - Players", 3, false);

  /**
   * the thread for plugins.
   */
  public static final PoolSpec PLUGINS = new PoolSpec("Shiru ka - Plugins", 1, false);

  /**
   * the thread for schedulers.
   */
  public static final PoolSpec SCHEDULER = new PoolSpec("Shiru ka - Scheduler", 3, false);

  /**
   * a thread factory that does handling for exceptions,
   * piping exception output to the loggers.
   */
  public static final ThreadFactory UNCAUGHT_FACTORY = new PoolSpec("Shiru ka - Net", 0,
    false);

  /**
   * the thread for worlds.
   */
  public static final PoolSpec WORLDS = new PoolSpec("Shiru ka - Worlds", 4, true);

  /**
   * whether or not the task order is relevant.
   */
  private final boolean doStealing;

  /**
   * maximum number of parallelism that should be limited in the given thread pool.
   */
  private final int maxThreads;

  /**
   * the name of the pool used to identify its threads.
   */
  private final String name;

  /**
   * Creates a new thread pool spec.
   *
   * @param name the name of the pool
   * @param maxThreads the max thread limit
   * @param doStealing whether or not the pool performs
   *   work steals
   */
  private PoolSpec(final String name, final int maxThreads, final boolean doStealing) {
    this.name = name;
    this.maxThreads = maxThreads;
    this.doStealing = doStealing;
  }

  /**
   * maximum number of parallelism that should be limited in the given thread pool.
   *
   * @return maximum number of parallelism that should be limited in the given thread pool.
   */
  public int getMaxThreads() {
    return this.maxThreads;
  }

  /**
   * the name of the pool used to identify its threads.
   *
   * @return the name of the pool used to identify its threads.
   */
  @NotNull
  public String getName() {
    return this.name;
  }

  /**
   * whether or not the task order is relevant.
   *
   * @return whether or not the task order is relevant.
   */
  public boolean isDoStealing() {
    return this.doStealing;
  }

  @Override
  public Thread newThread(@NotNull final Runnable r) {
    final var thread = new Thread(r, this.name);
    thread.setUncaughtExceptionHandler(this);
    return thread;
  }

  @Override
  public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
    final var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
    worker.setName(this.name + " - " + worker.getPoolIndex());
    worker.setUncaughtExceptionHandler(this);
    return worker;
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    e.printStackTrace(new PrintStream(System.out) {
      @Override
      public void println(final Object x) {
        Loggers.error(String.valueOf(x));
      }
    });
  }
}
