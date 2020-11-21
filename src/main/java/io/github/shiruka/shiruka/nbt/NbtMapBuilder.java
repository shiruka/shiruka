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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class NbtMapBuilder extends LinkedHashMap<String, Object> {

  NbtMapBuilder() {
  }

  @NotNull
  public static NbtMapBuilder from(@NotNull final NbtMap map) {
    final var builder = new NbtMapBuilder();
    builder.putAll(map);
    return builder;
  }

  @Override
  public Object put(@NotNull final String key, @NotNull Object value) {
    if (value instanceof Boolean) {
      value = (byte) ((boolean) value ? 1 : 0);
    }
    NbtType.byClass(value.getClass());
    return super.put(key, Nbt.copy(value));
  }

  @NotNull
  public NbtMapBuilder putBoolean(@NotNull final String name, final boolean value) {
    this.put(name, (byte) (value ? 1 : 0));
    return this;
  }

  @NotNull
  public NbtMapBuilder putByte(@NotNull final String name, final byte value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putByteArray(@NotNull final String name, @NotNull final byte[] value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putDouble(@NotNull final String name, final double value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putFloat(@NotNull final String name, final float value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putIntArray(@NotNull final String name, @NotNull final int[] value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putLongArray(@NotNull final String name, @NotNull final long[] value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putInt(@NotNull final String name, final int value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putLong(@NotNull final String name, final long value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putShort(@NotNull final String name, final short value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putString(@NotNull final String name, @NotNull final String value) {
    this.put(name, value);
    return this;
  }

  @NotNull
  public NbtMapBuilder putCompound(@NotNull final String name, @NotNull final NbtMap value) {
    this.put(name, value);
    return this;
  }

  @SafeVarargs
  @NotNull
  public final <T> NbtMapBuilder putList(@NotNull final String name, @NotNull final NbtType<T> type,
                                         @NotNull final T... values) {
    this.put(name, new NbtList<>(type, values));
    return this;
  }

  @NotNull
  public <T> NbtMapBuilder putList(@NotNull final String name, @NotNull final NbtType<T> type, @NotNull List<T> list) {
    if (!(list instanceof NbtList)) {
      list = new NbtList<>(type, list);
    }
    this.put(name, list);
    return this;
  }

  @NotNull
  public NbtMapBuilder rename(@NotNull final String oldName, @NotNull final String newName) {
    final var o = this.remove(oldName);
    if (o != null) {
      this.put(newName, o);
    }
    return this;
  }

  public Map build() {
    if (this.isEmpty()) {
      return Collections.unmodifiableMap(NbtMap.EMPTY);
    }
    return new NbtMap((Map<String, Object>) this);
  }

  @Override
  public String toString() {
    return NbtMap.mapToString(this);
  }
}
