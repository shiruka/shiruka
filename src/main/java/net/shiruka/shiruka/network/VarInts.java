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

package net.shiruka.shiruka.network;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import net.shiruka.api.base.Vector3D;
import org.jetbrains.annotations.NotNull;

/**
 * a class that helps developers to read/write {@link ByteBuf}.
 */
public final class VarInts {

  /**
   * ctor.
   */
  private VarInts() {
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
   * reads the next String value from the byte stream represented by the given buffer.
   *
   * @param buf the buffer which to read the String.
   *
   * @return the next String value.
   */
  public static String readString(final ByteBuf buf) {
    final var len = VarInts.readVarInt(buf);
    final var stringData = VarInts.arr(buf, len);
    return new String(stringData, StandardCharsets.UTF_8);
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
  public static long readVarLong(@NotNull final ByteBuf buf) {
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
   * writes the given bytes into the given buffer.
   *
   * @param buffer the buffer to write.
   * @param bytes the bytes to write.
   */
  public static void writeByteArray(@NotNull final ByteBuf buffer, final byte[] bytes) {
    VarInts.writeUnsignedInt(buffer, bytes.length);
    buffer.writeBytes(bytes);
  }

  /**
   * encodes the given String into the given buffer using the Minecraft protocol format.
   *
   * @param buf the buffer which to write.
   * @param s the String to write.
   */
  public static void writeString(@NotNull final ByteBuf buf, @NotNull final String s) {
    VarInts.writeVarInt(buf, s.length());
    buf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
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
   * writes a VarInt value to the given buffer.
   *
   * @param buf the buffer to write.
   * @param i the VarInt to write.
   */
  public static void writeVarInt(@NotNull final ByteBuf buf, final int i) {
    var tempI = i;
    while ((tempI & 0xFFFFFF80) != 0L) {
      buf.writeByte(tempI & 0x7F | 0x80);
      tempI >>>= 7;
    }
    buf.writeByte(tempI & 0x7F);
  }

  /**
   * writes a VarLong value to the given buffer.
   *
   * @param buf the buffer which to write.
   * @param l the VarLong value.
   */
  public static void writeVarLong(@NotNull final ByteBuf buf, final long l) {
    var tempL = l;
    while ((tempL & 0xFFFFFFFFFFFFFF80L) != 0L) {
      buf.writeByte((int) (tempL & 0x7FL | 0x80L));
      tempL >>>= 7L;
    }
    buf.writeByte((int) (tempL & 0x7FL));
  }

  /**
   * writes a vector value to the given buffer.
   *
   * @param buf the buffer to write.
   * @param vec the Vector coordinates to write.
   */
  public static void writeVector(@NotNull final ByteBuf buf, @NotNull final Vector3D vec) {
    final var l = ((long) vec.getIntX() & 0x3FFFFFFL) << 38 |
      ((long) vec.getIntY() & 0xFFFL) << 26 |
      (long) vec.getIntZ() & 0x3FFFFFFL;
    buf.writeLong(l);
  }

  /**
   * /**
   * encodes the given length into the buffer.
   *
   * @param buffer the buffer to encode.
   * @param length the length to encode.
   */
  private static void encodeUnsigned(@NotNull final ByteBuf buffer, final long length) {
    var tempLength = length;
    while (true) {
      if ((tempLength & ~0x7FL) == 0) {
        buffer.writeByte((int) tempLength);
        return;
      }
      buffer.writeByte((byte) ((int) tempLength & 0x7F | 0x80));
      tempLength >>>= 7;
    }
  }
}
