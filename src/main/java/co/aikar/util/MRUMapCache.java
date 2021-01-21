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
package co.aikar.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implements a Most Recently Used cache in front of a backing map, to quickly access the last accessed result.
 *
 * @param <K> Key Type of the Map
 * @param <V> Value Type of the Map
 */
public class MRUMapCache<K, V> extends AbstractMap<K, V> {

  final Map<K, V> backingMap;

  Object cacheKey;

  V cacheValue;

  public MRUMapCache(@NotNull final Map<K, V> backingMap) {
    this.backingMap = backingMap;
  }

  /**
   * Wraps the specified map with a most recently used cache
   *
   * @param map Map to be wrapped
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> of(@NotNull final Map<K, V> map) {
    return new MRUMapCache<K, V>(map);
  }

  @Override
  public int size() {
    return this.backingMap.size();
  }

  @Override
  public boolean isEmpty() {
    return this.backingMap.isEmpty();
  }

  @Override
  public boolean containsValue(@Nullable final Object value) {
    return value != null && value == this.cacheValue || this.backingMap.containsValue(value);
  }

  @Override
  public boolean containsKey(@Nullable final Object key) {
    return key != null && key.equals(this.cacheKey) || this.backingMap.containsKey(key);
  }

  @Override
  @Nullable
  public V get(@Nullable final Object key) {
    if (this.cacheKey != null && this.cacheKey.equals(key)) {
      return this.cacheValue;
    }
    this.cacheKey = key;
    return this.cacheValue = this.backingMap.get(key);
  }

  @Override
  @Nullable
  public V put(@Nullable final K key, @Nullable final V value) {
    this.cacheKey = key;
    return this.cacheValue = this.backingMap.put(key, value);
  }

  @Override
  @Nullable
  public V remove(@Nullable final Object key) {
    if (key != null && key.equals(this.cacheKey)) {
      this.cacheKey = null;
    }
    return this.backingMap.remove(key);
  }

  @Override
  public void putAll(@NotNull final Map<? extends K, ? extends V> m) {
    this.backingMap.putAll(m);
  }

  @Override
  public void clear() {
    this.cacheKey = null;
    this.cacheValue = null;
    this.backingMap.clear();
  }

  @Override
  @NotNull
  public Set<K> keySet() {
    return this.backingMap.keySet();
  }

  @Override
  @NotNull
  public Collection<V> values() {
    return this.backingMap.values();
  }

  @Override
  @NotNull
  public Set<Entry<K, V>> entrySet() {
    return this.backingMap.entrySet();
  }
}
