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

import com.whirvis.jraknet.peer.RakNetClientPeer;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.shiruka.api.Shiruka;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.network.PlayerConnection;
import net.shiruka.shiruka.text.TranslatedTexts;
import net.shiruka.shiruka.util.RollingAverage;
import net.shiruka.shiruka.util.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents the server heartbeat pulse called "tick" which occurs every 1/20th of a second.
 */
public final class ShirukaTick extends AsyncTaskHandlerReentrant<TickTask> implements Runnable {

  /**
   * the tps.
   */
  public static final int TPS = 20;

  /**
   * the tick time.
   */
  public static final int TICK_TIME = 1000000000 / ShirukaTick.TPS;

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger();

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
   * the command queue.
   */
  public final PriorityQueue<String> commandQueue = new ObjectArrayFIFOQueue<>();

  /**
   * the connected players.
   */
  public final Map<InetSocketAddress, PlayerConnection> connectedPlayers =
    Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

  /**
   * the pending.
   */
  public final PriorityQueue<RakNetClientPeer> pending = new ObjectArrayFIFOQueue<>();

  /**
   * the process queue.
   */
  private final Queue<Runnable> processQueue = new ConcurrentLinkedQueue<>();

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
   * the last ping time.
   */
  public long lastPingTime;

  /**
   * the next tick.
   */
  public long nextTick = SystemUtils.getMonotonicMillis();

  /**
   * the executed task.
   */
  private boolean hasExecutedTask;

  /**
   * the last overload time.
   */
  private long lastOverloadTime;

  /**
   * the last tick.
   */
  private long lastTick;

  /**
   * the is oversleep.
   */
  private boolean overslept;

  /**
   * the tick oversleep max time.
   */
  private long tickOversleepMaxTime;

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
    super("Server");
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
   * checks if the thread can sleep for tick.
   *
   * @return {@code true} if the thread can sleep for tick.
   */
  public boolean canSleepForTick() {
    if (this.overslept) {
      return this.canOversleep();
    }
    return this.forceTicks ||
      this.isEntered() ||
      SystemUtils.getMonotonicMillis() < (this.hasExecutedTask ? this.tickOversleepMaxTime : this.nextTick);
  }

  @Override
  public boolean isNotMainThread() {
    return super.isNotMainThread() && !this.server.isStopped();
  }

  /**
   * starts the heartbeat.
   */
  @Override
  public void run() {
    final var start = System.nanoTime();
    var currentTime = 0L;
    var tickSection = start;
    this.lastTick = start - ShirukaTick.TICK_TIME;
    try {
      while (this.server.isRunning()) {
        final var now = (currentTime = System.nanoTime()) / (1000L * 1000L) - this.nextTick;
        if (now > 5000L && this.nextTick - this.lastOverloadTime >= 30000L) {
          final var waitTime = now / 50L;
          ShirukaTick.LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind",
            now, waitTime);
          this.nextTick += waitTime * 50L;
          this.lastOverloadTime = this.nextTick;
        }
        if (++ShirukaTick.currentTick % ShirukaTick.SAMPLE_INTERVAL == 0) {
          final var different = currentTime - tickSection;
          final var currentTps = ShirukaTick.TPS_BASE.divide(new BigDecimal(different), 30,
            RoundingMode.HALF_UP);
          ShirukaTick.TPS_1.add(currentTps, different);
          ShirukaTick.TPS_5.add(currentTps, different);
          ShirukaTick.TPS_15.add(currentTps, different);
          tickSection = currentTime;
        }
        this.lastTick = currentTime;
        this.nextTick += 50;
        this.doTick();
        this.hasExecutedTask = true;
        this.tickOversleepMaxTime = Math.max(SystemUtils.getMonotonicMillis() + 50L, this.nextTick);
        this.awaitTasks(() -> !this.canSleepForTickNoOversleep());
      }
    } catch (final Throwable e) {
      JiraExceptionCatcher.serverException(e);
    } finally {
      this.server.isStopped = true;
      this.server.stopServer();
    }
  }

  @Override
  protected boolean canExecute(@NotNull final TickTask task) {
    return task.getTick() + 3 < this.ticks || this.canSleepForTick();
  }

  @Override
  public boolean executeNext() {
    final var flag = this.pollTaskInternal();
    this.hasExecutedTask = flag;
    return flag;
  }

  @NotNull
  @Override
  protected Thread getThread() {
    return this.server.getServerThread();
  }

  @NotNull
  @Override
  protected TickTask postToMainThread(@NotNull Runnable runnable) {
    if (this.server.hasStopped && Thread.currentThread().equals(this.server.getShutdownThread())) {
      runnable.run();
      runnable = () -> {
      };
    }
    return new TickTask(runnable, this.ticks);
  }

  /**
   * checks can server oversleep.
   *
   * @return {@code true} if server can oversleep.
   */
  private boolean canOversleep() {
    return this.hasExecutedTask && SystemUtils.getMonotonicMillis() < this.tickOversleepMaxTime;
  }

  /**
   * checks if server can sleep for tick no oversleep.
   *
   * @return {@code true} if server can sleep for tick no oversleep.
   */
  private boolean canSleepForTickNoOversleep() {
    return this.forceTicks || this.isEntered() || SystemUtils.getMonotonicMillis() < this.nextTick;
  }

  /**
   * ticks connection operations.
   */
  private void connectionTick() {
    while (!this.pending.isEmpty()) {
      final var peer = this.pending.dequeue();
      this.connectedPlayers.put(peer.getAddress(), new PlayerConnection(peer, this.server));
    }
    final var iterator = this.connectedPlayers.values().iterator();
    while (iterator.hasNext()) {
      final var connection = iterator.next();
      if (connection.getConnection().isDisconnected()) {
        iterator.remove();
        continue;
      }
      try {
        connection.tick();
      } catch (final Exception e) {
        connection.disconnect(TranslatedTexts.LOGIN_ERROR);
        JiraExceptionCatcher.serverException(e);
      }
    }
  }

  /**
   * does the tick operations.
   */
  private void doTick() {
    final var now = System.nanoTime();
    this.overslept = true;
    this.awaitTasks(() -> !this.canOversleep());
    this.overslept = false;
    Shiruka.getEventManager().serverTickStart(++this.ticks).callEvent();
    this.server.getScheduler().mainThreadHeartbeat(this.ticks);
    while (!this.processQueue.isEmpty()) {
      this.processQueue.remove().run();
    }
    this.worldTick();
    this.connectionTick();
    this.handleCommands();
    if (now - this.lastPingTime >= 5000000000L) {
      this.lastPingTime = now;
      this.server.updatePing();
    }
    this.executeAll();
    final var endTime = System.nanoTime();
    final var remaining = ShirukaTick.TICK_TIME - (endTime - this.lastTick);
    final var duration = (double) (endTime - this.lastTick) / 1000000D;
    Shiruka.getEventManager().serverTickEnd(this.ticks, duration, remaining).callEvent();
  }

  /**
   * handles the {@link #commandQueue}.
   */
  private void handleCommands() {
    while (!this.commandQueue.isEmpty()) {
      final var command = this.commandQueue.dequeue();
      final var event = Shiruka.getEventManager().serverCommand(Shiruka.getConsoleCommandSender(),
        command);
      event.callEvent();
      if (event.isCancelled()) {
        continue;
      }
      Shiruka.getCommandManager().execute(event.getCommand(), event.getSender());
    }
  }

  /**
   * polls the internal tasks.
   *
   * @return the internal tasks successfully executed.
   */
  private boolean pollTaskInternal() {
    if (super.executeNext()) {
      return true;
    }
    if (this.canSleepForTick()) {
//      final var iterator = this.getWorlds().iterator();
//      while (iterator.hasNext()) {
//        final WorldServer worldserver = (WorldServer) iterator.next();
//        if (worldserver.getChunkProvider().runTasks()) {
//          return true;
//        }
//      }
    }
    return false;
  }

  /**
   * ticks world operations.
   */
  private void worldTick() {
  }
}
