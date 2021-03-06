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

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.Shiruka;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.misc.RollingAverage;
import net.shiruka.shiruka.misc.TickTask;
import net.shiruka.shiruka.misc.TickTimes;
import net.shiruka.shiruka.network.NetworkManager;
import net.shiruka.shiruka.text.TranslatedTexts;
import net.shiruka.shiruka.util.SystemUtils;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents the server heartbeat pulse called "tick" which occurs every 1/20th of a second.
 */
@Log4j2
public final class ShirukaTick extends AsyncTaskHandlerReentrant<TickTask> implements Runnable {

  /**
   * the tps.
   */
  public static final int TPS = 20;

  /**
   * the sample interval.
   */
  private static final int SAMPLE_INTERVAL = 20;

  /**
   * the server overload.
   */
  private static final String SERVER_OVERLOAD = "shiruka.server.overload";

  /**
   * the tick time.
   */
  private static final int TICK_TIME = 1000000000 / ShirukaTick.TPS;

  /**
   * the tps base.
   */
  private static final BigDecimal TPS_BASE = new BigDecimal("1E9")
    .multiply(new BigDecimal(ShirukaTick.SAMPLE_INTERVAL));

  /**
   * the command queue.
   */
  @Getter
  private final PriorityQueue<String> commandQueue = new ObjectArrayFIFOQueue<>();

  /**
   * the connected players.
   */
  @Getter
  private final Map<InetSocketAddress, NetworkManager> connectedPlayers = new ConcurrentHashMap<>();

  /**
   * the pending.
   */
  @Getter
  private final PriorityQueue<BedrockServerSession> pending = new ObjectArrayFIFOQueue<>();

  /**
   * the process queue.
   */
  @Getter
  private final Queue<Runnable> processQueue = new ConcurrentLinkedQueue<>();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the tick times.
   */
  private final long[] tickTimes = new long[100];

  /**
   * tick times for 10 seconds.
   */
  private final TickTimes tickTimes10S = new TickTimes(200);

  /**
   * tick times for 5 seconds.
   */
  private final TickTimes tickTimes5S = new TickTimes(100);

  /**
   * tick times for 60 seconds.
   */
  private final TickTimes tickTimes60S = new TickTimes(1200);

  /**
   * tps for 1 minute.
   */
  private final RollingAverage tps1 = new RollingAverage(60);

  /**
   * tps for 15 minutes.
   */
  private final RollingAverage tps15 = new RollingAverage(60 * 15);

  /**
   * tps for 5 minutes.
   */
  private final RollingAverage tps5 = new RollingAverage(60 * 5);

  /**
   * the current average tick time.
   */
  private float currentAverageTickTime;

  /**
   * the current tick.
   */
  @Getter
  private int currentTick = 0;

  /**
   * the current tick as long.
   */
  @Getter
  private long currentTickLong = 0L;

  /**
   * the force ticks.
   */
  @Setter
  private boolean forceTicks;

  /**
   * the executed task.
   */
  private boolean hasExecutedTask;

  /**
   * the last overload time.
   */
  private long lastOverloadTime;

  /**
   * the last ping time.
   */
  @Setter
  private long lastPingTime;

  /**
   * the last tick.
   */
  private long lastTick;

  /**
   * the next tick.
   */
  @Setter
  private long nextTick = SystemUtils.getMonotonicMillis();

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

  /**
   * obtains the tick times of the server.
   *
   * @return a double array which has 3 average tick times numbers for 5, 10, and 60 seconds.
   */
  public long[][] getTickTimes() {
    return new long[][]{
      this.tickTimes5S.getTimes(),
      this.tickTimes10S.getTimes(),
      this.tickTimes60S.getTimes()
    };
  }

  /**
   * obtains the tps of the server.
   *
   * @return a double array which has 3 average tps numbers for 1, 5, and 15 minutes.
   */
  public double[] getTps() {
    return new double[]{
      this.tps1.getAverage(),
      this.tps5.getAverage(),
      this.tps15.getAverage()
    };
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
          if (ServerConfig.warnOnOverload) {
            ShirukaTick.log.warn(TranslatedText.get(ShirukaTick.SERVER_OVERLOAD, now, waitTime));
          }
          this.nextTick += waitTime * 50L;
          this.lastOverloadTime = this.nextTick;
        }
        this.currentTickLong++;
        if (++this.currentTick % ShirukaTick.SAMPLE_INTERVAL == 0) {
          final var different = currentTime - tickSection;
          final var currentTps = ShirukaTick.TPS_BASE.divide(new BigDecimal(different), 30,
            RoundingMode.HALF_UP);
          this.tps1.add(currentTps, different);
          this.tps5.add(currentTps, different);
          this.tps15.add(currentTps, different);
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
  protected TickTask postToMainThread(@NotNull final Runnable runnable) {
    var tempRunnable = runnable;
    if (this.server.hasStopped && Thread.currentThread().equals(this.server.getShutdownThread())) {
      tempRunnable.run();
      tempRunnable = () -> {
      };
    }
    return new TickTask(tempRunnable, this.ticks);
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
      final var client = this.pending.dequeue();
      this.connectedPlayers.put(client.getAddress(), new NetworkManager(client, this.server));
    }
    final var iterator = this.connectedPlayers.values().iterator();
    while (iterator.hasNext()) {
      final var manager = iterator.next();
      if (manager.isConnected()) {
        try {
          manager.tick();
        } catch (final Exception e) {
          // @todo #1:5m Add language support for Failed to handle packet for {}.
          ShirukaTick.log.warn("Failed to handle packet for {}", manager.getSocketAddress(), e);
          final var kickMessage = TranslatedTexts.LOGIN_ERROR;
          final var packet = new DisconnectPacket();
          packet.setKickMessage(kickMessage.asString());
          packet.setMessageSkipped(false);
          manager.getClient().sendPacketImmediately(packet);
          manager.close(kickMessage);
          JiraExceptionCatcher.serverException(e);
        }
      } else {
        iterator.remove();
        manager.handleDisconnection();
      }
    }
  }

  /**
   * does the tick operations.
   */
  private void doTick() {
    final var start = System.nanoTime();
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
    if (start - this.lastPingTime >= 5000000000L) {
      this.lastPingTime = start;
    }
    this.executeAll();
    final var endTime = System.nanoTime();
    final var remaining = ShirukaTick.TICK_TIME - (endTime - this.lastTick);
    final var duration = (double) (endTime - this.lastTick) / 1000000D;
    Shiruka.getEventManager().serverTickEnd(this.ticks, duration, remaining).callEvent();
    final var tickTime = System.nanoTime() - start;
    this.tickTimes[this.ticks % 100] = tickTime;
    this.currentAverageTickTime = this.currentAverageTickTime * 0.8F + (float) tickTime / 1000000.0F * 0.19999999F;
    this.tickTimes5S.add(this.ticks, tickTime);
    this.tickTimes10S.add(this.ticks, tickTime);
    this.tickTimes60S.add(this.ticks, tickTime);
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
//    if (super.executeNext()) {
//      return true;
//    }
//    if (this.canSleepForTick()) {
//      final var iterator = this.getWorlds().iterator();
//      while (iterator.hasNext()) {
//        final WorldServer worldserver = (WorldServer) iterator.next();
//        if (worldserver.getChunkProvider().runTasks()) {
//          return true;
//        }
//      }
//      return true;
//    }
//    return false;
    return super.executeNext();
  }

  /**
   * ticks world operations.
   */
  private void worldTick() {
  }
}
