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

package net.shiruka.shiruka.nbt.stream;

import com.google.common.base.Preconditions;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.ListTag;
import net.shiruka.shiruka.nbt.Tag;
import net.shiruka.shiruka.nbt.array.ByteArrayTag;
import net.shiruka.shiruka.nbt.array.IntArrayTag;
import net.shiruka.shiruka.nbt.array.LongArrayTag;
import net.shiruka.shiruka.nbt.primitive.*;
import org.jetbrains.annotations.NotNull;

/**
 * an input stream to read named binary tags.
 */
public final class NBTInputStream implements Closeable {

  /**
   * the input.
   */
  private final DataInput input;

  /**
   * if the stream closed.
   */
  private boolean closed = false;

  /**
   * ctor.
   *
   * @param input the input.
   */
  public NBTInputStream(@NotNull final DataInput input) {
    this.input = input;
  }

  @Override
  public void close() throws IOException {
    if (this.closed) {
      return;
    }
    this.closed = true;
    if (this.input instanceof Closeable) {
      ((Closeable) this.input).close();
    }
  }

  /**
   * reads the given input using the id.
   *
   * @param id the id to read.
   *
   * @return a new tag instance depends on the given id.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public Tag read(final byte id) throws IOException {
    Preconditions.checkState(!this.closed, "Trying to read from a closed reader!");
    switch (id) {
      case 1:
        return this.readByte();
      case 2:
        return this.readShort();
      case 3:
        return this.readInt();
      case 4:
        return this.readLong();
      case 5:
        return this.readFloat();
      case 6:
        return this.readDouble();
      case 7:
        return this.readByteArray();
      case 8:
        return this.readString();
      case 9:
        return this.readListTag();
      case 10:
        return this.readCompoundTag();
      case 11:
        return this.readIntArray();
      case 12:
        return this.readLongArray();
      case 0:
      default:
        throw new IllegalArgumentException("Unknown type " + id);
    }
  }

  /**
   * reads the given input and converts it into the {@link ByteTag}.
   *
   * @return an instance of {@link ByteTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public ByteTag readByte() throws IOException {
    return Tag.createByte(this.input.readByte());
  }

  /**
   * reads the given input and converts it into the {@link ByteArrayTag}.
   *
   * @return an instance of {@link ByteArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public ByteArrayTag readByteArray() throws IOException {
    final var length = this.input.readInt();
    final var value = new byte[length];
    this.input.readFully(value);
    return Tag.createByteArray(value);
  }

  /**
   * reads the given input and converts it into the {@link CompoundTag}.
   *
   * @return an instance of {@link CompoundTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public CompoundTag readCompoundTag() throws IOException {
    final var tags = new HashMap<String, Tag>();
    byte id;
    while ((id = this.input.readByte()) != Tag.END.id()) {
      final var key = this.input.readUTF();
      final var tag = this.read(id);
      tags.put(key, tag);
    }
    return Tag.createCompound(tags);
  }

  /**
   * reads the given input and converts it into the {@link DoubleTag}.
   *
   * @return an instance of {@link DoubleTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public DoubleTag readDouble() throws IOException {
    return Tag.createDouble(this.input.readDouble());
  }

  /**
   * reads the given input and converts it into the {@link FloatTag}.
   *
   * @return an instance of {@link FloatTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public FloatTag readFloat() throws IOException {
    return Tag.createFloat(this.input.readFloat());
  }

  /**
   * reads the given input and converts it into the {@link IntTag}.
   *
   * @return an instance of {@link IntTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public IntTag readInt() throws IOException {
    return Tag.createInt(this.input.readInt());
  }

  /**
   * reads the given input and converts it into the {@link IntArrayTag}.
   *
   * @return an instance of {@link IntArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public IntArrayTag readIntArray() throws IOException {
    final var length = this.input.readInt();
    final var value = new int[length];
    for (var i = 0; i < length; i++) {
      value[i] = this.input.readInt();
    }
    return Tag.createIntArray(value);
  }

  /**
   * reads the given input and converts it into the {@link ListTag}.
   *
   * @return an instance of {@link ListTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public ListTag readListTag() throws IOException {
    final var id = this.input.readByte();
    final var length = this.input.readInt();
    final var tags = new ArrayList<Tag>(length);
    for (var i = 0; i < length; i++) {
      final var read = this.read(id);
      tags.add(read);
    }
    return Tag.createList(tags);
  }

  /**
   * reads the given input and converts it into the {@link LongTag}.
   *
   * @return an instance of {@link LongTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public LongTag readLong() throws IOException {
    return Tag.createLong(this.input.readLong());
  }

  /**
   * reads the given input and converts it into the {@link LongArrayTag}.
   *
   * @return an instance of {@link LongArrayTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public LongArrayTag readLongArray() throws IOException {
    final var length = this.input.readInt();
    final var value = new long[length];
    for (var i = 0; i < length; i++) {
      value[i] = this.input.readLong();
    }
    return Tag.createLongArray(value);
  }

  /**
   * reads the given input and converts it into the {@link ShortTag}.
   *
   * @return an instance of {@link ShortTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public ShortTag readShort() throws IOException {
    return Tag.createShort(this.input.readShort());
  }

  /**
   * reads the given input and converts it into the {@link StringTag}.
   *
   * @return an instance of {@link StringTag}.
   *
   * @throws IOException if something went wrong when reading the given input.
   */
  @NotNull
  public StringTag readString() throws IOException {
    return Tag.createString(this.input.readUTF());
  }
}
