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

package net.shiruka.shiruka.nbt.compound;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link CompoundTag}.
 */
public final class CompoundTagBasic implements CompoundTag {

  /**
   * the original.
   */
  @NotNull
  private final Map<String, Tag> original;

  /**
   * ctor.
   *
   * @param original the original map.
   */
  public CompoundTagBasic(@NotNull final Map<String, Tag> original) {
    this.original = new Object2ObjectOpenHashMap<>(original);
  }

  /**
   * ctor.
   */
  public CompoundTagBasic() {
    this(new Object2ObjectOpenHashMap<>());
  }

  @NotNull
  @Override
  public Map<String, Tag> all() {
    return Collections.unmodifiableMap(this.original);
  }

  @Override
  public boolean hasKeyOfType(@NotNull final String key, final byte id) {
    final byte type = this.get(key).map(Tag::id).orElse((byte) 0);
    return id == type || id == 99 && this.isNumber(type);
  }

  @Override
  public boolean contains(@NotNull final Tag tag) {
    return this.original.containsValue(tag);
  }

  @Override
  public boolean containsKey(@NotNull final String key) {
    return this.original.containsKey(key);
  }

  @NotNull
  @Override
  public Optional<Tag> get(@NotNull final String key) {
    return Optional.ofNullable(this.original.get(key));
  }

  @Override
  public void remove(@NotNull final String key) {
    this.original.remove(key);
  }

  @Override
  public void set(@NotNull final String key, @NotNull final Tag tag) {
    this.original.put(key, tag);
  }

  @Override
  public int size() {
    return this.original.size();
  }

  @Override
  public String toString() {
    return this.original.toString();
  }

  /**
   * checks if the given {@code type} is a number.
   *
   * @param type the type to check.
   *
   * @return {@code true} if the type is a number.
   */
  private boolean isNumber(final byte type) {
    return type == 1 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6;
  }
}
