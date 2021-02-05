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
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.network.PlayerConnection;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.util.RollingAverage;
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
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("ShirukaTick");

  /**
   * the sample interval.
   */
  private static final int SAMPLE_INTERVAL = 20;

  /**
   * the amount of time taken by a single tick.
   */
  private static final long TICK_NANOS = TimeUnit.SECONDS.toNanos(1) / 20;

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
   * the connected players.
   */
  public final Map<InetSocketAddress, PlayerConnection> connectedPlayers =
    Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

  /**
   * the pending.
   */
  public final Queue<RakNetClientPeer> pending = new ConcurrentLinkedQueue<>();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

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
   * calculates the tps of the server.
   *
   * @param start the start to calculate.
   * @param tickSection the tick section to calculate.
   */
  private static void calculateTps(final long start, @NotNull final AtomicLong tickSection) {
    if (++ShirukaTick.currentTick % ShirukaTick.SAMPLE_INTERVAL == 0) {
      final var different = start - tickSection.get();
      final var currentTps = ShirukaTick.TPS_BASE.divide(
        new BigDecimal(different),
        30,
        RoundingMode.HALF_UP);
      ShirukaTick.TPS_1.add(currentTps, different);
      ShirukaTick.TPS_5.add(currentTps, different);
      ShirukaTick.TPS_15.add(currentTps, different);
      tickSection.set(start);
    }
  }

  /**
   * starts the heartbeat.
   */
  @Override
  public void run() {
    final var tickSection = new AtomicLong(System.nanoTime());
    while (this.server.isRunning()) {
      try {
        final var start = System.nanoTime();
        ShirukaTick.calculateTps(start, tickSection);
        this.doTick();
        final var end = System.nanoTime();
        final var elapsed = end - start;
        final var waitTime = TimeUnit.NANOSECONDS.toMillis(ShirukaTick.TICK_NANOS - elapsed);
        if (waitTime < 0) {
          ShirukaTick.LOGGER.debug("Server running behind " +
            -waitTime + "ms, skipped " + -waitTime / ShirukaTick.TICK_NANOS + " ticks");
        } else {
          Thread.sleep(waitTime);
        }
      } catch (final InterruptedException e) {
        break;
      } catch (final Exception e) {
        JiraExceptionCatcher.serverException(e);
        break;
      }
    }
  }

  /**
   * does the tick operations.
   */
  private void doTick() {
    // @todo #1:15m Implement worlds.tick()
    RakNetClientPeer peer;
    while ((peer = this.pending.poll()) != null) {
      this.connectedPlayers.put(peer.getAddress(), new PlayerConnection(peer));
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
        final var packet = new DisconnectPacket(
          TranslatedText.get("shiruka.concurrent.tick.do_tick.login_error").asString(),
          false);
        connection.sendPacketImmediately(packet);
        JiraExceptionCatcher.serverException(e);
      }
    }
    this.server.getScheduler().mainThreadHeartbeat(++this.ticks);
  }
}
