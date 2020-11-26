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
import io.github.shiruka.shiruka.nbt.stream.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine named binary tag.
 */
public interface Tag {

  /**
   * an end tag instance.
   */
  Tag END = () -> (byte) 0;

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
  ByteArrayTag BYTE_ARRAY = Tag.createByteArray(new Byte[0]);

  /**
   * an empty {@link IntArrayTag} instance.
   */
  IntArrayTag INT_ARRAY = Tag.createIntArray(new Integer[0]);

  /**
   * an empty {@link LongArrayTag} instance.
   */
  LongArrayTag LONG_ARRAY = Tag.createLongArray(new Long[0]);

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
   * an empty {@link StringTag} instance.
   */
  StringTag STRING = Tag.createString("");

  /**
   * creates a nbt reader from the {@link InputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTInputStream} with {@link DataInputStream}.
   */
  @NotNull
  static NBTInputStream createReader(@NotNull final InputStream stream) {
    return new NBTInputStream(new DataInputStream(stream));
  }

  /**
   * creates a nbt reader from the {@link InputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTInputStream} with {@link LittleEndianDataInputStream}.
   */
  @NotNull
  static NBTInputStream createReaderLE(@NotNull final InputStream stream) {
    return new NBTInputStream(new LittleEndianDataInputStream(stream));
  }

  /**
   * creates a nbt reader from the {@link InputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTInputStream} with {@link GZIPInputStream}.
   */
  @NotNull
  static NBTInputStream createGZIPReader(@NotNull final InputStream stream) throws IOException {
    return Tag.createReader(new GZIPInputStream(stream));
  }

  /**
   * creates a nbt reader from the {@link InputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTInputStream} with {@link NetworkDataInputStream}.
   */
  @NotNull
  static NBTInputStream createNetworkReader(@NotNull final InputStream stream) {
    return new NBTInputStream(new NetworkDataInputStream(stream));
  }

  /**
   * creates a nbt writer from the {@link OutputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTOutputStream} with {@link DataOutputStream}.
   */
  @NotNull
  static NBTOutputStream createWriter(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new DataOutputStream(stream));
  }

  /**
   * creates a nbt writer from the {@link OutputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTOutputStream} with {@link LittleEndianDataOutputStream}.
   */
  @NotNull
  static NBTOutputStream createWriterLE(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new LittleEndianDataOutputStream(stream));
  }

  /**
   * creates a nbt writer from the {@link OutputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTOutputStream} with {@link GZIPOutputStream}.
   */
  @NotNull
  static NBTOutputStream createGZIPWriter(@NotNull final OutputStream stream) throws IOException {
    return Tag.createWriter(new GZIPOutputStream(stream));
  }

  /**
   * creates a nbt writer from the {@link OutputStream}.
   *
   * @param stream the stream to create.
   *
   * @return a new instance of {@link NBTOutputStream} with {@link NetworkDataOutputStream}.
   */
  @NotNull
  static NBTOutputStream createNetworkWriter(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new NetworkDataOutputStream(stream));
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
}
