/*
 * Copyright (c) 2015. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */
package co.aikar.util;

import com.google.common.base.Function;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows you to pass a Loader function that when a key is accessed that doesn't exist,
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
 * @param <V> Value
 */
public class LoadingIntMap<V> extends Int2ObjectOpenHashMap<V> {

  private final Function<Integer, V> loader;

  public LoadingIntMap(@NotNull final Function<Integer, V> loader) {
    super();
    this.loader = loader;
  }

  public LoadingIntMap(final int expectedSize, @NotNull final Function<Integer, V> loader) {
    super(expectedSize);
    this.loader = loader;
  }

  public LoadingIntMap(final int expectedSize, final float loadFactor, @NotNull final Function<Integer, V> loader) {
    super(expectedSize, loadFactor);
    this.loader = loader;
  }

  @Nullable
  @Override
  public V get(final int k) {
    var res = super.get(k);
    if (res == null) {
      res = this.loader.apply(k);
      if (res != null) {
        this.put(k, res);
      }
    }
    return res;
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
