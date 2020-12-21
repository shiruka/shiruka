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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.jetbrains.annotations.NotNull;

/**
 * managed set of threads that can be constrained
 * in CPU resources and performs work stealing when
 * necessary.
 */
public final class ServerThreadPool implements Executor {

  /**
   * mapping of spec objects to delegate thread pools.
   */
  private static final Map<PoolSpec, ServerThreadPool> POOLS = new ConcurrentHashMap<>();

  /**
   * delegate executor service that is determined via
   * spec in the {@link #forSpec(PoolSpec)} method.
   */
  @NotNull
  private final ExecutorService service;

  /**
   * ctor.
   *
   * @param service the service.
   */
  private ServerThreadPool(@NotNull final ExecutorService service) {
    this.service = service;
  }

  /**
   * creates a new thread pool for the given spec.
   *
   * @param spec the specification for the new thread
   *   pool.
   *
   * @return the thread pool that is based on the spec.
   */
  @NotNull
  public static ServerThreadPool forSpec(@NotNull final PoolSpec spec) {
    return ServerThreadPool.POOLS.computeIfAbsent(spec, k -> {
      final var config = spec.getMaxThreads();
      final ExecutorService service;
      if (spec.isDoStealing()) {
        service = new ForkJoinPool(config, spec, null, true);
      } else {
        service = new ThreadPoolExecutor(1, config, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), spec);
      }
      return new ServerThreadPool(service);
    });
  }

  /**
   * initiates server's pool specs.
   */
  public static void init() {
    ServerThreadPool.forSpec(PoolSpec.WORLDS);
    ServerThreadPool.forSpec(PoolSpec.CHUNKS);
    ServerThreadPool.forSpec(PoolSpec.ENTITIES);
    ServerThreadPool.forSpec(PoolSpec.PLAYERS);
    ServerThreadPool.forSpec(PoolSpec.PLUGINS);
    ServerThreadPool.forSpec(PoolSpec.SCHEDULER);
  }

  /**
   * attempts to shutdown every thread pool that has been
   * registered through a spec in the server.
   */
  public static void shutdownAll() {
    ServerThreadPool.POOLS.values()
      .forEach(ServerThreadPool::shutdown);
  }

  /**
   * executes the given runnable command in the thread
   * pool.
   *
   * @param command the command which to schedule for
   *   running.
   */
  @Override
  public void execute(@NotNull final Runnable command) {
    this.service.execute(command);
  }

  /**
   * submits the given {@link Callable}.
   *
   * @param tasks the tasks to submit.
   * @param <T> type of the submitted task.
   *
   * @return submitted future.
   *
   * @throws InterruptedException if interrupted while waiting, in
   *   which case unfinished tasks are cancelled
   */
  @NotNull
  public <T> List<Future<T>> invokeAll(@NotNull final Collection<? extends Callable<T>> tasks)
    throws InterruptedException {
    return this.service.invokeAll(tasks);
  }

  /**
   * attempts to shutdown the thread pool immediately.
   */
  public void shutdown() {
    this.service.shutdown();
  }

  /**
   * submits the given {@link Runnable}.
   *
   * @param task the task to submit.
   *
   * @return submitted future.
   */
  @NotNull
  public Future<?> submit(@NotNull final Runnable task) {
    return this.service.submit(task);
  }

  /**
   * submits the given {@link Callable}.
   *
   * @param task the task to submit.
   * @param <T> type of the submitted task.
   *
   * @return submitted future.
   */
  @NotNull
  public <T> Future<T> submit(@NotNull final Callable<T> task) {
    return this.service.submit(task);
  }

  /**
   * submits the given {@link Runnable}.
   *
   * @param task the task to submit.
   * @param <T> type of the submitted task.
   * @param result the result to submit.
   *
   * @return submitted future.
   */
  @NotNull
  public <T> Future<T> submit(@NotNull final Runnable task, @NotNull final T result) {
    return this.service.submit(task, result);
  }
}
