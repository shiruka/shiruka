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

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NBTInputStream implements Closeable {

  @NotNull
  private final DataInput input;

  private boolean closed = false;

  public NBTInputStream(@NotNull final DataInput input) {
    this.input = input;
  }

  @Nullable
  public Object readTag() throws IOException {
    return this.readTag(Nbt.MAX_DEPTH);
  }

  @Nullable
  public Object readTag(final int maxDepth) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("Trying to read from a closed reader!");
    }
    final var typeId = this.input.readUnsignedByte();
    final var type = NbtType.byId(typeId);
    this.input.readUTF(); // Root tag name
    return this.deserialize(type, maxDepth);
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Nullable
  private Object deserialize(@NotNull final NbtType<?> type, final int maxDepth) throws IOException {
    if (maxDepth < 0) {
      throw new IllegalArgumentException("NBT compound is too deeply nested");
    }
    switch (type.getEnum()) {
      case END:
        return null;
      case BYTE:
        return this.input.readByte();
      case SHORT:
        return this.input.readShort();
      case INT:
        return this.input.readInt();
      case LONG:
        return this.input.readLong();
      case FLOAT:
        return this.input.readFloat();
      case DOUBLE:
        return this.input.readDouble();
      case BYTE_ARRAY:
        final var byteArraySize = this.input.readInt();
        final var bytes = new byte[byteArraySize];
        this.input.readFully(bytes);
        return bytes;
      case STRING:
        return this.input.readUTF();
      case COMPOUND:
        final var map = new LinkedHashMap<String, Object>();
        NbtType<?> nbtType;
        while ((nbtType = NbtType.byId(this.input.readUnsignedByte())) != NbtType.END) {
          final var name = this.input.readUTF();
          map.put(name, this.deserialize(nbtType, maxDepth - 1));
        }
        return new NbtMap(map);
      case LIST:
        final var typeId = this.input.readUnsignedByte();
        final var listType = NbtType.byId(typeId);
        final var list = new ArrayList<>();
        final var listLength = this.input.readInt();
        for (var i = 0; i < listLength; i++) {
          list.add(this.deserialize(listType, maxDepth - 1));
        }
        return new NbtList(listType, list);
      case INT_ARRAY:
        final var intArraySize = this.input.readInt();
        final var ints = new int[intArraySize];
        for (var i = 0; i < intArraySize; i++) {
          ints[i] = this.input.readInt();
        }
        return ints;
      case LONG_ARRAY:
        final var longArraySize = this.input.readInt();
        final var longs = new long[longArraySize];
        for (var i = 0; i < longArraySize; i++) {
          longs[i] = this.input.readLong();
        }
        return longs;
    }
    throw new IllegalArgumentException("Unknown type " + type);
  }
}
