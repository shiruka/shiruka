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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NbtType<T> {

  public static final NbtType<Void> END = new NbtType<>(Void.class, Type.END);

  public static final NbtType<Byte> BYTE = new NbtType<>(Byte.class, Type.BYTE);

  public static final NbtType<Short> SHORT = new NbtType<>(Short.class, Type.SHORT);

  public static final NbtType<Integer> INT = new NbtType<>(Integer.class, Type.INT);

  public static final NbtType<Long> LONG = new NbtType<>(Long.class, Type.LONG);

  public static final NbtType<Float> FLOAT = new NbtType<>(Float.class, Type.FLOAT);

  public static final NbtType<Double> DOUBLE = new NbtType<>(Double.class, Type.DOUBLE);

  public static final NbtType<byte[]> BYTE_ARRAY = new NbtType<>(byte[].class, Type.BYTE_ARRAY);

  public static final NbtType<String> STRING = new NbtType<>(String.class, Type.STRING);

  @SuppressWarnings("rawtypes")
  public static final NbtType<NbtList> LIST = new NbtType<>(NbtList.class, Type.LIST);

  public static final NbtType<NbtMap> COMPOUND = new NbtType<>(NbtMap.class, Type.COMPOUND);

  public static final NbtType<int[]> INT_ARRAY = new NbtType<>(int[].class, Type.INT_ARRAY);

  public static final NbtType<long[]> LONG_ARRAY = new NbtType<>(long[].class, Type.LONG_ARRAY);

  private static final NbtType<?>[] BY_ID = {
    NbtType.END, NbtType.BYTE, NbtType.SHORT, NbtType.INT, NbtType.LONG, NbtType.FLOAT, NbtType.DOUBLE,
    NbtType.BYTE_ARRAY, NbtType.STRING, NbtType.LIST, NbtType.COMPOUND, NbtType.INT_ARRAY, NbtType.LONG_ARRAY
  };

  private static final Map<Class<?>, NbtType<?>> BY_CLASS = new HashMap<>();

  static {
    for (final NbtType<?> type : NbtType.BY_ID) {
      NbtType.BY_CLASS.put(type.getTagClass(), type);
    }
  }

  private final Class<T> tagClass;

  private final Type enumeration;

  private NbtType(final Class<T> tagClass, final Type enumeration) {
    this.tagClass = tagClass;
    this.enumeration = enumeration;
  }

  public static NbtType<?> byId(final int id) {
    if (id < 0 || id >= NbtType.BY_ID.length) {
      throw new IndexOutOfBoundsException("Tag type id must be greater than 0 and less than " + (NbtType.BY_ID.length - 1));
    }
    return NbtType.BY_ID[id];
  }

  @SuppressWarnings("unchecked")
  public static <T> NbtType<T> byClass(final Class<T> tagClass) {
    return Objects.requireNonNull((NbtType<T>) NbtType.BY_CLASS.get(tagClass),
      "Tag of class " + tagClass + " does not exist");
  }

  public Class<T> getTagClass() {
    return this.tagClass;
  }

  public int getId() {
    return this.enumeration.ordinal();
  }

  public String getTypeName() {
    return this.enumeration.getName();
  }

  public Type getEnum() {
    return this.enumeration;
  }

  public enum Type {
    END,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BYTE_ARRAY,
    STRING,
    LIST,
    COMPOUND,
    INT_ARRAY,
    LONG_ARRAY;

    private final String name;

    Type() {
      this.name = "TAG_" + this.name();
    }

    public String getName() {
      return this.name;
    }
  }
}
