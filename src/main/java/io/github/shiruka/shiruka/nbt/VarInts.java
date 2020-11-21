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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public final class VarInts {

  private VarInts() {
  }

  public static void writeInt(@NotNull final DataOutput buffer, final int integer) throws IOException {
    VarInts.encodeUnsigned(buffer, integer << 1 ^ integer >> 31);
  }

  public static int readInt(@NotNull final DataInput buffer) throws IOException {
    final int n = (int) VarInts.decodeUnsigned(buffer);
    return n >>> 1 ^ -(n & 1);
  }

  public static void writeUnsignedInt(@NotNull final DataOutput buffer, final long integer) throws IOException {
    VarInts.encodeUnsigned(buffer, integer);
  }

  public static int readUnsignedInt(@NotNull final DataInput buffer) throws IOException {
    return (int) VarInts.decodeUnsigned(buffer);
  }

  public static void writeLong(@NotNull final DataOutput buffer, final long longInteger) throws IOException {
    VarInts.encodeUnsigned(buffer, longInteger << 1 ^ longInteger >> 63);
  }

  public static long readLong(@NotNull final DataInput buffer) throws IOException {
    final long n = VarInts.decodeUnsigned(buffer);
    return n >>> 1 ^ -(n & 1);
  }

  public static void writeUnsignedLong(@NotNull final DataOutput buffer, final long longInteger) throws IOException {
    VarInts.encodeUnsigned(buffer, longInteger);
  }

  public static long readUnsignedLong(@NotNull final DataInput buffer) throws IOException {
    return VarInts.decodeUnsigned(buffer);
  }

  private static long decodeUnsigned(@NotNull final DataInput buffer) throws IOException {
    long result = 0;
    for (int shift = 0; shift < 64; shift += 7) {
      final byte b = buffer.readByte();
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return result;
      }
    }
    throw new ArithmeticException("VarInt was too large");
  }

  private static void encodeUnsigned(@NotNull final DataOutput buffer, long value) throws IOException {
    while (true) {
      if ((value & ~0x7FL) == 0) {
        buffer.writeByte((int) value);
        return;
      } else {
        buffer.writeByte((byte) ((int) value & 0x7F | 0x80));
        value >>>= 7;
      }
    }
  }
}