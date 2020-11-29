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

package io.github.shiruka.shiruka.nbt.stream;

import io.github.shiruka.shiruka.misc.ArrayUtils;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import io.github.shiruka.shiruka.nbt.ListTag;
import io.github.shiruka.shiruka.nbt.Tag;
import io.github.shiruka.shiruka.nbt.array.ByteArrayTag;
import io.github.shiruka.shiruka.nbt.array.IntArrayTag;
import io.github.shiruka.shiruka.nbt.array.LongArrayTag;
import io.github.shiruka.shiruka.nbt.primitive.*;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * an output stream to write named binary tags.
 */
public final class NBTOutputStream implements Closeable {

  /**
   * the output.
   */
  @NotNull
  private final DataOutput output;

  /**
   * if the stream is closed.
   */
  private boolean closed = false;

  /**
   * ctor.
   *
   * @param output the output.
   */
  public NBTOutputStream(@NotNull final DataOutput output) {
    this.output = output;
  }

  @Override
  public void close() throws IOException {
    if (this.closed) {
      return;
    }
    this.closed = true;
    if (this.output instanceof Closeable) {
      ((Closeable) this.output).close();
    }
  }

  /**
   * writes the given values into the {@link #output}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void write(@NotNull final Tag value) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("Trying to read from a closed reader!");
    }
    final var id = value.id();
    if (value.isByte()) {
      this.writeByte(value.asByte());
    } else if (value.isShort()) {
      this.writeShort(value.asShort());
    } else if (value.isInt()) {
      this.writeInt(value.asInt());
    } else if (value.isLong()) {
      this.writeLong(value.asLong());
    } else if (value.isFloat()) {
      this.writeFloat(value.asFloat());
    } else if (value.isDouble()) {
      this.writeDouble(value.asDouble());
    } else if (value.isByteArray()) {
      this.writeByteArray(value.asByteArray());
    } else if (value.isString()) {
      this.writeString(value.asString());
    } else if (value.isList()) {
      this.writeListTag(value.asList());
    } else if (value.isCompound()) {
      this.writeCompoundTag(value.asCompound());
    } else if (value.isIntArray()) {
      this.writeIntArray(value.asIntArray());
    } else if (value.isLongArray()) {
      this.writeLongArray(value.asLongArray());
    } else {
      throw new IllegalArgumentException("Unknown type " + id);
    }
  }

  /**
   * reads the given input and converts it into the {@link ByteTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeByte(@NotNull final ByteTag value) throws IOException {
    this.output.writeByte(value.byteValue());
  }

  /**
   * reads the given input and converts it into the {@link ByteArrayTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeByteArray(@NotNull final ByteArrayTag value) throws IOException {
    final var primitive = ArrayUtils.toPrimitive(value.value());
    this.output.writeInt(primitive.length);
    this.output.write(primitive);
  }

  /**
   * reads the given input and converts it into the {@link CompoundTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeCompoundTag(@NotNull final CompoundTag value) throws IOException {
    final var entries = value.all().entrySet();
    for (final var entry : entries) {
      final var tag = entry.getValue();
      this.output.writeByte(tag.id());
      if (tag.id() != Tag.END.id()) {
        this.output.writeUTF(entry.getKey());
        this.write(tag);
      }
    }
    this.output.writeByte(Tag.END.id());
  }

  /**
   * reads the given input and converts it into the {@link DoubleTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeDouble(@NotNull final DoubleTag value) throws IOException {
    this.output.writeDouble(value.doubleValue());
  }

  /**
   * reads the given input and converts it into the {@link FloatTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeFloat(@NotNull final FloatTag value) throws IOException {
    this.output.writeFloat(value.floatValue());
  }

  /**
   * reads the given input and converts it into the {@link IntTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeInt(@NotNull final IntTag value) throws IOException {
    this.output.writeInt(value.intValue());
  }

  /**
   * reads the given input and converts it into the {@link IntArrayTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeIntArray(@NotNull final IntArrayTag value) throws IOException {
    this.output.writeInt(value.size());
    for (final var val : value.value()) {
      this.output.writeInt(val);
    }
  }

  /**
   * reads the given input and converts it into the {@link ListTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeListTag(@NotNull final ListTag value) throws IOException {
    this.output.writeByte(value.listType());
    this.output.writeInt(value.size());
    for (final var tag : value) {
      this.write(tag);
    }
  }

  /**
   * reads the given input and converts it into the {@link LongTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeLong(@NotNull final LongTag value) throws IOException {
    this.output.writeLong(value.longValue());
  }

  /**
   * reads the given input and converts it into the {@link LongArrayTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeLongArray(@NotNull final LongArrayTag value) throws IOException {
    this.output.writeInt(value.size());
    for (final var val : value.value()) {
      this.output.writeLong(val);
    }
  }

  /**
   * reads the given input and converts it into the {@link ShortTag}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  public void writeShort(@NotNull final ShortTag value) throws IOException {
    this.output.writeShort(value.shortValue());
  }

  /**
   * writes the given string into the {@link #output}.
   *
   * @param value the value to write.
   *
   * @throws IOException if something went wrong when writing the given value into the output.
   */
  public void writeString(@NotNull final StringTag value) throws IOException {
    this.output.writeUTF(value.value());
  }
}
