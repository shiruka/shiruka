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

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.TimingsManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.shiruka.api.Shiruka;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.ShirukaPlayer;
import net.shiruka.shiruka.util.RollingAverage;
import net.shiruka.shiruka.util.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents the server heartbeat pulse called "tick" which occurs every 1/20th of a second.
 */
public final class ShirukaTick implements Runnable {

  /**
   * the maximum tps.
   */
  public static final int TPS = 20;

  /**
   * the tick time.
   */
  public static final int TICK_TIME = 1000000000 / ShirukaTick.TPS;

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("ShirukaTick");

  /**
   * the sample interval.
   */
  private static final int SAMPLE_INTERVAL = 20;

  /**
   * tps for 1 minute.
   */
  private static final RollingAverage TPS_1 = new RollingAverage(60);

  /**
   * tps for 15 minutes.
   */
  private static final RollingAverage TPS_15 = new RollingAverage(60 * 15);

  /**
   * tps for 5 minutes.
   */
  private static final RollingAverage TPS_5 = new RollingAverage(60 * 5);

  /**
   * the tps base.
   */
  private static final BigDecimal TPS_BASE = new BigDecimal("1E9")
    .multiply(new BigDecimal(ShirukaTick.SAMPLE_INTERVAL));

  /**
   * the current tick.
   */
  public static int currentTick = 0;

  /**
   * the console command queue.
   */
  private final Queue<String> consoleCommandQueue = new ConcurrentLinkedQueue<>();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the force ticks.
   */
  public boolean forceTicks;

  /**
   * the delayed tasks max next tick time.
   */
  private long delayedTasksMaxNextTickTime;

  /**
   * the has ticked.
   */
  private boolean hasTicked;

  /**
   * is oversleep.
   */
  private boolean isOversleep = false;

  /**
   * the last overload time.
   */
  private long lastOverloadTime;

  /**
   * the last ticking.
   */
  private long lastTick = 0;

  /**
   * the may have delayed task.
   */
  private boolean mayHaveDelayedTasks;

  /**
   * the mid tick chunks tasks ran.
   */
  private int midTickChunksTasksRan;

  /**
   * the mid tick last ran.
   */
  private long midTickLastRan;

  /**
   * the next tick.
   */
  private long nextTick;

  /**
   * the ticks.
   */
  private int ticks;

  /**
   * ctor.
   *
   * @param server the server.
   */
  public ShirukaTick(@NotNull final ShirukaServer server) {
    this.server = server;
  }

  /**
   * obtains the tps of the server.
   *
   * @return a double array which has 3 average tps numbers for 1, 5, and 15 minutes.
   */
  public static double[] getTps() {
    return new double[]{
      ShirukaTick.TPS_1.getAverage(),
      ShirukaTick.TPS_5.getAverage(),
      ShirukaTick.TPS_15.getAverage()
    };
  }

  /**
   * adds the given {@code command}.
   *
   * @param command the command to add.
   */
  public void addCommand(@NotNull final String command) {
    this.consoleCommandQueue.add(command);
  }

  /**
   * checks if the thread can sleep for tick.
   *
   * @return {@code true} if the thread can sleep for tick.
   */
  public boolean canSleepForTick() {
    if (this.isOversleep) {
      return this.mayHaveDelayedTasks && SystemUtils.getMonotonicMillis() < this.delayedTasksMaxNextTickTime;
    }
    final var check = this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTick;
    return this.forceTicks || this.server.getTaskHandler().isEntered() || SystemUtils.getMonotonicMillis() < check;
  }

  /**
   * obtains the ticks.
   *
   * @return ticks.
   */
  public int getTicks() {
    return this.ticks;
  }

  /**
   * loads the chunks.
   */
  public void midTickLoadChunks() {
    if (!this.server.getTaskHandler().isMainThread() ||
      System.nanoTime() - this.midTickLastRan < 1000000) {
      return;
    }
    try (final var ignored = MinecraftTimings.midTickChunkTasks.startTiming()) {
//      for (final var value : this.server.getWorlds()) {
//        value.getChunkProvider().serverThreadQueue.midTickLoadChunks();
//      }
      this.midTickLastRan = System.nanoTime();
    }
  }

  /**
   * starts the heartbeat.
   */
  @Override
  public void run() {
    this.nextTick = SystemUtils.getMonotonicMillis();
    final var warnOnOverload = ServerConfig.WARN_ON_OVERLOAD.getValue()
      .orElse(true);
    final var start = System.nanoTime();
    var currentTime = 0L;
    var tickSection = start;
    this.lastTick = start - ShirukaTick.TICK_TIME;
    while (this.server.isRunning()) {
      final var tickTime = (currentTime = System.nanoTime()) / (1000L * 1000L) - this.nextTick;
      if (tickTime > 5000L && this.nextTick - this.lastOverloadTime >= 30000L) {
        final var waitTime = tickTime / 50L;
        if (warnOnOverload) {
          ShirukaTick.LOGGER.warn(TranslatedText.get("shiruka.concurrent.tick.run.overload", tickTime, waitTime));
        }
        this.nextTick += waitTime * 50L;
        this.lastOverloadTime = this.nextTick;
      }
      if (++ShirukaTick.currentTick % ShirukaTick.SAMPLE_INTERVAL == 0) {
        final var diff = currentTime - tickSection;
        final var currentTps = ShirukaTick.TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
        ShirukaTick.TPS_1.add(currentTps, diff);
        ShirukaTick.TPS_5.add(currentTps, diff);
        ShirukaTick.TPS_15.add(currentTps, diff);
        tickSection = currentTime;
      }
      this.midTickChunksTasksRan = 0;
      this.lastTick = currentTime;
      this.nextTick += 50L;
      try (final var ignored = TimingsManager.FULL_SERVER_TICK.startTiming()) {
        this.doTick();
      }
      this.mayHaveDelayedTasks = true;
      this.delayedTasksMaxNextTickTime = Math.max(SystemUtils.getMonotonicMillis() + 50L, this.nextTick);
      this.sleepForTick();
      this.hasTicked = true;
    }
  }

  /**
   * checks if the thread can oversleep.
   *
   * @return {@code true} if the thread can oversleep.
   */
  private boolean canOversleep() {
    return this.mayHaveDelayedTasks &&
      SystemUtils.getMonotonicMillis() < this.delayedTasksMaxNextTickTime;
  }

  /**
   * checks if thread can sleep for tick no oversleep.
   *
   * @return {@code true} if thread can sleep for tick no oversleep.
   */
  private boolean canSleepForTickNoOversleep() {
    return this.forceTicks ||
      this.server.getTaskHandler().isEntered() ||
      SystemUtils.getMonotonicMillis() < this.nextTick;
  }

  /**
   * runs the game's elements.
   */
  private void doTick() {
    final var now = SystemUtils.getMonotonicNanos();
    this.isOversleep = true;
    try (final var ignored = MinecraftTimings.serverOversleep.startTiming()) {
      this.server.getTaskHandler().awaitJobs(() -> {
        this.midTickLoadChunks();
        return !this.canOversleep();
      });
      this.isOversleep = false;
    }
    Shiruka.getEventManager().serverTick(++this.ticks).callEvent();
    this.doTick0();
    this.handleQueuedConsoleCommands();
  }

  /**
   * runs the game's elements.
   */
  private void doTick0() {
    this.midTickLoadChunks();
    try (final var ignored = MinecraftTimings.shirukaSchedulerTimer.startTiming()) {
      this.server.getScheduler().mainThreadHeartbeat(this.ticks);
    }
    this.midTickLoadChunks();
    this.server.getOnlinePlayers().forEach(ShirukaPlayer::tick);
  }

  /**
   * handles queued console commands.
   */
  private void handleQueuedConsoleCommands() {
    try (final var ignored = MinecraftTimings.serverCommandTimer.startTiming()) {
      String command;
      while ((command = this.consoleCommandQueue.poll()) != null) {
        final var sender = Shiruka.getConsoleCommandSender();
        final var event = Shiruka.getEventManager().serverCommand(sender, command);
        if (event.callEvent()) {
          Shiruka.getCommandManager().execute(event.getCommand(), sender);
        }
      }
    }
  }

  /**
   * sleeps for tick.
   */
  private void sleepForTick() {
    this.server.getTaskHandler().awaitJobs(() -> !this.canSleepForTickNoOversleep());
  }
}
