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

import io.github.shiruka.shiruka.nbt.compound.CompoundTagBasic;
import io.github.shiruka.shiruka.nbt.list.ListTagBasic;
import io.github.shiruka.shiruka.nbt.primitive.NumberTagEnvelope;
import io.github.shiruka.shiruka.nbt.primitive.StringTag;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine named binary tag.
 */
public interface Tag {

  /**
   * an empty {@link CompoundTag} instance.
   */
  CompoundTag EMPTY_COMPOUND = Tag.createCompound();

  /**
   * an empty {@link ListTag} instance.
   */
  ListTag EMPTY_LIST = Tag.createList();

  /**
   * an empty {@link NumberTag} instance.
   */
  NumberTag EMPTY_NUMBER = Tag.createNumber(0);

  /**
   * an empty {@link PrimitiveTag} instance.
   */
  PrimitiveTag<String> EMPTY_STRING = Tag.createString("");

  /**
   * creates an instance of {@link CompoundTag}.
   *
   * @return an instance of {@link CompoundTag}.
   */
  @NotNull
  static CompoundTag createCompound() {
    return new CompoundTagBasic();
  }

  /**
   * creates an instance of {@link CompoundTag}.
   *
   * @param original the original map.
   *
   * @return an instance of {@link CompoundTag}.
   */
  @NotNull
  static CompoundTag createCompound(@NotNull final Map<String, Tag> original) {
    return new CompoundTagBasic(original);
  }

  /**
   * creates an instance of {@link ListTag}.
   *
   * @return an instance of {@link ListTag}.
   */
  @NotNull
  static ListTag createList() {
    return new ListTagBasic();
  }

  /**
   * creates an instance of {@link ListTag}.
   *
   * @param original the original list.
   *
   * @return an instance of {@link ListTag}.
   */
  @NotNull
  static ListTag createList(@NotNull final Tag... original) {
    return new ListTagBasic(original);
  }

  /**
   * creates an instance of {@link ListTag}.
   *
   * @param original the original list.
   *
   * @return an instance of {@link ListTag}.
   */
  @NotNull
  static ListTag createList(@NotNull final List<Tag> original) {
    return new ListTagBasic(original);
  }

  /**
   * creates an instance of {@link NumberTag}.
   *
   * @param original the original number.
   *
   * @return an instance of {@link NumberTag}.
   */
  @NotNull
  static NumberTag createNumber(@NotNull final Number original) {
    return new NumberTagEnvelope(original);
  }

  /**
   * creates an instance of {@link StringTag}.
   *
   * @param original the original number.
   *
   * @return an instance of {@link StringTag}.
   */
  @NotNull
  static StringTag createString(@NotNull final String original) {
    return new StringTag(original);
  }

  /**
   * checks if {@code this} is a {@link CompoundTag}.
   *
   * @return {@code true} if {@code this} is a {@link CompoundTag}.
   */
  default boolean isCompound() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link ListTag}.
   *
   * @return {@code true} if {@code this} is a {@link ListTag}.
   */
  default boolean isList() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link NumberTag}.
   *
   * @return {@code true} if {@code this} is a {@link NumberTag}.
   */
  default boolean isNumber() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link PrimitiveTag}.
   *
   * @return {@code true} if {@code this} is a {@link PrimitiveTag}.
   */
  default boolean isPrimitive() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link StringTag}.
   *
   * @return {@code true} if {@code this} is a {@link StringTag}.
   */
  default boolean isString() {
    return false;
  }

  /**
   * an instance of {@code this} as a {@link CompoundTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link CompoundTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link CompoundTag}.
   */
  @NotNull
  default CompoundTag asCompound() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a CompoundTag!");
  }

  /**
   * an instance of {@code this} as a {@link ListTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link ListTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link ListTag}.
   */
  @NotNull
  default ListTag asList() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a ListTag!");
  }

  /**
   * an instance of {@code this} as a {@link NumberTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link NumberTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link NumberTag}.
   */
  @NotNull
  default NumberTag asNumber() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a NumberTag!");
  }

  /**
   * an instance of {@code this} as a {@link PrimitiveTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link PrimitiveTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link PrimitiveTag}.
   */
  @NotNull
  default PrimitiveTag<?> asPrimitive() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a PrimitiveTag!");
  }

  /**
   * an instance of {@code this} as a {@link StringTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link StringTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link StringTag}.
   */
  @NotNull
  default StringTag asString() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a StringTag!");
  }
}
