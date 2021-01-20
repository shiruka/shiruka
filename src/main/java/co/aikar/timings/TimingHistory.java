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

import static co.aikar.timings.TimingsManager.FULL_SERVER_TICK;
import static co.aikar.timings.TimingsManager.MINUTE_REPORTS;
import static co.aikar.util.JSONUtil.*;
import co.aikar.util.JSONUtil;
import co.aikar.util.LoadingMap;
import co.aikar.util.MRUMapCache;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import java.util.*;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.Material;
import net.shiruka.api.entity.EntityType;
import net.shiruka.api.world.World;
import java.lang.management.ManagementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"SuppressionAnnotation", "Convert2Lambda", "Anonymous2MethodRef"})
public class TimingHistory {

  public static long activatedEntityTicks;

  public static long entityTicks;

  public static long lastMinuteTime;

  public static long playerTicks;

  public static long tileEntityTicks;

  public static long timedTicks;

  private static int worldIdPool = 1;

  static Map<String, Integer> worldMap = LoadingMap.newHashMap(new Function<>() {
    @NotNull
    @Override
    public Integer apply(@Nullable final String input) {
      return TimingHistory.worldIdPool++;
    }
  });

  final Set<EntityType> entityTypeSet = Sets.newHashSet();

  final Set<Material> tileEntityTypeSet = Sets.newHashSet();

  private final long endTime;

  private final TimingHistoryEntry[] entries;

  private final MinuteReport[] minuteReports;

  private final long startTime;

  private final long totalTicks;

  private final long totalTime; // Represents all time spent running the server this history

  private final Map<Object, Object> worlds;

  TimingHistory() {
    this.endTime = System.currentTimeMillis() / 1000;
    this.startTime = TimingsManager.historyStart / 1000;
    if (TimingHistory.timedTicks % 1200 != 0 || MINUTE_REPORTS.isEmpty()) {
      this.minuteReports = MINUTE_REPORTS.toArray(new MinuteReport[MINUTE_REPORTS.size() + 1]);
      this.minuteReports[this.minuteReports.length - 1] = new MinuteReport();
    } else {
      this.minuteReports = MINUTE_REPORTS.toArray(new MinuteReport[0]);
    }
    var ticks = 0;
    for (final var mp : this.minuteReports) {
      ticks += mp.ticksRecord.timed;
    }
    this.totalTicks = ticks;
    this.totalTime = FULL_SERVER_TICK.record.getTotalTime();
    this.entries = new TimingHistoryEntry[TimingsManager.HANDLERS.size()];
    int i = 0;
    for (final var handler : TimingsManager.HANDLERS) {
      this.entries[i++] = new TimingHistoryEntry(handler);
    }
    // Information about all loaded chunks/entities
    this.worlds = new HashMap<>();
//    @todo #1:60m Implement world for timings 1.
//    //noinspection unchecked
//    this.worlds = toObjectMapper(Shiruka.getWorlds(), new Function<World, JSONUtil.JSONPair>() {
//      @NotNull
//      @Override
//      public JSONUtil.JSONPair apply(final World world) {
//        final var regions = LoadingMap.newHashMap(RegionData.LOADER);
//        for (final var chunk : world.getLoadedChunks()) {
//          final var data = regions.get(new TimingHistory.RegionData.RegionId(chunk.getX(), chunk.getZ()));
//          for (final var entity : chunk.getEntities()) {
//            if (entity == null) {
//              Shiruka.getLogger().warn("Null entity detected in chunk at position x: {}, z: {}", chunk.getX(), chunk.getZ());
//              continue;
//            }
//            data.entityCounts.get(entity.getType()).increment();
//          }
//          for (final var tileEntity : chunk.getTileEntities(false)) {
//            if (tileEntity == null) {
//              Shiruka.getLogger().warn("Null tile entity detected in chunk at position x: {}, z: {]", chunk.getX(), chunk.getZ());
//              continue;
//            }
//            data.tileEntityCounts.get(tileEntity.getBlock().getType()).increment();
//          }
//        }
//        return pair(
//          TimingHistory.worldMap.get(world.getName()),
//          toArrayMapper(regions.values(), new Function<>() {
//            @NotNull
//            @Override
//            public Object apply(final RegionData input) {
//              return toArray(
//                input.regionId.x,
//                input.regionId.z,
//                toObjectMapper(input.entityCounts.entrySet(),
//                  new Function<>() {
//                    @NotNull
//                    @Override
//                    public JSONUtil.JSONPair apply(final Map.Entry<EntityType, Counter> entry) {
//                      TimingHistory.this.entityTypeSet.add(entry.getKey());
//                      return pair(
//                        String.valueOf(entry.getKey().ordinal()),
//                        entry.getValue().count());
//                    }
//                  }),
//                toObjectMapper(input.tileEntityCounts.entrySet(),
//                  new Function<>() {
//                    @NotNull
//                    @Override
//                    public JSONUtil.JSONPair apply(final Map.Entry<Material, Counter> entry) {
//                      TimingHistory.this.tileEntityTypeSet.add(entry.getKey());
//                      return pair(
//                        String.valueOf(entry.getKey().ordinal()),
//                        entry.getValue().count());
//                    }
//                  }));
//            }
//          })
//        );
//      }
//    });
  }

  static void resetTicks(final boolean fullReset) {
    if (fullReset) {
      // Non full is simply for 1 minute reports
      TimingHistory.timedTicks = 0;
    }
    TimingHistory.lastMinuteTime = System.nanoTime();
    TimingHistory.playerTicks = 0;
    TimingHistory.tileEntityTicks = 0;
    TimingHistory.entityTicks = 0;
    TimingHistory.activatedEntityTicks = 0;
  }

  @NotNull
  Object export() {
    return createObject(
      pair("s", this.startTime),
      pair("e", this.endTime),
      pair("tk", this.totalTicks),
      pair("tm", this.totalTime),
      pair("w", this.worlds),
      pair("h", toArrayMapper(this.entries, new Function<>() {
        @Nullable
        @Override
        public Object apply(final TimingHistoryEntry entry) {
          final TimingData record = entry.data;
          if (!record.hasData()) {
            return null;
          }
          return entry.export();
        }
      })),
      pair("mp", toArrayMapper(this.minuteReports, new Function<>() {
        @NotNull
        @Override
        public Object apply(final MinuteReport input) {
          return input.export();
        }
      }))
    );
  }

  private static class Counter {

    private int count = 0;

    public int count() {
      return this.count;
    }

    public int increment() {
      return ++this.count;
    }
  }

  static class MinuteReport {

    final double freeMemory = TimingsManager.FULL_SERVER_TICK.avgFreeMemory;

    final TimingData fst = TimingsManager.FULL_SERVER_TICK.minuteData.clone();

    final double loadAvg = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

    final PingRecord pingRecord = new PingRecord();

    final TicksRecord ticksRecord = new TicksRecord();

    final long time = System.currentTimeMillis() / 1000;

    final double tps = 1E9 / (System.nanoTime() - TimingHistory.lastMinuteTime) * this.ticksRecord.timed;

    final double usedMemory = TimingsManager.FULL_SERVER_TICK.avgUsedMemory;

    @NotNull
    List<Object> export() {
      return toArray(
        this.time,
        Math.round(this.tps * 100D) / 100D,
        Math.round(this.pingRecord.avg * 100D) / 100D,
        this.fst.export(),
        toArray(this.ticksRecord.timed,
          this.ticksRecord.player,
          this.ticksRecord.entity,
          this.ticksRecord.activatedEntity,
          this.ticksRecord.tileEntity
        ),
        this.usedMemory,
        this.freeMemory,
        this.loadAvg
      );
    }
  }

  private static class PingRecord {

    final double avg;

    PingRecord() {
      final var onlinePlayers = Shiruka.getOnlinePlayers();
      var totalPing = 0L;
      for (final var player : onlinePlayers) {
        totalPing += player.getPing();
      }
      this.avg = onlinePlayers.isEmpty() ? 0 : totalPing / onlinePlayers.size();
    }
  }

  static class RegionData {

    @SuppressWarnings("Guava")
    static Function<TimingHistory.RegionData.RegionId, RegionData> LOADER = new Function<>() {
      @NotNull
      @Override
      public RegionData apply(@NotNull final TimingHistory.RegionData.RegionId id) {
        return new RegionData(id);
      }
    };

    @SuppressWarnings("unchecked")
    final Map<EntityType, Counter> entityCounts = MRUMapCache.of(LoadingMap.of(
      new EnumMap<>(EntityType.class), k -> new Counter()
    ));

    final TimingHistory.RegionData.RegionId regionId;

    @SuppressWarnings("unchecked")
    final Map<Material, Counter> tileEntityCounts = MRUMapCache.of(LoadingMap.of(
      new EnumMap<>(Material.class), k -> new Counter()
    ));

    RegionData(@NotNull final TimingHistory.RegionData.RegionId id) {
      this.regionId = id;
    }

    @Override
    public int hashCode() {
      return this.regionId.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || this.getClass() != obj.getClass()) {
        return false;
      }
      final RegionData that = (RegionData) obj;
      return this.regionId.equals(that.regionId);
    }

    static class RegionId {

      final long regionId;

      final int x, z;

      RegionId(final int x, final int z) {
        this.x = x >> 5 << 5;
        this.z = z >> 5 << 5;
        this.regionId = ((long) this.x << 32) + (this.z >> 5 << 5) - Integer.MIN_VALUE;
      }

      @Override
      public int hashCode() {
        return (int) (this.regionId ^ this.regionId >>> 32);
      }

      @Override
      public boolean equals(final Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
          return false;
        }
        final TimingHistory.RegionData.RegionId regionId1 = (TimingHistory.RegionData.RegionId) obj;
        return this.regionId == regionId1.regionId;
      }
    }
  }

  private static class TicksRecord {

    final long activatedEntity;

    final long entity;

    final long player;

    final long tileEntity;

    final long timed;

    TicksRecord() {
      this.timed = TimingHistory.timedTicks - TimingsManager.MINUTE_REPORTS.size() * 1200L;
      this.player = TimingHistory.playerTicks;
      this.entity = TimingHistory.entityTicks;
      this.tileEntity = TimingHistory.tileEntityTicks;
      this.activatedEntity = TimingHistory.activatedEntityTicks;
    }
  }
}
