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
import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class NBTOutputStream implements Closeable {

  @NotNull
  private final DataOutput output;

  private boolean closed = false;

  public NBTOutputStream(@NotNull final DataOutput output) {
    this.output = output;
  }

  public void writeTag(@NotNull final Object tag) throws IOException {
    this.writeTag(tag, Nbt.MAX_DEPTH);
  }

  public void writeTag(@NotNull final Object tag, final int maxDepth) throws IOException {
    if (this.closed) {
      throw new IllegalStateException("closed");
    }
    final var type = NbtType.byClass(tag.getClass());
    this.output.writeByte(type.getId());
    this.output.writeUTF("");
    this.serialize(tag, maxDepth);
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

  private void serialize(@NotNull final Object tag, final int maxDepth) throws IOException {
    if (maxDepth < 0) {
      throw new IllegalArgumentException("Reached depth limit");
    }
    final var type = NbtType.byClass(tag.getClass());
    switch (type.getEnum()) {
      case END:
        break;
      case BYTE:
        final var byteVal = (Byte) tag;
        this.output.writeByte(byteVal);
        break;
      case SHORT:
        final var shortVal = (Short) tag;
        this.output.writeShort(shortVal);
        break;
      case INT:
        final var intVal = (Integer) tag;
        this.output.writeInt(intVal);
        break;
      case LONG:
        final var longVal = (Long) tag;
        this.output.writeLong(longVal);
        break;
      case FLOAT:
        final var floatVal = (Float) tag;
        this.output.writeFloat(floatVal);
        break;
      case DOUBLE:
        final var doubleVal = (Double) tag;
        this.output.writeDouble(doubleVal);
        break;
      case BYTE_ARRAY:
        final var byteArray = (byte[]) tag;
        this.output.writeInt(byteArray.length);
        this.output.write(byteArray);
        break;
      case STRING:
        final var string = (String) tag;
        this.output.writeUTF(string);
        break;
      case LIST:
        final var list = (NbtList<?>) tag;
        final var listType = list.getType();
        this.output.writeByte(listType.getId());
        this.output.writeInt(list.size());
        for (final Object entry : list) {
          this.serialize(entry, maxDepth - 1);
        }
        break;
      case COMPOUND:
        final var map = (NbtMap) tag;
        for (final var entry : map.entrySet()) {
          final var entryType = NbtType.byClass(entry.getValue().getClass());
          this.output.writeByte(entryType.getId());
          this.output.writeUTF(entry.getKey());
          this.serialize(entry.getValue(), maxDepth - 1);
        }
        this.output.writeByte(0); // End tag
        break;
      case INT_ARRAY:
        final var intArray = (int[]) tag;
        this.output.writeInt(intArray.length);
        for (final var val : intArray) {
          this.output.writeInt(val);
        }
        break;
      case LONG_ARRAY:
        final var longArray = (long[]) tag;
        this.output.writeInt(longArray.length);
        for (final var val : longArray) {
          this.output.writeLong(val);
        }
        break;
    }
  }
}