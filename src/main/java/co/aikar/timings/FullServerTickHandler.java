package co.aikar.timings;

import static co.aikar.timings.TimingsManager.*;
import net.shiruka.api.Shiruka;
import org.jetbrains.annotations.NotNull;

public class FullServerTickHandler extends TimingHandler {

  private static final TimingIdentifier IDENTITY = new TimingIdentifier("Minecraft", "Full Server Tick", null);

  final TimingData minuteData;

  double avgFreeMemory = -1D;

  double avgUsedMemory = -1D;

  FullServerTickHandler() {
    super(FullServerTickHandler.IDENTITY);
    this.minuteData = new TimingData(this.id);
    TIMING_MAP.put(FullServerTickHandler.IDENTITY, this);
  }

  @NotNull
  @Override
  public Timing startTiming() {
    if (TimingsManager.needsFullReset) {
      TimingsManager.resetTimings();
    } else if (TimingsManager.needsRecheckEnabled) {
      TimingsManager.recheckEnabled();
    }
    return super.startTiming();
  }

  @Override
  public void stopTiming() {
    super.stopTiming();
    if (!this.isEnabled() || Shiruka.isStopping()) {
      return;
    }
    if (TimingHistory.timedTicks % 20 == 0) {
      final var runtime = Runtime.getRuntime();
      final var usedMemory = runtime.totalMemory() - runtime.freeMemory();
      final var freeMemory = runtime.maxMemory() - usedMemory;
      if (this.avgFreeMemory == -1) {
        this.avgFreeMemory = freeMemory;
      } else {
        this.avgFreeMemory = this.avgFreeMemory * (59 / 60D) + freeMemory * (1 / 60D);
      }
      if (this.avgUsedMemory == -1) {
        this.avgUsedMemory = usedMemory;
      } else {
        this.avgUsedMemory = this.avgUsedMemory * (59 / 60D) + usedMemory * (1 / 60D);
      }
    }
    final var start = System.nanoTime();
    TimingsManager.tick();
    final var diff = System.nanoTime() - start;
    TIMINGS_TICK.addDiff(diff, null);
    // addDiff for TIMINGS_TICK incremented this, bring it back down to 1 per tick.
    this.record.setCurTickCount(this.record.getCurTickCount() - 1);
    this.minuteData.setCurTickTotal(this.record.getCurTickTotal());
    this.minuteData.setCurTickCount(1);
    final var violated = this.isViolated();
    this.minuteData.processTick(violated);
    TIMINGS_TICK.processTick(violated);
    this.processTick(violated);
    if (TimingHistory.timedTicks % 1200 == 0) {
      MINUTE_REPORTS.add(new TimingHistory.MinuteReport());
      TimingHistory.resetTicks(false);
      this.minuteData.reset();
    }
    if (TimingHistory.timedTicks % Timings.getHistoryInterval() == 0) {
      TimingsManager.HISTORY.add(new TimingHistory());
      TimingsManager.resetTimings();
    }
    TimingsExport.reportTimings();
  }

  boolean isViolated() {
    return this.record.getCurTickTotal() > 50000000;
  }
}
