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
 * managed set of threads that can be constrained in CPU resources and performs work stealing when necessary.
 */
public final class ServerThreadPool implements ExecutorService {

  /**
   * mapping of spec objects to delegate thread pools.
   */
  private static final Map<PoolSpec, ServerThreadPool> POOLS = new ConcurrentHashMap<>();

  /**
   * delegate executor service that is determined via spec in the {@link #forSpec(PoolSpec)} method.
   */
  @NotNull
  private final ExecutorService delegate;

  /**
   * ctor.
   *
   * @param delegate the delegate.
   */
  private ServerThreadPool(@NotNull final ExecutorService delegate) {
    this.delegate = delegate;
  }

  /**
   * creates a new thread pool for the given spec.
   *
   * @param spec the specification for the new thread pool.
   *
   * @return the thread pool that is based on the spec.
   */
  @NotNull
  public static ServerThreadPool forSpec(@NotNull final PoolSpec spec) {
    return ServerThreadPool.POOLS.computeIfAbsent(spec, k -> {
      final var maxThreads = spec.getMaxThreads();
      final ExecutorService service;
      if (spec.isDoStealing()) {
        service = new ForkJoinPool(maxThreads, spec, null, true);
      } else {
        service = new ThreadPoolExecutor(maxThreads, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(), spec);
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
  }

  /**
   * attempts to shutdown every thread pool that has been registered through a spec in the server.
   */
  public static void shutdownAll() {
    ServerThreadPool.POOLS.values()
      .forEach(ServerThreadPool::shutdown);
  }

  @Override
  public void execute(@NotNull final Runnable command) {
    this.delegate.execute(command);
  }

  @Override
  public void shutdown() {
    this.delegate.shutdown();
  }

  @NotNull
  @Override
  public List<Runnable> shutdownNow() {
    return this.delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return this.delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return this.delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(final long timeout, @NotNull final TimeUnit unit) throws InterruptedException {
    return this.delegate.awaitTermination(timeout, unit);
  }

  @NotNull
  @Override
  public <T> Future<T> submit(@NotNull final Callable<T> task) {
    return this.delegate.submit(task);
  }

  @NotNull
  @Override
  public <T> Future<T> submit(@NotNull final Runnable task, @NotNull final T result) {
    return this.delegate.submit(task, result);
  }

  @NotNull
  @Override
  public Future<?> submit(@NotNull final Runnable task) {
    return this.delegate.submit(task);
  }

  @NotNull
  @Override
  public <T> List<Future<T>> invokeAll(@NotNull final Collection<? extends Callable<T>> tasks)
    throws InterruptedException {
    return this.delegate.invokeAll(tasks);
  }

  @NotNull
  @Override
  public <T> List<Future<T>> invokeAll(@NotNull final Collection<? extends Callable<T>> tasks, final long timeout,
                                       @NotNull final TimeUnit unit) throws InterruptedException {
    return this.delegate.invokeAll(tasks, timeout, unit);
  }

  @NotNull
  @Override
  public <T> T invokeAny(@NotNull final Collection<? extends Callable<T>> tasks) throws InterruptedException,
    ExecutionException {
    return this.delegate.invokeAny(tasks);
  }

  @NotNull
  @Override
  public <T> T invokeAny(@NotNull final Collection<? extends Callable<T>> tasks, final long timeout,
                         @NotNull final TimeUnit unit) throws InterruptedException, ExecutionException,
    TimeoutException {
    return this.delegate.invokeAny(tasks, timeout, unit);
  }
}
