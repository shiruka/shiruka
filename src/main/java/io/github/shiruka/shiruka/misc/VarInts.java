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

package io.github.shiruka.shiruka.misc;

import com.google.common.base.Preconditions;
import io.github.shiruka.api.util.Vector;
import io.netty.buffer.ByteBuf;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * an utility class to write/read {@link DataInput} and {@link DataOutput}.
 */
public final class VarInts {

  /**
   * The default character encoding for protocol Strings.
   */
  public static final Charset NET_CHARSET = StandardCharsets.UTF_8;

  /**
   * ctor.
   */
  private VarInts() {
  }

  /**
   * transfers all of the next readable bytes from the buffer into a new byte array.
   *
   * @param buf the buffer to transfer from.
   *
   * @return the an array containing the bytes of the buffer.
   */
  public static byte[] arr(@NotNull final ByteBuf buf) {
    return VarInts.arr(buf, buf.readableBytes());
  }

  /**
   * transfers the specified length of bytes from the buffer into a new byte array.
   *
   * @param buf the buffer to transfer from.
   * @param len the length of bytes to transfer.
   *
   * @return the new byte array.
   */
  public static byte[] arr(@NotNull final ByteBuf buf, final int len) {
    final var bytes = new byte[len];
    buf.readBytes(bytes);
    return bytes;
  }

  /**
   * converts the given angle into protocol format.
   *
   * @param angle the angle to convert.
   *
   * @return the protocol format.
   */
  public static byte convertAngle(final float angle) {
    return (byte) (angle / 1.40625);
  }

  /**
   * reads the integer from the given input.
   *
   * @param input the input to read.
   *
   * @return the given input's integer value.
   *
   * @throws IOException if something went wrong when reading to the input.
   */
  public static int readInt(@NotNull final DataInput input) throws IOException {
    final var n = (int) VarInts.decodeUnsigned(input);
    return n >>> 1 ^ -(n & 1);
  }

  /**
   * reads the long from the given input.
   *
   * @param input the input to read.
   *
   * @return the given input's long value.
   *
   * @throws IOException if something went wrong when reading the input.
   */
  public static long readLong(@NotNull final DataInput input) throws IOException {
    final var n = VarInts.decodeUnsigned(input);
    return n >>> 1 ^ -(n & 1);
  }

  /**
   * reads the next String value from the byte stream represented by the given buffer.
   *
   * @param buf the buffer which to read the String.
   *
   * @return the next String value.
   */
  public static String readString(final ByteBuf buf) {
    final var len = VarInts.readVarInt(buf);
    final var stringData = VarInts.arr(buf, len);
    return new String(stringData, VarInts.NET_CHARSET);
  }

  /**
   * reads the integer from the given input.
   *
   * @param input the input to read.
   *
   * @return the given input's integer value.
   *
   * @throws IOException if something went wrong when reading the input.
   */
  public static int readUnsignedInt(@NotNull final DataInput input) throws IOException {
    return (int) VarInts.decodeUnsigned(input);
  }

  /**
   * reads the integer from the given input.
   *
   * @param input the input to read.
   *
   * @return the given input's integer value.
   */
  public static int readUnsignedVarInt(@NotNull final ByteBuf input) {
    var value = 0;
    var i = 0;
    int b;
    while (((b = input.readByte()) & 0x80) != 0) {
      value |= (b & 0x7F) << i;
      i += 7;
      Preconditions.checkArgument(i <= 35, "VarInt too big!");
    }
    return value | b << i;
  }

  /**
   * reads the next VarInt from the given buffer.
   *
   * @param buf the buffer to read.
   *
   * @return the next VarInt.
   */
  public static int readVarInt(@NotNull final ByteBuf buf) {
    var result = 0;
    var indent = 0;
    var b = (int) buf.readByte();
    while ((b & 0x80) == 0x80) {
      Preconditions.checkArgument(indent < 21, "Too many bytes for a VarInt32.");
      result += (b & 0x7f) << indent;
      indent += 7;
      b = buf.readByte();
    }
    result += (b & 0x7f) << indent;
    return result;
  }

  /**
   * reads the next VarLong value from the byte stream represented by the given buffer.
   *
   * @param buf the buffer which to read the VarLong.
   *
   * @return the next VarLong value.
   */
  public static long readVarlong(@NotNull final ByteBuf buf) {
    var result = 0L;
    var indent = 0;
    var b = (long) buf.readByte();
    while ((b & 0x80L) == 0x80) {
      Preconditions.checkArgument(indent < 48, "Too many bytes for a VarInt64.");
      result += (b & 0x7fL) << indent;
      indent += 7;
      b = buf.readByte();
    }
    result += b & 0x7fL;
    return result << indent;
  }

  /**
   * reads the next vector and creates a new Vector instance.
   *
   * @param buf the buffer to read from.
   *
   * @return a new vector.
   */
  @NotNull
  public static Vector readVector(@NotNull final ByteBuf buf) {
    final var pos = buf.readLong();
    return Vector.create(pos >> 38, pos >> 26 & 0xFFF, pos << 38 >> 38);
  }

  /**
   * writes the given integer into the given output.
   *
   * @param output the output to write.
   * @param integer the integer to write.
   *
   * @throws IOException if something went wrong when writing to the output..
   */
  public static void writeInt(@NotNull final DataOutput output, final int integer) throws IOException {
    VarInts.encodeUnsigned(output, (long) integer << 1 ^ integer >> 31);
  }

  /**
   * writes the given long into the given output.
   *
   * @param output the output to write.
   * @param longInteger the long integer to write.
   *
   * @throws IOException if something went wrong when writing to the output.
   */
  public static void writeLong(@NotNull final DataOutput output, final long longInteger) throws IOException {
    VarInts.encodeUnsigned(output, longInteger << 1 ^ longInteger >> 63);
  }

  /**
   * encodes the given String into the given buffer using the Minecraft protocol format.
   *
   * @param buf the buffer which to write.
   * @param s the String to write.
   */
  public static void writeString(@NotNull final ByteBuf buf, @NotNull final String s) {
    VarInts.writeVarInt(buf, s.length());
    buf.writeBytes(s.getBytes(VarInts.NET_CHARSET));
  }

  /**
   * writes the given length into the given buffer.
   *
   * @param buffer the buffer to write.
   * @param length the length to write.
   */
  public static void writeUnsignedInt(@NotNull final ByteBuf buffer, final long length) {
    VarInts.encodeUnsigned(buffer, length);
  }

  /**
   * writes the given integer into the given output.
   *
   * @param output the output to write.
   * @param integer the integer to write.
   *
   * @throws IOException if something went wrong when writing to the output.
   */
  public static void writeUnsignedInt(@NotNull final DataOutput output, final long integer) throws IOException {
    VarInts.encodeUnsigned(output, integer);
  }

  /**
   * writes a VarInt value to the given buffer.
   *
   * @param buf the buffer to write.
   * @param i the VarInt to write.
   */
  public static void writeVarInt(@NotNull final ByteBuf buf, int i) {
    while ((i & 0xFFFFFF80) != 0L) {
      buf.writeByte(i & 0x7F | 0x80);
      i >>>= 7;
    }
    buf.writeByte(i & 0x7F);
  }

  /**
   * writes a VarLong value to the given buffer.
   *
   * @param buf the buffer which to write.
   * @param l the VarLong value.
   */
  public static void writeVarLong(@NotNull final ByteBuf buf, long l) {
    while ((l & 0xFFFFFFFFFFFFFF80L) != 0L) {
      buf.writeByte((int) (l & 0x7FL | 0x80L));
      l >>>= 7L;
    }
    buf.writeByte((int) (l & 0x7FL));
  }

  /**
   * writes a vector value to the given buffer.
   *
   * @param buf the buffer to write.
   * @param vec the Vector coordinates to write.
   */
  public static void writeVector(@NotNull final ByteBuf buf, @NotNull final Vector vec) {
    final var l = ((long) vec.getIntX() & 0x3FFFFFFL) << 38 |
      ((long) vec.getIntY() & 0xFFFL) << 26 |
      (long) vec.getIntZ() & 0x3FFFFFFL;
    buf.writeLong(l);
  }

  /**
   * decodes the long value from the given input.
   *
   * @param input the input to decode.
   *
   * @return decoded lon value from the given input.
   *
   * @throws IOException if something went wrong when decoding the given input.
   */
  private static long decodeUnsigned(@NotNull final DataInput input) throws IOException {
    var result = 0;
    for (var shift = 0; shift < 64; shift += 7) {
      final var b = input.readByte();
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return result;
      }
    }
    throw new ArithmeticException("VarInt was too large");
  }

  /**
   * encodes the given length into the buffer.
   *
   * @param buffer the buffer to encode.
   * @param length the length to encode.
   */
  private static void encodeUnsigned(@NotNull final ByteBuf buffer, long length) {
    while (true) {
      if ((length & ~0x7FL) == 0) {
        buffer.writeByte((int) length);
        return;
      }
      buffer.writeByte((byte) ((int) length & 0x7F | 0x80));
      length >>>= 7;
    }
  }

  /**
   * encodes the given value into the output.
   *
   * @param output the output to encode.
   * @param value the value to encode.
   *
   * @throws IOException if something went wrong when encoding to the output.
   */
  private static void encodeUnsigned(@NotNull final DataOutput output, long value) throws IOException {
    while (true) {
      if ((value & ~0x7FL) == 0) {
        output.writeByte((int) value);
        return;
      }
      output.writeByte((byte) ((int) value & 0x7F | 0x80));
      value >>>= 7;
    }
  }
}
