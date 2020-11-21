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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NbtMap extends AbstractMap<String, Object> {

  public static final NbtMap EMPTY = new NbtMap();

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private static final long[] EMPTY_LONG_ARRAY = new long[0];

  private final LinkedHashMap<String, Object> map;

  @Nullable
  private transient Set<String> keySet;

  @Nullable
  private transient Set<Entry<String, Object>> entrySet;

  @Nullable
  private transient Collection<Object> values;

  private transient boolean hashCodeGenerated;

  private transient int hashCode;

  private NbtMap() {
    this.map = new LinkedHashMap<>();
  }

  NbtMap(@NotNull final Map<String, Object> map) {
    this.map = new LinkedHashMap<>(map);
  }

  NbtMap(@NotNull final LinkedHashMap<String, Object> map) {
    this.map = map;
  }

  @NotNull
  public static NbtMapBuilder builder() {
    return new NbtMapBuilder();
  }

  @NotNull
  static String mapToString(@NotNull final Map<String, Object> map) {
    final var i = map.entrySet().iterator();
    if (!i.hasNext()) {
      return "{}";
    }
    final StringBuilder sb = new StringBuilder();
    sb.append('{').append('\n');
    for (; ; ) {
      final Entry<String, Object> e = i.next();
      final String key = e.getKey();
      final String value = Nbt.toString(e.getValue());
      final String string = Nbt.indent("\"" + key + "\": " + value);
      sb.append(string);
      if (!i.hasNext()) {
        return sb.append('\n').append('}').toString();
      }
      sb.append(',').append('\n');
    }
  }

  @NotNull
  public NbtMapBuilder toBuilder() {
    return NbtMapBuilder.from(this);
  }

  public boolean containsKey(@NotNull final String key, @NotNull final NbtType<?> type) {
    final var o = this.map.get(key);
    return o != null && o.getClass() == type.getTagClass();
  }

  @Nullable
  @Override
  public Object get(final Object key) {
    return Nbt.copy(this.map.get(key));
  }

  @NotNull
  @Override
  public Set<String> keySet() {
    if (this.keySet == null) {
      this.keySet = Collections.unmodifiableSet(this.map.keySet());
    }
    return Collections.unmodifiableSet(this.keySet);
  }

  @NotNull
  @Override
  public Collection<Object> values() {
    if (this.values == null) {
      this.values = Collections.unmodifiableCollection(this.map.values());
    }
    return Collections.unmodifiableCollection(this.values);
  }

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    if (this.entrySet == null) {
      this.entrySet = Collections.unmodifiableSet(this.map.entrySet());
    }
    return Collections.unmodifiableSet(this.entrySet);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Map)) {
      return false;
    }
    final Map<?, ?> m = (Map<?, ?>) o;
    if (m.size() != this.size()) {
      return false;
    }
    try {
      for (final Entry<String, Object> e : this.entrySet()) {
        final String key = e.getKey();
        final Object value = e.getValue();
        if (value == null) {
          if (!(m.get(key) == null && m.containsKey(key))) {
            return false;
          }
        } else {
          if (!Objects.deepEquals(value, m.get(key))) {
            return false;
          }
        }
      }
    } catch (final ClassCastException | NullPointerException unused) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (this.hashCodeGenerated) {
      return this.hashCode;
    }
    int h = 0;
    for (final Entry<String, Object> stringObjectEntry : this.map.entrySet()) {
      h += stringObjectEntry.hashCode();
    }
    this.hashCode = h;
    this.hashCodeGenerated = true;
    return h;
  }

  @Override
  public String toString() {
    return NbtMap.mapToString(this.map);
  }

  public boolean getBoolean(final String key) {
    return this.getBoolean(key, false);
  }

  public boolean getBoolean(final String key, final boolean defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Byte) {
      return (byte) tag != 0;
    }
    return defaultValue;
  }

  public void listenForBoolean(@NotNull final String key, @NotNull final Consumer<Boolean> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Byte) {
      consumer.accept((byte) tag != 0);
    }
  }

  public byte getByte(@NotNull final String key) {
    return this.getByte(key, (byte) 0);
  }

  public byte getByte(@NotNull final String key, final byte defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Byte) {
      return (byte) tag;
    }
    return defaultValue;
  }

  public void listenForByte(@NotNull final String key, @NotNull final Consumer<Byte> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Byte) {
      consumer.accept((byte) tag);
    }
  }

  public short getShort(@NotNull final String key) {
    return this.getShort(key, (short) 0);
  }

  public short getShort(@NotNull final String key, final short defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Short) {
      return (short) tag;
    }
    return defaultValue;
  }

  public void listenForShort(@NotNull final String key, @NotNull final Consumer<Short> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Short) {
      consumer.accept((short) tag);
    }
  }

  public int getInt(@NotNull final String key) {
    return this.getInt(key, 0);
  }

  public int getInt(@NotNull final String key, final int defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Integer) {
      return (int) tag;
    }
    return defaultValue;
  }

  public void listenForInt(@NotNull final String key, @NotNull final IntConsumer consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Integer) {
      consumer.accept((int) tag);
    }
  }

  public long getLong(@NotNull final String key) {
    return this.getLong(key, 0L);
  }

  public long getLong(@NotNull final String key, final long defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Long) {
      return (long) tag;
    }
    return defaultValue;
  }

  public void listenForLong(@NotNull final String key, @NotNull final LongConsumer consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Long) {
      consumer.accept((long) tag);
    }
  }

  public float getFloat(@NotNull final String key) {
    return this.getFloat(key, 0F);
  }

  public float getFloat(@NotNull final String key, final float defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Float) {
      return (float) tag;
    }
    return defaultValue;
  }

  public void listenForFloat(@NotNull final String key, @NotNull final Consumer<Float> consumer) {
    final Object tag = this.map.get(key);
    if (tag instanceof Float) {
      consumer.accept((float) tag);
    }
  }

  public double getDouble(@NotNull final String key) {
    return this.getDouble(key, 0D);
  }

  public double getDouble(@NotNull final String key, final double defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Double) {
      return (double) tag;
    }
    return defaultValue;
  }

  public void listenForDouble(@NotNull final String key, @NotNull final DoubleConsumer consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Double) {
      consumer.accept((double) tag);
    }
  }

  @Nullable
  public String getString(@NotNull final String key) {
    return this.getString(key, "");
  }

  @Nullable
  public String getString(@NotNull final String key, @Nullable final String defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof String) {
      return (String) tag;
    }
    return defaultValue;
  }

  public void listenForString(@NotNull final String key, @NotNull final Consumer<String> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof String) {
      consumer.accept((String) tag);
    }
  }

  @Nullable
  public byte[] getByteArray(@NotNull final String key) {
    return this.getByteArray(key, NbtMap.EMPTY_BYTE_ARRAY);
  }

  @Nullable
  public byte[] getByteArray(@NotNull final String key, @Nullable final byte[] defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof byte[]) {
      final var bytes = (byte[]) tag;
      return Arrays.copyOf(bytes, bytes.length);
    }
    return defaultValue;
  }

  public void listenForByteArray(@NotNull final String key, @NotNull final Consumer<byte[]> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof byte[]) {
      final var bytes = (byte[]) tag;
      consumer.accept(Arrays.copyOf(bytes, bytes.length));
    }
  }

  @Nullable
  public int[] getIntArray(@NotNull final String key) {
    return this.getIntArray(key, NbtMap.EMPTY_INT_ARRAY);
  }

  @Nullable
  public int[] getIntArray(@NotNull final String key, @Nullable final int[] defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof int[]) {
      final var ints = (int[]) tag;
      return Arrays.copyOf(ints, ints.length);
    }
    return defaultValue;
  }

  public void listenForIntArray(@NotNull final String key, @NotNull final Consumer<int[]> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof int[]) {
      final var ints = (int[]) tag;
      consumer.accept(Arrays.copyOf(ints, ints.length));
    }
  }

  @Nullable
  public long[] getLongArray(@NotNull final String key) {
    return this.getLongArray(key, NbtMap.EMPTY_LONG_ARRAY);
  }

  @Nullable
  public long[] getLongArray(@NotNull final String key, @Nullable final long[] defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof long[]) {
      final var longs = (long[]) tag;
      return Arrays.copyOf(longs, longs.length);
    }
    return defaultValue;
  }

  public void listenForLongArray(@NotNull final String key, @NotNull final Consumer<long[]> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof long[]) {
      final long[] longs = (long[]) tag;
      consumer.accept(Arrays.copyOf(longs, longs.length));
    }
  }

  @Nullable
  public NbtMap getCompound(@NotNull final String key) {
    return this.getCompound(key, NbtMap.EMPTY);
  }

  @Nullable
  public NbtMap getCompound(@NotNull final String key, @Nullable final NbtMap defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof NbtMap) {
      return (NbtMap) tag;
    }
    return defaultValue;
  }

  public void listenForCompound(@NotNull final String key, @NotNull final Consumer<NbtMap> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof NbtMap) {
      consumer.accept((NbtMap) tag);
    }
  }

  @Nullable
  public <T> List<T> getList(@NotNull final String key, @NotNull final NbtType<T> type) {
    return this.getList(key, type, Collections.emptyList());
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T> List<T> getList(@NotNull final String key, @NotNull final NbtType<T> type,
                             @Nullable final List<T> defaultValue) {
    final Object tag = this.map.get(key);
    if (tag instanceof NbtList && ((NbtList<?>) tag).getType() == type) {
      return (NbtList<T>) tag;
    }
    return defaultValue;
  }

  @SuppressWarnings("unchecked")
  public <T> void listenForList(@NotNull final String key, @NotNull final NbtType<T> type,
                                @NotNull final Consumer<List<T>> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof NbtList<?> && ((NbtList<?>) tag).getType() == type) {
      consumer.accept((NbtList<T>) tag);
    }
  }

  @Nullable
  public Number getNumber(@NotNull final String key) {
    return this.getNumber(key, 0);
  }

  @Nullable
  public Number getNumber(@NotNull final String key, @Nullable final Number defaultValue) {
    final var tag = this.map.get(key);
    if (tag instanceof Number) {
      return (Number) tag;
    }
    return defaultValue;
  }

  public void listenForNumber(@NotNull final String key, @NotNull final Consumer<Number> consumer) {
    final var tag = this.map.get(key);
    if (tag instanceof Number) {
      consumer.accept((Number) tag);
    }
  }
}
