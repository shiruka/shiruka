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

package co.aikar.timings;

import com.google.common.collect.MapMaker;
import java.util.Map;
import net.shiruka.api.block.Block;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.shiruka.network.packet.Packet;
import net.shiruka.shiruka.scheduler.ShirukaTask;

public final class MinecraftTimings {

  public static final Timing antiXrayObfuscateTimer = Timings.ofSafe("anti-xray - obfuscate");

  public static final Timing antiXrayUpdateTimer = Timings.ofSafe("anti-xray - update");

  public static final Timing chunkIOTickTimer = Timings.ofSafe("ChunkIOTick");

  public static final Timing commandFunctionsTimer = Timings.ofSafe("Command Functions");

  public static final Timing connectionTimer = Timings.ofSafe("Connection Handler");

  public static final Timing entityActivationCheckTimer = Timings.ofSafe("entityActivationCheck");

  public static final Timing midTickChunkTasks = Timings.ofSafe("Mid Tick Chunk Tasks");

  public static final Timing minecraftSchedulerTimer = Timings.ofSafe("Minecraft Scheduler");

  public static final Timing packetProcessTimer = Timings.ofSafe("## Packet Processing");

  public static final Timing playerCommandTimer = Timings.ofSafe("playerCommand");

  public static final Timing playerListTimer = Timings.ofSafe("Player List");

  public static final Timing processQueueTimer = Timings.ofSafe("processQueue");

  public static final Timing processTasksTimer = Timings.ofSafe("processTasks");

  public static final Timing savePlayers = Timings.ofSafe("Save Players");

  public static final Timing scheduledBlocksTimer = Timings.ofSafe("## Scheduled Blocks");

  public static final Timing serverCommandTimer = Timings.ofSafe("Server Command");

  public static final Timing serverOversleep = Timings.ofSafe("Server Oversleep");

  public static final Timing shirukaSchedulerFinishTimer = Timings.ofSafe("Shiru ka Scheduler - Finishing");

  public static final Timing shirukaSchedulerPendingTimer = Timings.ofSafe("Shiru ka Scheduler - Pending");

  public static final Timing shirukaSchedulerTimer = Timings.ofSafe("Shiru ka Scheduler");

  public static final Timing structureGenerationTimer = Timings.ofSafe("Structure Generation");

  public static final Timing tickEntityTimer = Timings.ofSafe("## tickEntity");

  public static final Timing tickTileEntityTimer = Timings.ofSafe("## tickTileEntity");

  public static final Timing tickablesTimer = Timings.ofSafe("Tickables");

  public static final Timing timeUpdateTimer = Timings.ofSafe("Time Update");

  private static final Map<Class<?>, String> taskNameCache = new MapMaker().weakKeys().makeMap();

  private MinecraftTimings() {
  }

  public static Timing getBlockTiming(final Block block) {
    return Timings.ofSafe("## Scheduled Block: " + block.toString(), MinecraftTimings.scheduledBlocksTimer);
  }

  public static Timing getCancelTasksTimer() {
    return Timings.ofSafe("Cancel Tasks");
  }

  public static Timing getCancelTasksTimer(final Plugin plugin) {
    return Timings.ofSafe(plugin, "Cancel Tasks");
  }

  /**
   * Get a named timer for the specified entity type to track type specific timings.
   */
  public static Timing getEntityTimings(final String entityType, final String type) {
    return Timings.ofSafe("Minecraft", "## tickEntity - " + entityType + " - " + type, MinecraftTimings.tickEntityTimer);
  }

  public static Timing getInternalTaskName(final String taskName) {
    return Timings.ofSafe(taskName);
  }

  public static Timing getPacketTiming(final Packet packet) {
    return Timings.ofSafe("## Packet - " + packet.getClass().getName(), MinecraftTimings.packetProcessTimer);
  }

  /**
   * Gets a timer associated with a plugins tasks.
   */
  public static Timing getPluginTaskTimings(final ShirukaTask task, final long period) {
    if (!task.isSync()) {
      return NullTimingHandler.NULL;
    }
    final Plugin plugin;
    final var taskClass = task.getJob() != null
      ? task.getJob().getClass()
      : null;
    if (task.getOwner() != null) {
      plugin = task.getOwner();
    } else {
      plugin = TimingsManager.getPluginByClassloader(taskClass);
    }
    final var taskName = MinecraftTimings.taskNameCache.computeIfAbsent(taskClass, clazz -> {
      try {
        String clsName = !clazz.isMemberClass()
          ? clazz.getName()
          : clazz.getCanonicalName();
        if (clsName != null && clsName.contains("$Lambda$")) {
          clsName = clsName.replaceAll("(Lambda\\$.*?)/.*", "$1");
        }
        return clsName != null ? clsName : "UnknownTask";
      } catch (final Throwable ex) {
        new Exception("Error occurred detecting class name", ex).printStackTrace();
        return "MangledClassFile";
      }
    });
    final var name = new StringBuilder(64)
      .append("Task: ")
      .append(taskName);
    if (period > 0) {
      name.append(" (interval:")
        .append(period)
        .append(")");
    } else {
      name.append(" (Single)");
    }
    if (plugin == null) {
      return Timings.ofSafe(null, name.toString());
    }
    return Timings.ofSafe(plugin, name.toString());
  }

  public static void stopServer() {
    TimingsManager.stopServer();
  }
//  @todo #1:15m Implement getTileEntityTimings method.
//  /**
//   * Get a named timer for the specified tile entity type to track type specific timings.
//   */
//  public static Timing getTileEntityTimings(final TileEntity entity) {
//    final String entityType = entity.getClass().getName();
//    return Timings.ofSafe("Minecraft", "## tickTileEntity - " + entityType, MinecraftTimings.tickTileEntityTimer);
//  }
//  @todo #1:15m Implement getCommandFunctionTiming method.
//  public static Timing getCommandFunctionTiming(final CustomFunction function) {
//    return Timings.ofSafe("Command Function - " + function.getMinecraftKey().toString());
//  }
}
