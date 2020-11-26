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

package io.github.shiruka.shiruka.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine stored tags.
 *
 * @param <K> type of key.
 */
public interface StoredTag<K> {

  /**
   * gets the string from the tag store.
   *
   * @param key the key to get.
   *
   * @return a string instance from the tag store.
   */
  @Nullable
  default String getString(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isString()) {
      return null;
    }
    return tag.asString()
      .value();
  }

  /**
   * sets the string to the tag store.
   *
   * @param key the key to set.
   * @param tag the tag to set.
   */
  default void setString(@NotNull final K key, @NotNull final String tag) {
    this.set(key, Tag.createString(tag));
  }

  /**
   * gets the integer from the tag store.
   *
   * @param key the key to get.
   *
   * @return a integer instance from the tag store.
   */
  @Nullable
  default Integer getInteger(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isInt()) {
      return null;
    }
    return tag.asInt()
      .intValue();
  }

  /**
   * sets the integer to the tag store.
   *
   * @param key the key to set.
   * @param tag the tag to set.
   */
  default void setInteger(@NotNull final K key, @NotNull final Integer tag) {
    this.set(key, Tag.createInt(tag));
  }

  /**
   * gets the tag at the given key.
   *
   * @param key the key to get.
   *
   * @return the tag.
   */
  @Nullable
  Tag get(@NotNull K key);

  /**
   * sets the given tag into the given key.
   *
   * @param key the key to set.
   * @param tag the tag to set.
   */
  void set(@NotNull K key, @NotNull Tag tag);

  /**
   * removes the tag at the given key.
   *
   * @param key the key to remove.
   */
  void remove(@NotNull K key);

  /**
   * checks if the stored tag has the given key.
   *
   * @param key the key to check.
   *
   * @return {@code true} if the stored tag has the given key.
   */
  boolean containsKey(@NotNull K key);

  /**
   * checks if the stored tag has the given tag.
   *
   * @param tag the tag to check.
   *
   * @return {@code true} if the stored tag has the given tag.
   */
  boolean contains(@NotNull Tag tag);

  /**
   * gets the size.
   *
   * @return the size.
   */
  int size();
}
