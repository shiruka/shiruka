/*
 * This file is licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 Daniel Ennis <http://aikar.co>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.aikar.timings;

import co.aikar.util.LoadingMap;
import com.google.common.collect.EvictingQueue;
import net.shiruka.api.Shiruka;
import net.shiruka.api.command.tree.LiteralNode;
import net.shiruka.api.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.shiruka.api.plugin.java.JavaPluginClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TimingsManager {

  public static final FullServerTickHandler FULL_SERVER_TICK = new FullServerTickHandler();

  static final List<TimingHandler> HANDLERS = new ArrayList<>(1024);

  static final List<TimingHistory.MinuteReport> MINUTE_REPORTS = new ArrayList<>(64);

  static final Map<TimingIdentifier, TimingHandler> TIMING_MAP = LoadingMap.of(
    new ConcurrentHashMap<>(4096, .5F), TimingHandler::new
  );

  public static final Timing PLUGIN_GROUP_HANDLER = Timings.ofSafe("Plugins");

  public static final TimingHandler TIMINGS_TICK = Timings.ofSafe("Timings Tick", TimingsManager.FULL_SERVER_TICK);

  public static List<String> hiddenConfigs = new ArrayList<String>();

  public static boolean privacy = false;

  static EvictingQueue<TimingHistory> HISTORY = EvictingQueue.create(12);

  static long historyStart = 0;

  static boolean needsFullReset = false;

  static boolean needsRecheckEnabled = false;

  static long timingStart = 0;

  private TimingsManager() {
  }

  /**
   * Due to access restrictions, we need a helper method to get a Command TimingHandler with String group
   * <p>
   * Plugins should never call this
   *
   * @param pluginName Plugin this command is associated with
   * @param command Command to get timings for
   *
   * @return TimingHandler
   */
  @NotNull
  public static Timing getCommandTiming(@Nullable final String pluginName, @NotNull final LiteralNode command) {
    var plugin = Optional.<Plugin>empty();
    final var server = Shiruka.getServer();
    if (pluginName != null && !"minecraft".equals(pluginName) && !"shiruka".equalsIgnoreCase(pluginName)) {
      plugin = Shiruka.getPluginManager().getPlugin(pluginName);
    }
    if (plugin.isEmpty()) {
      plugin = Optional.ofNullable(TimingsManager.getPluginByClassloader(command.getClass()));
    }
    if (plugin.isEmpty()) {
      return Timings.ofSafe("Command: " + pluginName + ":" + command.getName());
    }
    return Timings.ofSafe(plugin.get(), "Command: " + pluginName + ":" + command.getName());
  }

  /**
   * Looks up the class loader for the specified class, and if it is a PluginClassLoader, return the
   * Plugin that created this class.
   *
   * @param clazz Class to check
   *
   * @return Plugin if created by a plugin
   */
  @Nullable
  public static Plugin getPluginByClassloader(@Nullable final Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    final var classLoader = clazz.getClassLoader();
    if (classLoader instanceof JavaPluginClassLoader) {
      return ((JavaPluginClassLoader) classLoader).getPlugin();
    }
    return null;
  }

  @NotNull
  static TimingHandler getHandler(@Nullable final String group, @NotNull final String name, @Nullable final Timing parent) {
    return TimingsManager.TIMING_MAP.get(new TimingIdentifier(group, name, parent));
  }

  static void recheckEnabled() {
    synchronized (TimingsManager.TIMING_MAP) {
      for (final var timings : TimingsManager.TIMING_MAP.values()) {
        timings.checkEnabled();
      }
    }
    TimingsManager.needsRecheckEnabled = false;
  }

  /**
   * Resets all timing data on the next tick
   */
  static void reset() {
    TimingsManager.needsFullReset = true;
  }

  static void resetTimings() {
    if (TimingsManager.needsFullReset) {
      // Full resets need to re-check every handlers enabled state
      // Timing map can be modified from async so we must sync on it.
      synchronized (TimingsManager.TIMING_MAP) {
        for (final var timings : TimingsManager.TIMING_MAP.values()) {
          timings.reset(true);
        }
      }
      Shiruka.getLogger().info("Timings Reset");
      TimingsManager.HISTORY.clear();
      TimingsManager.needsFullReset = false;
      TimingsManager.needsRecheckEnabled = false;
      TimingsManager.timingStart = System.currentTimeMillis();
    } else {
      // Soft resets only need to act on timings that have done something
      // Handlers can only be modified on main thread.
      for (final var timings : TimingsManager.HANDLERS) {
        timings.reset(false);
      }
    }
    TimingsManager.HANDLERS.clear();
    TimingsManager.MINUTE_REPORTS.clear();
    TimingHistory.resetTicks(true);
    TimingsManager.historyStart = System.currentTimeMillis();
  }

  static void stopServer() {
    Timings.timingsEnabled = false;
    TimingsManager.recheckEnabled();
  }

  /**
   * Ticked every tick by Shiru ka to count the number of times a timer
   * caused TPS loss.
   */
  static void tick() {
    if (Timings.timingsEnabled) {
      final var violated = TimingsManager.FULL_SERVER_TICK.isViolated();
      for (final var handler : TimingsManager.HANDLERS) {
        if (handler.isSpecial()) {
          // We manually call this
          continue;
        }
        handler.processTick(violated);
      }
      TimingHistory.playerTicks += Shiruka.getOnlinePlayers().size();
      TimingHistory.timedTicks++;
      // Generate TPS/Ping/Tick reports every minute
    }
  }
}
