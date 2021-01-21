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

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows you to pass a Loader function that when a key is accessed that doesn't exists,
 * automatically loads the entry into the map by calling the loader Function.
 * <p>
 * .get() Will only return null if the Loader can return null.
 * <p>
 * You may pass any backing Map to use.
 * <p>
 * This class is not thread safe and should be wrapped with Collections.synchronizedMap on the OUTSIDE of the LoadingMap
 * if needed.
 * <p>
 * Do not wrap the backing map with Collections.synchronizedMap.
 *
 * @param <K> Key
 * @param <V> Value
 */
public class LoadingMap<K, V> extends AbstractMap<K, V> {

  private final Map<K, V> backingMap;

  private final Function<K, V> loader;

  /**
   * Initializes an auto loading map using specified loader and backing map
   *
   * @param backingMap Map to wrap
   * @param loader Loader
   */
  public LoadingMap(@NotNull final Map<K, V> backingMap, @NotNull final Function<K, V> loader) {
    this.backingMap = backingMap;
    this.loader = loader;
  }

  /**
   * Creates a LoadingMap with an auto instantiating loader.
   * <p>
   * Will auto construct class of of Value when not found
   * <p>
   * Since this uses Reflection, It is more effecient to define your own static loader
   * than using this helper, but if performance is not critical, this is easier.
   *
   * @param backingMap Actual map being used.
   * @param keyClass Class used for the K generic
   * @param valueClass Class used for the V generic
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   */
  @NotNull
  public static <K, V> Map<K, V> newAutoMap(@NotNull final Map<K, V> backingMap, @Nullable final Class<? extends K> keyClass,
                                            @NotNull final Class<? extends V> valueClass) {
    return new LoadingMap<>(backingMap, new AutoInstantiatingLoader<>(keyClass, valueClass));
  }

  /**
   * Creates a LoadingMap with an auto instantiating loader.
   * <p>
   * Will auto construct class of of Value when not found
   * <p>
   * Since this uses Reflection, It is more effecient to define your own static loader
   * than using this helper, but if performance is not critical, this is easier.
   *
   * @param backingMap Actual map being used.
   * @param valueClass Class used for the V generic
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   */
  @NotNull
  public static <K, V> Map<K, V> newAutoMap(@NotNull final Map<K, V> backingMap,
                                            @NotNull final Class<? extends V> valueClass) {
    return LoadingMap.newAutoMap(backingMap, null, valueClass);
  }

  /**
   * @param keyClass Class used for the K generic
   * @param valueClass Class used for the V generic
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   *
   * @see #newAutoMap
   *   <p>
   *   new Auto initializing map using a HashMap.
   */
  @NotNull
  public static <K, V> Map<K, V> newHashAutoMap(@Nullable final Class<? extends K> keyClass, @NotNull final Class<? extends V> valueClass) {
    return LoadingMap.newAutoMap(new HashMap<>(), keyClass, valueClass);
  }

  /**
   * @param valueClass Class used for the V generic
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   *
   * @see #newAutoMap
   *   <p>
   *   new Auto initializing map using a HashMap.
   */
  @NotNull
  public static <K, V> Map<K, V> newHashAutoMap(@NotNull final Class<? extends V> valueClass) {
    return LoadingMap.newHashAutoMap(null, valueClass);
  }

  /**
   * @param keyClass Class used for the K generic
   * @param valueClass Class used for the V generic
   * @param initialCapacity Initial capacity to use
   * @param loadFactor Load factor to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   *
   * @see #newAutoMap
   *   <p>
   *   new Auto initializing map using a HashMap.
   */
  @NotNull
  public static <K, V> Map<K, V> newHashAutoMap(@Nullable final Class<? extends K> keyClass, @NotNull final Class<? extends V> valueClass, final int initialCapacity, final float loadFactor) {
    return LoadingMap.newAutoMap(new HashMap<>(initialCapacity, loadFactor), keyClass, valueClass);
  }

  /**
   * @param valueClass Class used for the V generic
   * @param initialCapacity Initial capacity to use
   * @param loadFactor Load factor to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map that auto instantiates on .get()
   *
   * @see #newAutoMap
   *   <p>
   *   new Auto initializing map using a HashMap.
   */
  @NotNull
  public static <K, V> Map<K, V> newHashAutoMap(@NotNull final Class<? extends V> valueClass, final int initialCapacity, final float loadFactor) {
    return LoadingMap.newHashAutoMap(null, valueClass, initialCapacity, loadFactor);
  }

  /**
   * Initializes an auto loading map using a HashMap
   *
   * @param loader Loader to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> newHashMap(@NotNull final Function<K, V> loader) {
    return new LoadingMap<>(new HashMap<>(), loader);
  }

  /**
   * Initializes an auto loading map using a HashMap
   *
   * @param loader Loader to use
   * @param initialCapacity Initial capacity to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> newHashMap(@NotNull final Function<K, V> loader, final int initialCapacity) {
    return new LoadingMap<>(new HashMap<>(initialCapacity), loader);
  }

  /**
   * Initializes an auto loading map using a HashMap
   *
   * @param loader Loader to use
   * @param initialCapacity Initial capacity to use
   * @param loadFactor Load factor to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> newHashMap(@NotNull final Function<K, V> loader, final int initialCapacity, final float loadFactor) {
    return new LoadingMap<>(new HashMap<>(initialCapacity, loadFactor), loader);
  }

  /**
   * Initializes an auto loading map using an Identity HashMap
   *
   * @param loader Loader to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> newIdentityHashMap(@NotNull final Function<K, V> loader) {
    return new LoadingMap<>(new IdentityHashMap<>(), loader);
  }

  /**
   * Initializes an auto loading map using an Identity HashMap
   *
   * @param loader Loader to use
   * @param initialCapacity Initial capacity to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> newIdentityHashMap(@NotNull final Function<K, V> loader, final int initialCapacity) {
    return new LoadingMap<>(new IdentityHashMap<>(initialCapacity), loader);
  }

  /**
   * Creates a new LoadingMap with the specified map and loader
   *
   * @param backingMap Actual map being used.
   * @param loader Loader to use
   * @param <K> Key Type of the Map
   * @param <V> Value Type of the Map
   *
   * @return Map
   */
  @NotNull
  public static <K, V> Map<K, V> of(@NotNull final Map<K, V> backingMap, @NotNull final Function<K, V> loader) {
    return new LoadingMap<>(backingMap, loader);
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
    return this.backingMap.containsValue(value);
  }

  @Override
  public boolean containsKey(@Nullable final Object key) {
    return this.backingMap.containsKey(key);
  }

  @Nullable
  @Override
  public V get(@Nullable final Object key) {
    final var v = this.backingMap.get(key);
    if (v != null) {
      return v;
    }
    return this.backingMap.computeIfAbsent((K) key, this.loader);
  }

  @Override
  @Nullable
  public V put(@Nullable final K key, @Nullable final V value) {
    return this.backingMap.put(key, value);
  }

  @Nullable
  @Override
  public V remove(@Nullable final Object key) {
    return this.backingMap.remove(key);
  }

  @Override
  public void putAll(@NotNull final Map<? extends K, ? extends V> m) {
    this.backingMap.putAll(m);
  }

  @Override
  public void clear() {
    this.backingMap.clear();
  }

  @NotNull
  @Override
  public Set<K> keySet() {
    return this.backingMap.keySet();
  }

  @NotNull
  @Override
  public Collection<V> values() {
    return this.backingMap.values();
  }

  @NotNull
  @Override
  public Set<Entry<K, V>> entrySet() {
    return this.backingMap.entrySet();
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    return this.backingMap.equals(o);
  }

  @Override
  public int hashCode() {
    return this.backingMap.hashCode();
  }

  @Override
  @NotNull
  public LoadingMap<K, V> clone() {
    return new LoadingMap<>(this.backingMap, this.loader);
  }

  private static class AutoInstantiatingLoader<K, V> implements Function<K, V> {

    final Constructor<? extends V> constructor;

    private final Class<? extends V> valueClass;

    AutoInstantiatingLoader(@Nullable final Class<? extends K> keyClass, @NotNull final Class<? extends V> valueClass) {
      try {
        this.valueClass = valueClass;
        if (keyClass != null) {
          this.constructor = valueClass.getConstructor(keyClass);
        } else {
          this.constructor = null;
        }
      } catch (final NoSuchMethodException e) {
        throw new IllegalStateException(
          valueClass.getName() + " does not have a constructor for " + (keyClass != null ? keyClass.getName() : null));
      }
    }

    @NotNull
    @Override
    public V apply(@Nullable final K input) {
      try {
        return this.constructor != null ? this.constructor.newInstance(input) : this.valueClass.newInstance();
      } catch (final Exception e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      return false;
    }
  }

  /**
   * Due to java stuff, you will need to cast it to (Function) for some cases
   *
   * @param <T> Type
   */
  public abstract static class Feeder<T> implements Function<T, T> {

    @Nullable
    @Override
    public T apply(@Nullable final Object input) {
      return this.apply();
    }

    @Nullable
    public abstract T apply();
  }
}
