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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine stored tags.
 *
 * @param <K> type of key.
 */
public interface StoredTag<K> {

  /**
   * checks if the stored tag has the given tag.
   *
   * @param tag the tag to check.
   *
   * @return {@code true} if the stored tag has the given tag.
   */
  boolean contains(@NotNull Tag tag);

  /**
   * checks if the stored tag has the given key.
   *
   * @param key the key to check.
   *
   * @return {@code true} if the stored tag has the given key.
   */
  boolean containsKey(@NotNull K key);

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
   * gets the byte from the tag store.
   *
   * @param key the key to get.
   *
   * @return a byte instance from the tag store.
   */
  @Nullable
  default Byte getByte(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isByte()) {
      return null;
    }
    return tag.asByte()
      .byteValue();
  }

  /**
   * gets the byte array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a byte array instance from the tag store.
   */
  @NotNull
  default Byte[] getByteArray(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isByteArray()) {
      return new Byte[0];
    }
    return tag.asByteArray()
      .value();
  }

  /**
   * gets the double from the tag store.
   *
   * @param key the key to get.
   *
   * @return a double instance from the tag store.
   */
  @Nullable
  default Double getDouble(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isDouble()) {
      return null;
    }
    return tag.asDouble()
      .doubleValue();
  }

  /**
   * gets the float from the tag store.
   *
   * @param key the key to get.
   *
   * @return a float instance from the tag store.
   */
  @Nullable
  default Float getFloat(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isFloat()) {
      return null;
    }
    return tag.asFloat()
      .floatValue();
  }

  /**
   * gets the int array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a int array instance from the tag store.
   */
  @NotNull
  default Integer[] getIntArray(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isIntArray()) {
      return new Integer[0];
    }
    return tag.asIntArray()
      .value();
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
   * gets the list from the tag store.
   *
   * @param key the key to get.
   *
   * @return a list instance from the tag store.
   */
  @NotNull
  default List<Tag> getList(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isList()) {
      return Collections.emptyList();
    }
    return tag.asList()
      .all();
  }

  /**
   * gets the long from the tag store.
   *
   * @param key the key to get.
   *
   * @return a long instance from the tag store.
   */
  @Nullable
  default Long getLong(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isLong()) {
      return null;
    }
    return tag.asLong()
      .longValue();
  }

  /**
   * gets the long array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a long array instance from the tag store.
   */
  @NotNull
  default Long[] getLongArray(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isLongArray()) {
      return new Long[0];
    }
    return tag.asLongArray()
      .value();
  }

  /**
   * gets the map from the tag store.
   *
   * @param key the key to get.
   *
   * @return a map instance from the tag store.
   */
  @NotNull
  default Map<String, Tag> getMap(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isCompound()) {
      return Collections.emptyMap();
    }
    return tag.asCompound()
      .all();
  }

  /**
   * gets the short from the tag store.
   *
   * @param key the key to get.
   *
   * @return a short instance from the tag store.
   */
  @Nullable
  default Short getShort(@NotNull final K key) {
    final var tag = this.get(key);
    if (tag == null || !tag.isShort()) {
      return null;
    }
    return tag.asShort()
      .shortValue();
  }

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
   * removes the tag at the given key.
   *
   * @param key the key to remove.
   */
  void remove(@NotNull K key);

  /**
   * sets the given tag into the given key.
   *
   * @param key the key to set.
   * @param tag the tag to set.
   */
  void set(@NotNull K key, @NotNull Tag tag);

  /**
   * sets the byte to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setByte(@NotNull final K key, @NotNull final Byte value) {
    this.set(key, Tag.createByte(value));
  }

  /**
   * sets the byte array to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setByteArray(@NotNull final K key, @NotNull final Byte @NotNull [] value) {
    this.set(key, Tag.createByteArray(value));
  }

  /**
   * sets the double to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setDouble(@NotNull final K key, @NotNull final Double value) {
    this.set(key, Tag.createDouble(value));
  }

  /**
   * sets the float to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setFloat(@NotNull final K key, @NotNull final Float value) {
    this.set(key, Tag.createFloat(value));
  }

  /**
   * sets the int array to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setIntArray(@NotNull final K key, @NotNull final Integer @NotNull [] value) {
    this.set(key, Tag.createIntArray(value));
  }

  /**
   * sets the integer to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setInteger(@NotNull final K key, @NotNull final Integer value) {
    this.set(key, Tag.createInt(value));
  }

  /**
   * sets the list to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setList(@NotNull final K key, @NotNull final List<Tag> value) {
    this.set(key, Tag.createList(value));
  }

  /**
   * sets the long to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setLong(@NotNull final K key, @NotNull final Long value) {
    this.set(key, Tag.createLong(value));
  }

  /**
   * sets the long array to the tag store.
   *
   * @param key the key to set.
   * @param value the value set.
   */
  default void setLongArray(@NotNull final K key, @NotNull final Long @NotNull [] value) {
    this.set(key, Tag.createLongArray(value));
  }

  /**
   * sets the map to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setMap(@NotNull final K key, @NotNull final Map<String, Tag> value) {
    this.set(key, Tag.createCompound(value));
  }

  /**
   * sets the short to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setShort(@NotNull final K key, @NotNull final Short value) {
    this.set(key, Tag.createShort(value));
  }

  /**
   * sets the string to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setString(@NotNull final K key, @NotNull final String value) {
    this.set(key, Tag.createString(value));
  }

  /**
   * gets the size.
   *
   * @return the size.
   */
  int size();
}
