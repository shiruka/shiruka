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

import io.github.shiruka.shiruka.nbt.array.ByteArrayTag;
import io.github.shiruka.shiruka.nbt.array.IntArrayTag;
import io.github.shiruka.shiruka.nbt.array.LongArrayTag;
import io.github.shiruka.shiruka.nbt.compound.CompoundTagBasic;
import io.github.shiruka.shiruka.nbt.list.ListTagBasic;
import io.github.shiruka.shiruka.nbt.primitive.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine named binary tag.
 */
public interface Tag {

  /**
   * an end tag instance.
   */
  Tag END = new Tag() {
    @Override
    public byte id() {
      return 0;
    }

    @Override
    public void write(@NotNull final DataOutput output) {
    }
  };

  /**
   * an empty {@link CompoundTag} instance.
   */
  CompoundTag COMPOUND = Tag.createCompound();

  /**
   * an empty {@link ListTag} instance.
   */
  ListTag LIST = Tag.createList();

  /**
   * an empty {@link ByteArrayTag} instance.
   */
  ArrayTag<Byte> BYTE_ARRAY = Tag.createByteArray(new Byte[0]);

  /**
   * an empty {@link IntArrayTag} instance.
   */
  ArrayTag<Integer> INT_ARRAY = Tag.createIntArray(new Integer[0]);

  /**
   * an empty {@link LongArrayTag} instance.
   */
  ArrayTag<Long> LONG_ARRAY = Tag.createLongArray(new Long[0]);

  /**
   * an empty {@link ByteTag} instance.
   */
  ByteTag BYTE = Tag.createByte((byte) 0);

  /**
   * an empty {@link DoubleTag} instance.
   */
  DoubleTag DOUBLE = Tag.createDouble(0.0d);

  /**
   * an empty {@link FloatTag} instance.
   */
  FloatTag FLOAT = Tag.createFloat(0.0f);

  /**
   * an empty {@link IntTag} instance.
   */
  IntTag INT = Tag.createInt(0);

  /**
   * an empty {@link LongTag} instance.
   */
  LongTag LONG = Tag.createLong(0L);

  /**
   * an empty {@link ShortTag} instance.
   */
  ShortTag SHORT = Tag.createShort((short) 0);

  /**
   * an empty {@link PrimitiveTag} instance.
   */
  PrimitiveTag<String> STRING = Tag.createString("");

  /**
   * reads the given input using the id.
   *
   * @param id the id to read.
   * @param input the input read.
   *
   * @return a new tag instance depends on the given id.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @Nullable
  static Tag read(final byte id, @NotNull final DataInput input) throws IOException {
    switch (id) {
      case 1:
        return Tag.readByte(input);
      case 2:
        return Tag.readShort(input);
      case 3:
        return Tag.readInt(input);
      case 4:
        return Tag.readLong(input);
      case 5:
        return Tag.readFloat(input);
      case 6:
        return Tag.readDouble(input);
      case 7:
        return Tag.readByteArray(input);
      case 8:
        return Tag.readString(input);
      case 9:
        return Tag.readListTag(input);
      case 10:
        return Tag.readCompoundTag(input);
      case 11:
        return Tag.readIntArray(input);
      case 12:
        return Tag.readLongArray(input);
      case 0:
      default:
        return null;
    }
  }

  /**
   * reads the given input and converts it into the {@link CompoundTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link CompoundTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static CompoundTag readCompoundTag(@NotNull final DataInput input) throws IOException {
    final var tags = new HashMap<String, Tag>();
    byte id;
    while ((id = input.readByte()) != Tag.END.id()) {
      final var key = input.readUTF();
      final var tag = Tag.read(id, input);
      if (tag != null) {
        tags.put(key, tag);
      }
    }
    return Tag.createCompound(tags);
  }

  /**
   * reads the given input and converts it into the {@link ListTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link ListTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static ListTag readListTag(@NotNull final DataInput input) throws IOException {
    final var id = input.readByte();
    final var length = input.readInt();
    final var tags = new ArrayList<Tag>(length);
    for (int i = 0; i < length; i++) {
      final var read = Tag.read(id, input);
      if (read != null) {
        tags.add(read);
      }
    }
    return Tag.createList(tags);
  }

  /**
   * reads the given input and converts it into the {@link ByteArrayTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link ByteArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static ByteArrayTag readByteArray(@NotNull final DataInput input) throws IOException {
    final var length = input.readInt();
    final var value = new byte[length];
    input.readFully(value);
    return Tag.createByteArray(ArrayUtils.toObject(value));
  }

  /**
   * reads the given input and converts it into the {@link IntArrayTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link IntArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static IntArrayTag readIntArray(@NotNull final DataInput input) throws IOException {
    final var length = input.readInt();
    final var value = new Integer[length];
    for (int i = 0; i < length; i++) {
      value[i] = input.readInt();
    }
    return Tag.createIntArray(value);
  }

  /**
   * reads the given input and converts it into the {@link LongArrayTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link LongArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static LongArrayTag readLongArray(@NotNull final DataInput input) throws IOException {
    final var length = input.readInt();
    final var value = new Long[length];
    for (int i = 0; i < length; i++) {
      value[i] = input.readLong();
    }
    return Tag.createLongArray(value);
  }

  /**
   * reads the given input and converts it into the {@link ByteTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link ByteTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static ByteTag readByte(@NotNull final DataInput input) throws IOException {
    return Tag.createByte(input.readByte());
  }

  /**
   * reads the given input and converts it into the {@link DoubleTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link DoubleTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static DoubleTag readDouble(@NotNull final DataInput input) throws IOException {
    return Tag.createDouble(input.readDouble());
  }

  /**
   * reads the given input and converts it into the {@link FloatTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link FloatTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static FloatTag readFloat(@NotNull final DataInput input) throws IOException {
    return Tag.createFloat(input.readFloat());
  }

  /**
   * reads the given input and converts it into the {@link IntTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link IntTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static IntTag readInt(@NotNull final DataInput input) throws IOException {
    return Tag.createInt(input.readInt());
  }

  /**
   * reads the given input and converts it into the {@link LongTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link LongTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static LongTag readLong(@NotNull final DataInput input) throws IOException {
    return Tag.createLong(input.readLong());
  }

  /**
   * reads the given input and converts it into the {@link ShortTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link ShortTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static ShortTag readShort(@NotNull final DataInput input) throws IOException {
    return Tag.createShort(input.readShort());
  }

  /**
   * reads the given input and converts it into the {@link StringTag}.
   *
   * @param input the input to read.
   *
   * @return an instance of {@link StringTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  static StringTag readString(@NotNull final DataInput input) throws IOException {
    return Tag.createString(input.readUTF());
  }

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
   * @param original the original list.
   *
   * @return an instance of {@link ListTag}.
   */
  @NotNull
  static ListTag createList(@NotNull final Tag... original) {
    return Tag.createList(Arrays.asList(original));
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
    return new ListTagBasic(original, original.isEmpty() ? 0 : original.get(0).id());
  }

  /**
   * creates an instance of {@link ByteArrayTag}.
   *
   * @param original the original list.
   *
   * @return an instance of {@link ByteArrayTag}.
   */
  @NotNull
  static ByteArrayTag createByteArray(@NotNull final Byte @NotNull [] original) {
    return new ByteArrayTag(original);
  }

  /**
   * creates an instance of {@link IntArrayTag}.
   *
   * @param original the original list.
   *
   * @return an instance of {@link IntArrayTag}.
   */
  @NotNull
  static IntArrayTag createIntArray(@NotNull final Integer @NotNull [] original) {
    return new IntArrayTag(original);
  }

  /**
   * creates an instance of {@link LongArrayTag}.
   *
   * @param original the original list.
   *
   * @return an instance of {@link LongArrayTag}.
   */
  @NotNull
  static LongArrayTag createLongArray(@NotNull final Long @NotNull [] original) {
    return new LongArrayTag(original);
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
    if (original instanceof Byte) {
      return new ByteTag(original.byteValue());
    } else if (original instanceof Short) {
      return new ShortTag(original.shortValue());
    } else if (original instanceof Integer) {
      return new IntTag(original.intValue());
    } else if (original instanceof Long) {
      return new LongTag(original.longValue());
    } else if (original instanceof Float) {
      return new FloatTag(original.floatValue());
    }
    return new DoubleTag(original.doubleValue());
  }

  /**
   * creates an instance of {@link ByteTag}.
   *
   * @param original the original byte.
   *
   * @return an instance of {@link ByteTag}.
   */
  @NotNull
  static ByteTag createByte(final byte original) {
    return new ByteTag(original);
  }

  /**
   * creates an instance of {@link DoubleTag}.
   *
   * @param original the original double.
   *
   * @return an instance of {@link DoubleTag}.
   */
  @NotNull
  static DoubleTag createDouble(final double original) {
    return new DoubleTag(original);
  }

  /**
   * creates an instance of {@link FloatTag}.
   *
   * @param original the original float.
   *
   * @return an instance of {@link FloatTag}.
   */
  @NotNull
  static FloatTag createFloat(final float original) {
    return new FloatTag(original);
  }

  /**
   * creates an instance of {@link IntTag}.
   *
   * @param original the original int.
   *
   * @return an instance of {@link IntTag}.
   */
  @NotNull
  static IntTag createInt(final int original) {
    return new IntTag(original);
  }

  /**
   * creates an instance of {@link LongTag}.
   *
   * @param original the original long.
   *
   * @return an instance of {@link LongTag}.
   */
  @NotNull
  static LongTag createLong(final long original) {
    return new LongTag(original);
  }

  /**
   * creates an instance of {@link ShortTag}.
   *
   * @param original the original short.
   *
   * @return an instance of {@link ShortTag}.
   */
  @NotNull
  static ShortTag createShort(final short original) {
    return new ShortTag(original);
  }

  /**
   * creates an instance of {@link StringTag}.
   *
   * @param original the original string.
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
   * checks if {@code this} is a {@link ArrayTag}.
   *
   * @return {@code true} if {@code this} is a {@link ArrayTag}.
   */
  default boolean isArray() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link ByteArrayTag}.
   *
   * @return {@code true} if {@code this} is a {@link ByteArrayTag}.
   */
  default boolean isByteArray() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link IntArrayTag}.
   *
   * @return {@code true} if {@code this} is a {@link IntArrayTag}.
   */
  default boolean isIntArray() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link LongArrayTag}.
   *
   * @return {@code true} if {@code this} is a {@link LongArrayTag}.
   */
  default boolean isLongArray() {
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
   * checks if {@code this} is a {@link NumberTag}.
   *
   * @return {@code true} if {@code this} is a {@link NumberTag}.
   */
  default boolean isNumber() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link ByteTag}.
   *
   * @return {@code true} if {@code this} is a {@link ByteTag}.
   */
  default boolean isByte() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link DoubleTag}.
   *
   * @return {@code true} if {@code this} is a {@link DoubleTag}.
   */
  default boolean isDouble() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link FloatTag}.
   *
   * @return {@code true} if {@code this} is a {@link FloatTag}.
   */
  default boolean isFloat() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link IntTag}.
   *
   * @return {@code true} if {@code this} is a {@link IntTag}.
   */
  default boolean isInt() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link LongTag}.
   *
   * @return {@code true} if {@code this} is a {@link LongTag}.
   */
  default boolean isLong() {
    return false;
  }

  /**
   * checks if {@code this} is a {@link ShortTag}.
   *
   * @return {@code true} if {@code this} is a {@link ShortTag}.
   */
  default boolean isShort() {
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
   * an instance of {@code this} as a {@link ArrayTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link ArrayTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link ArrayTag}.
   */
  @NotNull
  default ArrayTag<?> asArray() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a ArrayTag!");
  }

  /**
   * an instance of {@code this} as a {@link ByteArrayTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link ByteArrayTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link ByteArrayTag}.
   */
  @NotNull
  default ByteArrayTag asByteArray() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a ByteArrayTag!");
  }

  /**
   * an instance of {@code this} as a {@link IntArrayTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link IntArrayTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link IntArrayTag}.
   */
  @NotNull
  default IntArrayTag asIntArray() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a IntArrayTag!");
  }

  /**
   * an instance of {@code this} as a {@link LongArrayTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link LongArrayTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link LongArrayTag}.
   */
  @NotNull
  default LongArrayTag asLongArray() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a LongArrayTag!");
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
   * an instance of {@code this} as a {@link ByteTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link ByteTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link ByteTag}.
   */
  @NotNull
  default ByteTag asByte() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a ByteTag!");
  }

  /**
   * an instance of {@code this} as a {@link DoubleTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link DoubleTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link DoubleTag}.
   */
  @NotNull
  default DoubleTag asDouble() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a DoubleTag!");
  }

  /**
   * an instance of {@code this} as a {@link FloatTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link FloatTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link FloatTag}.
   */
  @NotNull
  default FloatTag asFloat() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a FloatTag!");
  }

  /**
   * an instance of {@code this} as a {@link IntTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link IntTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link IntTag}.
   */
  @NotNull
  default IntTag asInt() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a IntTag!");
  }

  /**
   * an instance of {@code this} as a {@link LongTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link LongTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link LongTag}.
   */
  @NotNull
  default LongTag asLong() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a LongTag!");
  }

  /**
   * an instance of {@code this} as a {@link ShortTag}.
   *
   * @return an autoboxed instance of {@code this} as {@link ShortTag}.
   *
   * @throws IllegalStateException if {@code this} is not a {@link ShortTag}.
   */
  @NotNull
  default ShortTag asShort() {
    throw new IllegalStateException(this.getClass() + " cannot cast as a ShortTag!");
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

  /**
   * obtains id of the tag.
   *
   * @return id of the tag.
   */
  byte id();

  /**
   * writes the tag to the given output.
   *
   * @param output the output to write.
   *
   * @throws IOException if an exception was encountered while writing.
   */
  void write(@NotNull DataOutput output) throws IOException;
}
