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

import io.github.shiruka.common.Optionals;
import io.github.shiruka.shiruka.nbt.stream.LittleEndianDataInputStream;
import io.github.shiruka.shiruka.nbt.stream.LittleEndianDataOutputStream;
import io.github.shiruka.shiruka.nbt.stream.NetworkDataInputStream;
import io.github.shiruka.shiruka.nbt.stream.NetworkDataOutputStream;
import java.io.*;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Nbt {

  public static final int MAX_DEPTH = 16;

  private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();

  private Nbt() {
  }

  @NotNull
  public static NBTInputStream createReader(@NotNull final InputStream stream) {
    return new NBTInputStream(new DataInputStream(stream));
  }

  @NotNull
  public static NBTInputStream createReaderLE(@NotNull final InputStream stream) {
    return new NBTInputStream(new LittleEndianDataInputStream(stream));
  }

  @NotNull
  public static NBTInputStream createGZIPReader(@NotNull final InputStream stream) throws IOException {
    return Nbt.createReader(new GZIPInputStream(stream));
  }

  @NotNull
  public static NBTInputStream createNetworkReader(@NotNull final InputStream stream) {
    return new NBTInputStream(new NetworkDataInputStream(stream));
  }

  @NotNull
  public static NBTOutputStream createWriter(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new DataOutputStream(stream));
  }

  @NotNull
  public static NBTOutputStream createWriterLE(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new LittleEndianDataOutputStream(stream));
  }

  @NotNull
  public static NBTOutputStream createGZIPWriter(@NotNull final OutputStream stream) throws IOException {
    return Nbt.createWriter(new GZIPOutputStream(stream));
  }

  @NotNull
  public static NBTOutputStream createNetworkWriter(@NotNull final OutputStream stream) {
    return new NBTOutputStream(new NetworkDataOutputStream(stream));
  }

  @NotNull
  public static String toString(@NotNull final Object o) {
    if (o instanceof Byte) {
      return (byte) o + "b";
    } else if (o instanceof Short) {
      return (short) o + "s";
    } else if (o instanceof Integer) {
      return (int) o + "i";
    } else if (o instanceof Long) {
      return (long) o + "l";
    } else if (o instanceof Float) {
      return (float) o + "f";
    } else if (o instanceof Double) {
      return (double) o + "d";
    } else if (o instanceof byte[]) {
      return "0x" + Nbt.printHexBinary((byte[]) o);
    } else if (o instanceof String) {
      return "\"" + o + "\"";
    } else if (o instanceof int[]) {
      return "[ " +
        Optionals.useAndGet(
          new StringJoiner(", "),
          joiner -> Arrays.stream((int[]) o).mapToObj(i -> i + "i").forEach(joiner::add)) +
        " ]";
    } else if (o instanceof long[]) {
      return "[ " +
        Optionals.useAndGet(
          new StringJoiner(", "),
          joiner -> Arrays.stream((long[]) o).mapToObj(l -> l + "l").forEach(joiner::add))
        + " ]";
    }
    return o.toString();
  }

  @Nullable
  public static <T> T copy(@Nullable final T val) {
    if (val instanceof byte[]) {
      final var bytes = (byte[]) val;
      return (T) Arrays.copyOf(bytes, bytes.length);
    } else if (val instanceof int[]) {
      final var ints = (int[]) val;
      return (T) Arrays.copyOf(ints, ints.length);
    } else if (val instanceof long[]) {
      final var longs = (long[]) val;
      return (T) Arrays.copyOf(longs, longs.length);
    }
    return val;
  }

  @NotNull
  public static String indent(@NotNull final String string) {
    return Optionals.useAndGet(
      new StringBuilder("  " + string),
      builder -> {
        for (int i = 2; i < builder.length(); i++) {
          if (builder.charAt(i) == '\n') {
            builder.insert(i + 1, "  ");
            i += 2;
          }
        }
      }).toString();
  }

  @NotNull
  public static String printHexBinary(@NotNull final byte[] data) {
    return Optionals.useAndGet(
      new StringBuilder(data.length << 1),
      r -> {
        for (final byte b : data) {
          r.append(Nbt.HEX_CODE[b >> 4 & 0xF]);
          r.append(Nbt.HEX_CODE[b & 0xF]);
        }
      }).toString();
  }
}
