/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
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

package io.github.shiruka.shiruka.world.options;

import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.WorldBorder;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;

/**
 * the implementation of a world border which can be enabled on a world.
 *
 * @todo #1:5m Add more JavaDoc here.
 */
public final class ShirukaWorldBorder implements WorldBorder {

  /**
   * the center.
   */
  private final ThreadLocal<DoubleXZ> center = ThreadLocal.withInitial(() -> WorldBorder.DEFAULT_CENTER);

  private final AtomicLong damage = new AtomicLong(Double.doubleToLongBits(WorldBorder.DEFAULT_DAMAGE));

  private final AtomicLong safeZoneDistance = new AtomicLong(Double.doubleToLongBits(WorldBorder.DEFAULT_SAFE_AND_WARN_DIST));

  private final AtomicLong size = new AtomicLong(Double.doubleToLongBits(WorldBorder.DEFAULT_SIZE));

  private final AtomicLong sizeTime = new AtomicLong(0);

  private final AtomicLong targetSize = new AtomicLong(Double.doubleToLongBits(WorldBorder.DEFAULT_SIZE));

  private final AtomicInteger warn = new AtomicInteger(WorldBorder.DEFAULT_SAFE_AND_WARN_DIST);

  private final AtomicInteger warnTime = new AtomicInteger(WorldBorder.DEFAULT_WARN_TIME);

  /**
   * the world which contains this world border
   */
  @NotNull
  private final World world;

  /**
   * ctor.
   *
   * @param world the world.
   */
  @NotNull
  public ShirukaWorldBorder(@NotNull final World world) {
    this.world = world;
  }

  @NotNull
  @Override
  public WorldBorder.DoubleXZ getCenter() {
    return this.center.get();
  }

  @Override
  public void setCenter(@NotNull final DoubleXZ center) {
    this.center.set(center);
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetCenter(center.getX(), center.getZ()));
  }

  @Override
  public double getDamage() {
    return Double.longBitsToDouble(this.damage.get());
  }

  @Override
  public void setDamage(final double damage) {
    this.damage.set(Double.doubleToLongBits(damage));
  }

  @Override
  public double getSafeZoneDistance() {
    return Double.longBitsToDouble(this.safeZoneDistance.get());
  }

  @Override
  public void setSafeZoneDistance(final int size) {
    this.safeZoneDistance.set(Double.doubleToLongBits(size));
  }

  @Override
  public double getSize() {
    return Double.longBitsToDouble(this.size.get());
  }

  @Override
  public double getTargetSize() {
    return Double.longBitsToDouble(this.targetSize.get());
  }

  @Override
  public long getTargetTime() {
    return this.sizeTime.get();
  }

  @Override
  public int getWarnDistance() {
    return this.warn.get();
  }

  @Override
  public void setWarnDistance(final int dist) {
    this.warn.set(dist);
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetWarnBlocks(dist));
  }

  @Override
  public int getWarnTime() {
    return this.warnTime.get();
  }

  @Override
  public void setWarnTime(final int seconds) {
    this.warnTime.set(seconds);
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetWarnTime(seconds));
  }

  @Override
  public void grow(final double delta, final long time) {
    this.sizeTime.set(time);
    long oldSize;
    long newSize;
    double currentSize;
    double nextSize;
    do {
      oldSize = this.targetSize.get();
      currentSize = Double.longBitsToDouble(oldSize);
      nextSize = currentSize + delta;
      newSize = Double.doubleToLongBits(nextSize);
    }
    while (!this.targetSize.compareAndSet(oldSize, newSize));
    if (time == 0) {
      // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetSize(nextSize));
    } else {
      // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.LerpSize(currentSize, nextSize, time));
    }
  }

  @Override
  public void growWarnDistance(final int dist) {
    int oldWarn;
    int newWarn;
    do {
      oldWarn = this.warn.get();
      newWarn = oldWarn + dist;
    }
    while (!this.warn.compareAndSet(oldWarn, newWarn));
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetWarnBlocks(newWarn));
  }

  @Override
  public void init() {
    final DoubleXZ xz = this.center.get();
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.Init(xz.getX(), xz.getZ(), WorldBorder.DEFAULT_SIZE, this.getSize(), this.getTargetTime(), this.warnTime.get(), this.warn.get()));
  }

  @Override
  public void setSize(final double size, final long time) {
    this.sizeTime.set(time);
    this.targetSize.set(Double.doubleToLongBits(size));
    if (time == 0) {
      // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.SetSize(size));
    } else {
      // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutWorldBorder.LerpSize(Double.longBitsToDouble(this.size.get()), size, time));
    }
  }

  public void read(@NotNull final CompoundTag compound) {
    this.center.set(new DoubleXZ(compound.getDouble("BorderCenterX").orElseThrow(),
      compound.getDouble("BorderCenterZ").orElseThrow()));
    this.size.set(Double.doubleToLongBits(compound.getDouble("BorderSize").orElseThrow()));
    this.targetSize.set(Double.doubleToLongBits(compound.getDouble("BorderSizeLerpTarget").orElseThrow()));
    this.sizeTime.set(compound.getLong("BorderSizeLerpTime").orElseThrow());
    this.damage.set(Double.doubleToLongBits(compound.getDouble("BorderDamagePerBlock").orElseThrow()));
    this.safeZoneDistance.set(Double.doubleToLongBits(compound.getDouble("BorderSafeZone").orElseThrow()));
    this.warn.set(compound.getDouble("BorderWarningBlocks").orElseThrow().intValue());
    this.warnTime.set(compound.getDouble("BorderWarningTime").orElseThrow().intValue());
  }

  @Override
  public void tick() {
    long prevTime;
    long nextTime;
    long oldSize;
    double newSize;
    do {
      final double target = Double.longBitsToDouble(this.targetSize.get());
      prevTime = this.sizeTime.get();
      oldSize = this.size.get();
      newSize = Double.longBitsToDouble(oldSize);
      if (Double.compare(newSize, target) == 0) {
        break;
      }
      long period = prevTime;
      if (prevTime == 0) {
        period = 50;
      }
      final double diff = target - newSize;
      final long ticksUntilDone = period / 50;
      final double delta = diff / ticksUntilDone;
      newSize = Math.max(0, newSize + delta);
      nextTime = Math.max(0, prevTime - 50);
    }
    while (!this.sizeTime.compareAndSet(prevTime, nextTime) || !this.size.compareAndSet(oldSize, Double.doubleToLongBits(newSize)));
  }

  public void write(@NotNull final CompoundTag compound) {
    compound.setDouble("BorderCenterX", this.center.get().getX());
    compound.setDouble("BorderCenterZ", this.center.get().getZ());
    compound.setDouble("BorderSize", this.getSize());
    compound.setDouble("BorderSizeLerpTarget", this.getTargetSize());
    compound.setLong("BorderSizeLerpTime", this.getTargetTime());
    compound.setDouble("BorderDamagePerBlock", this.getDamage());
    compound.setDouble("BorderSafeZone", this.getSafeZoneDistance());
    compound.setDouble("BorderWarningBlocks", (double) this.getWarnDistance());
    compound.setDouble("BorderWarningTime", (double) this.getWarnTime());
  }
}
