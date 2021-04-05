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

import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine compound tags which contain map of {@link Tag}.
 */
public interface CompoundTag extends Tag, StoredTag<String> {

  /**
   * checks if the given {@code type} is a number.
   *
   * @param type the type to check.
   *
   * @return {@code true} if the type is a number.
   */
  private static boolean isNumber(@NotNull final TagTypes type) {
    return type == TagTypes.BYTE ||
      type == TagTypes.SHORT ||
      type == TagTypes.INT ||
      type == TagTypes.LONG ||
      type == TagTypes.FLOAT ||
      type == TagTypes.DOUBLE;
  }

  /**
   * obtains compound tag's map as unmodifiable..
   *
   * @return compound tag's map.
   */
  @NotNull
  Map<String, Tag> all();

  @NotNull
  @Override
  default CompoundTag asCompound() {
    return this;
  }

  @NotNull
  @Override
  default TagTypes getType() {
    return TagTypes.COMPOUND;
  }

  @Override
  default boolean isCompound() {
    return true;
  }

  /**
   * checks if the given {@code key} contains and the id of the key's value equals the given {@code id}.
   *
   * @param key the key to check.
   * @param id the id to check.
   *
   * @return {@code true} if the id of the key's value equals the given {@code id}.
   */
  default boolean hasKeyOfType(@NotNull final String key, @NotNull final TagTypes id) {
    final var type = this.get(key).map(Tag::getType).orElse(TagTypes.END);
    return id == type || id.getId() == 99 && CompoundTag.isNumber(type);
  }

  @Override
  default boolean isEmpty() {
    return this.all().isEmpty();
  }
}
