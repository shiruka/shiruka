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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used as a basis for fast HashMap key comparisons for the Timing Map.
 * <p>
 * This class uses interned strings giving us the ability to do an identity check instead of equals() on the strings
 */
final class TimingIdentifier {

  /**
   * Holds all groups. auto-loads on request for a group by name.
   */
  static final Map<String, TimingGroup> GROUP_MAP = LoadingMap.of(new ConcurrentHashMap<>(64, .5F), TimingGroup::new);

  private static final TimingGroup DEFAULT_GROUP = TimingIdentifier.getGroup("Minecraft");

  final String group;

  final TimingHandler groupHandler;

  final String name;

  private final int hashCode;

  TimingIdentifier(@Nullable final String group, @NotNull final String name, @Nullable final Timing groupHandler) {
    this.group = group != null ? group : TimingIdentifier.DEFAULT_GROUP.name;
    this.name = name;
    this.groupHandler = groupHandler != null ? groupHandler.getTimingHandler() : null;
    this.hashCode = 31 * this.group.hashCode() + this.name.hashCode();
  }

  @NotNull
  static TimingGroup getGroup(@Nullable final String groupName) {
    if (groupName == null) {
      //noinspection ConstantConditions
      return TimingIdentifier.DEFAULT_GROUP;
    }
    return TimingIdentifier.GROUP_MAP.get(groupName);
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    final TimingIdentifier that = (TimingIdentifier) obj;
    return Objects.equals(this.group, that.group) && Objects.equals(this.name, that.name);
  }

  @Override
  public String toString() {
    return "TimingIdentifier{id=" + this.group + ":" + this.name + '}';
  }

  static class TimingGroup {

    private static final AtomicInteger idPool = new AtomicInteger(1);

    final List<TimingHandler> handlers = Collections.synchronizedList(new ArrayList<>(64));

    final int id = TimingGroup.idPool.getAndIncrement();

    final String name;

    private TimingGroup(final String name) {
      this.name = name;
    }

    @Override
    public int hashCode() {
      return this.id;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || this.getClass() != obj.getClass()) {
        return false;
      }
      final TimingGroup that = (TimingGroup) obj;
      return this.id == that.id;
    }
  }
}
