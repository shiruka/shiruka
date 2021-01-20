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

import co.aikar.util.LoadingIntMap;
import net.shiruka.api.Shiruka;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TimingHandler implements Timing {

  private static final Deque<TimingHandler> TIMING_STACK = new ArrayDeque<>();

  private static final AtomicInteger idPool = new AtomicInteger(1);

  final int id = TimingHandler.idPool.getAndIncrement();

  final TimingIdentifier identifier;

  final TimingData record;

  private final Int2ObjectOpenHashMap<TimingData> children = new LoadingIntMap<>(TimingData::new);

  private final TimingHandler groupHandler;

  private final boolean verbose;

  private boolean added;

  private boolean enabled;

  private long start = 0;

  private TimingHandler startParent;

  private boolean timed;

  private int timingDepth = 0;

  TimingHandler(@NotNull final TimingIdentifier id) {
    this.identifier = id;
    this.verbose = id.name.startsWith("##");
    this.record = new TimingData(this.id);
    this.groupHandler = id.groupHandler;
    TimingIdentifier.getGroup(id.group).handlers.add(this);
    this.checkEnabled();
  }

  /**
   * This is simply for the Closeable interface so it can be used with try-with-resources ()
   */
  @Override
  public void close() {
    this.stopTimingIfSync();
  }

  @NotNull
  @Override
  public TimingHandler getTimingHandler() {
    return this;
  }

  @Override
  @NotNull
  public Timing startTiming() {
    if (!this.enabled || !Shiruka.isPrimaryThread()) {
      return this;
    }
    if (++this.timingDepth == 1) {
      this.startParent = TimingHandler.TIMING_STACK.peekLast();
      this.start = System.nanoTime();
    }
    TimingHandler.TIMING_STACK.addLast(this);
    return this;
  }

  @NotNull
  @Override
  public Timing startTimingIfSync() {
    this.startTiming();
    return this;
  }

  @Override
  public void stopTiming() {
    if (!this.enabled || this.timingDepth <= 0 || this.start == 0 || !Shiruka.isPrimaryThread()) {
      return;
    }
    this.popTimingStack();
    if (--this.timingDepth == 0) {
      this.addDiff(System.nanoTime() - this.start, this.startParent);
      this.startParent = null;
      this.start = 0;
    }
  }

  @Override
  public void stopTimingIfSync() {
    this.stopTiming();
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public boolean isSpecial() {
    return this == TimingsManager.FULL_SERVER_TICK || this == TimingsManager.TIMINGS_TICK;
  }

  final void checkEnabled() {
    this.enabled = Timings.timingsEnabled && (!this.verbose || Timings.verboseEnabled);
  }

  void addDiff(final long diff, @Nullable final TimingHandler parent) {
    if (parent != null) {
      parent.children.get(this.id).add(diff);
    }
    this.record.add(diff);
    if (!this.added) {
      this.added = true;
      this.timed = true;
      TimingsManager.HANDLERS.add(this);
    }
    if (this.groupHandler != null) {
      this.groupHandler.addDiff(diff, parent);
      this.groupHandler.children.get(this.id).add(diff);
    }
  }

  @NotNull
  TimingData[] cloneChildren() {
    final var clonedChildren = new TimingData[this.children.size()];
    var i = 0;
    for (final var child : this.children.values()) {
      clonedChildren[i++] = child.clone();
    }
    return clonedChildren;
  }

  boolean isTimed() {
    return this.timed;
  }

  void processTick(final boolean violated) {
    if (this.timingDepth != 0 || this.record.getCurTickCount() == 0) {
      this.timingDepth = 0;
      this.start = 0;
      return;
    }
    this.record.processTick(violated);
    for (final var handler : this.children.values()) {
      handler.processTick(violated);
    }
  }

  /**
   * Reset this timer, setting all values to zero.
   */
  void reset(final boolean full) {
    this.record.reset();
    if (full) {
      this.timed = false;
    }
    this.start = 0;
    this.timingDepth = 0;
    this.added = false;
    this.children.clear();
    this.checkEnabled();
  }

  private void popTimingStack() {
    TimingHandler last;
    while ((last = TimingHandler.TIMING_STACK.removeLast()) != this) {
      last.timingDepth = 0;
      if ("Minecraft".equalsIgnoreCase(last.identifier.group)) {
        Logger.getGlobal().log(Level.SEVERE, "TIMING_STACK_CORRUPTION - Look above this for any errors and report this to Shiru ka unless it has a plugin in the stack trace (" + last.identifier + " did not stopTiming)");
      } else {
        Logger.getGlobal().log(Level.SEVERE, "TIMING_STACK_CORRUPTION - Report this to the plugin " + last.identifier.group + " (Look for errors above this in the logs) (" + last.identifier + " did not stopTiming)", new Throwable());
      }
      final var found = TimingHandler.TIMING_STACK.contains(this);
      if (!found) {
        // We aren't even in the stack... Don't pop everything
        TimingHandler.TIMING_STACK.addLast(last);
        break;
      }
    }
  }
}
