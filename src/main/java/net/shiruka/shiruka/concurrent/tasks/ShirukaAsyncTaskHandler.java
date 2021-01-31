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

package net.shiruka.shiruka.concurrent.tasks;

import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import net.shiruka.shiruka.concurrent.TickTask;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents Shiru ka's async task handler.
 */
public final class ShirukaAsyncTaskHandler extends AsyncTaskHandlerReentrant<TickTask> {

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the tick.
   */
  @NotNull
  private final ShirukaTick tick;

  /**
   * ctor.
   *
   * @param server the server.
   * @param tick the tick.
   */
  public ShirukaAsyncTaskHandler(@NotNull final ShirukaServer server, @NotNull final ShirukaTick tick) {
    super("Shiruka");
    this.server = server;
    this.tick = tick;
  }

  @Override
  public boolean canExecute(@NotNull final TickTask job) {
    return job.getTick() + 3 < this.tick.getTicks() || this.tick.canSleepForTick();
  }

  @Override
  public boolean executeNext() {
    final var flag = this.pollTaskInternal();
    this.tick.setMayHaveDelayedTasks(flag);
    return flag;
  }

  @NotNull
  @Override
  public Thread getThread() {
    return this.server.getServerThread();
  }

  @Override
  public TickTask postToMainThread(@NotNull Runnable job) {
    if (this.server.isStopped() &&
      Thread.currentThread().equals(this.server.getShutdownThread())) {
      job.run();
      job = () -> {
      };
    }
    return new TickTask(job, this.tick.getTicks());
  }

  @Override
  public void close() {
    this.server.stopServer();
  }

  @Override
  public boolean isNotMainThread() {
    return super.isNotMainThread() && !this.server.isStopped();
  }

  /**
   * polls internal tasks.
   */
  private boolean pollTaskInternal() {
    if (super.executeNext()) {
      return true;
    }
    //      final var iterator = this.getWorlds().iterator();
    //      while (iterator.hasNext()) {
    //        final var worldServer = (WorldServer) iterator.next();
    //        if (worldServer.getChunkProvider().runTasks()) {
    //        }
    //      }
    return this.tick.canSleepForTick();
  }
}
