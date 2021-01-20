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

import static co.aikar.util.JSONUtil.toArray;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight object for tracking timing data
 * <p>
 * This is broken out to reduce memory usage
 */
class TimingData {

  private final int id;

  private int count = 0;

  private int curTickCount = 0;

  private long curTickTotal = 0;

  private int lagCount = 0;

  private long lagTotalTime = 0;

  private long totalTime = 0;

  TimingData(final int id) {
    this.id = id;
  }

  private TimingData(final TimingData data) {
    this.id = data.id;
    this.totalTime = data.totalTime;
    this.lagTotalTime = data.lagTotalTime;
    this.count = data.count;
    this.lagCount = data.lagCount;
  }

  @Override
  protected TimingData clone() {
    return new TimingData(this);
  }

  void add(final long diff) {
    ++this.curTickCount;
    this.curTickTotal += diff;
  }

  @NotNull
  List<Object> export() {
    final var list = toArray(
      this.id,
      this.count,
      this.totalTime);
    if (this.lagCount > 0) {
      list.add(this.lagCount);
      list.add(this.lagTotalTime);
    }
    return list;
  }

  int getCurTickCount() {
    return this.curTickCount;
  }

  void setCurTickCount(final int curTickCount) {
    this.curTickCount = curTickCount;
  }

  long getCurTickTotal() {
    return this.curTickTotal;
  }

  void setCurTickTotal(final long curTickTotal) {
    this.curTickTotal = curTickTotal;
  }

  long getTotalTime() {
    return this.totalTime;
  }

  boolean hasData() {
    return this.count > 0;
  }

  void processTick(final boolean violated) {
    this.totalTime += this.curTickTotal;
    this.count += this.curTickCount;
    if (violated) {
      this.lagTotalTime += this.curTickTotal;
      this.lagCount += this.curTickCount;
    }
    this.curTickTotal = 0;
    this.curTickCount = 0;
  }

  void reset() {
    this.count = 0;
    this.lagCount = 0;
    this.curTickTotal = 0;
    this.curTickCount = 0;
    this.totalTime = 0;
    this.lagTotalTime = 0;
  }
}
