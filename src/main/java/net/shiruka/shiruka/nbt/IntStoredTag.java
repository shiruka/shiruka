/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
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

package net.shiruka.shiruka.nbt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.shiruka.shiruka.nbt.array.ByteArrayTag;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine primitive int stored tags.
 */
public interface IntStoredTag {

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
  default boolean containsKey(final int key) {
    return this.contains(Tag.createInt(key));
  }

  /**
   * gets the tag at the given key.
   *
   * @param key the key to get.
   *
   * @return the tag.
   */
  @NotNull
  Optional<Tag> get(int key);

  /**
   * gets the byte from the tag store.
   *
   * @param key the key to get.
   *
   * @return a byte instance from the tag store.
   */
  @NotNull
  default Optional<Byte> getByte(final int key) {
    return this.get(key)
      .filter(Tag::isByte)
      .map(Tag::asByte)
      .map(NumberTag::byteValue);
  }

  /**
   * gets the byte array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a byte array instance from the tag store.
   */
  @NotNull
  default Optional<Byte[]> getByteArray(final int key) {
    return this.get(key)
      .filter(Tag::isByteArray)
      .map(Tag::asByteArray)
      .map(ArrayTag::value);
  }

  /**
   * gets the compound tag from the tag store.
   *
   * @param key the key to get.
   *
   * @return a compound tag instance from the tag store.
   */
  @NotNull
  default Optional<CompoundTag> getCompoundTag(final int key) {
    return this.get(key)
      .filter(Tag::isCompound)
      .map(Tag::asCompound);
  }

  /**
   * gets the double from the tag store.
   *
   * @param key the key to get.
   *
   * @return a double instance from the tag store.
   */
  @NotNull
  default Optional<Double> getDouble(final int key) {
    return this.get(key)
      .filter(Tag::isDouble)
      .map(Tag::asDouble)
      .map(NumberTag::doubleValue);
  }

  /**
   * gets the float from the tag store.
   *
   * @param key the key to get.
   *
   * @return a float instance from the tag store.
   */
  @NotNull
  default Optional<Float> getFloat(final int key) {
    return this.get(key)
      .filter(Tag::isFloat)
      .map(Tag::asFloat)
      .map(NumberTag::floatValue);
  }

  /**
   * gets the int array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a int array instance from the tag store.
   */
  @NotNull
  default Optional<Integer[]> getIntArray(final int key) {
    return this.get(key)
      .filter(Tag::isIntArray)
      .map(Tag::asIntArray)
      .map(ArrayTag::value);
  }

  /**
   * gets the integer from the tag store.
   *
   * @param key the key to get.
   *
   * @return a integer instance from the tag store.
   */
  @NotNull
  default Optional<Integer> getInteger(final int key) {
    return this.get(key)
      .filter(Tag::isInt)
      .map(Tag::asInt)
      .map(NumberTag::intValue);
  }

  /**
   * gets the list from the tag store.
   *
   * @param key the key to get.
   *
   * @return a list instance from the tag store.
   */
  @NotNull
  default Optional<List<Tag>> getList(final int key) {
    return this.getListTag(key)
      .map(ListTag::all);
  }

  /**
   * gets the list from the tag store.
   *
   * @param key the key to get.
   * @param listType the list type to get.
   *
   * @return a list instance from the tag store.
   */
  @NotNull
  default Optional<List<Tag>> getList(final int key, @NotNull final TagTypes listType) {
    return this.getListTag(key, listType)
      .map(ListTag::all);
  }

  /**
   * gets the list tag from the tag store.
   *
   * @param key the key to get.
   *
   * @return a list tag instance from the tag store.
   */
  @NotNull
  default Optional<ListTag> getListTag(final int key) {
    return this.get(key)
      .filter(Tag::isList)
      .map(Tag::asList);
  }

  /**
   * gets the list tag from the tag store.
   *
   * @param key the key to get.
   * @param listType the list type to get.
   *
   * @return a list tag instance from the tag store.
   */
  @NotNull
  default Optional<ListTag> getListTag(final int key, @NotNull final TagTypes listType) {
    return this.get(key)
      .filter(Tag::isList)
      .map(Tag::asList)
      .filter(tags -> tags.getListType() == listType);
  }

  /**
   * gets the long from the tag store.
   *
   * @param key the key to get.
   *
   * @return a long instance from the tag store.
   */
  @NotNull
  default Optional<Long> getLong(final int key) {
    return this.get(key)
      .filter(Tag::isLong)
      .map(Tag::asLong)
      .map(NumberTag::longValue);
  }

  /**
   * gets the long array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a long array instance from the tag store.
   */
  @NotNull
  default Optional<Long[]> getLongArray(final int key) {
    return this.get(key)
      .filter(Tag::isLongArray)
      .map(Tag::asLongArray)
      .map(ArrayTag::value);
  }

  /**
   * gets the map from the tag store.
   *
   * @param key the key to get.
   *
   * @return a map instance from the tag store.
   */
  @NotNull
  default Optional<Map<String, Tag>> getMap(final int key) {
    return this.getCompoundTag(key)
      .map(CompoundTag::all);
  }

  /**
   * gets the primitive byte array from the tag store.
   *
   * @param key the key to get.
   *
   * @return a primitive byte array instance from the tag store.
   */
  @NotNull
  default Optional<byte[]> getPrimitiveByteArray(final int key) {
    return this.get(key)
      .filter(Tag::isByteArray)
      .map(Tag::asByteArray)
      .map(ByteArrayTag::primitiveValue);
  }

  /**
   * gets the short from the tag store.
   *
   * @param key the key to get.
   *
   * @return a short instance from the tag store.
   */
  @NotNull
  default Optional<Short> getShort(final int key) {
    return this.get(key)
      .filter(Tag::isShort)
      .map(Tag::asShort)
      .map(NumberTag::shortValue);
  }

  /**
   * gets the string from the tag store.
   *
   * @param key the key to get.
   *
   * @return a string instance from the tag store.
   */
  @NotNull
  default Optional<String> getString(final int key) {
    return this.get(key)
      .filter(Tag::isString)
      .map(Tag::asString)
      .map(PrimitiveTag::value);
  }

  /**
   * checks if the stored tag has not any element in it.
   *
   * @return {@code true} if the stored tag has not any element in it.
   */
  boolean isEmpty();

  /**
   * removes the tag at the given key.
   *
   * @param key the key to remove.
   */
  void remove(int key);

  /**
   * sets the given tag into the given key.
   *
   * @param key the key to set.
   * @param tag the tag to set.
   */
  void set(int key, @NotNull Tag tag);

  /**
   * sets the byte to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setByte(final int key, @NotNull final Byte value) {
    this.set(key, Tag.createByte(value));
  }

  /**
   * sets the byte array to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setByteArray(final int key, final byte... value) {
    this.set(key, Tag.createByteArray(value));
  }

  /**
   * sets the double to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setDouble(final int key, @NotNull final Double value) {
    this.set(key, Tag.createDouble(value));
  }

  /**
   * sets the float to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setFloat(final int key, @NotNull final Float value) {
    this.set(key, Tag.createFloat(value));
  }

  /**
   * sets the int array to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setIntArray(final int key, final int... value) {
    this.set(key, Tag.createIntArray(value));
  }

  /**
   * sets the integer to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setInteger(final int key, @NotNull final Integer value) {
    this.set(key, Tag.createInt(value));
  }

  /**
   * sets the list to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setList(final int key, @NotNull final List<Tag> value) {
    this.set(key, Tag.createList(value));
  }

  /**
   * sets the long to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setLong(final int key, @NotNull final Long value) {
    this.set(key, Tag.createLong(value));
  }

  /**
   * sets the long array to the tag store.
   *
   * @param key the key to set.
   * @param value the value set.
   */
  default void setLongArray(final int key, final long @NotNull [] value) {
    this.set(key, Tag.createLongArray(value));
  }

  /**
   * sets the map to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setMap(final int key, @NotNull final Map<String, Tag> value) {
    this.set(key, Tag.createCompound(value));
  }

  /**
   * sets the short to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setShort(final int key, @NotNull final Short value) {
    this.set(key, Tag.createShort(value));
  }

  /**
   * sets the string to the tag store.
   *
   * @param key the key to set.
   * @param value the value to set.
   */
  default void setString(final int key, @NotNull final String value) {
    this.set(key, Tag.createString(value));
  }

  /**
   * gets the size.
   *
   * @return the size.
   */
  int size();
}
