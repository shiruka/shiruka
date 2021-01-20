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

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link DataOutput}.
 */
public class LittleEndianDataOutputStream implements DataOutput, Closeable {

  /**
   * the stream.
   */
  @NotNull
  protected final DataOutputStream stream;

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public LittleEndianDataOutputStream(@NotNull final OutputStream stream) {
    this.stream = new DataOutputStream(stream);
  }

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public LittleEndianDataOutputStream(@NotNull final DataOutputStream stream) {
    this.stream = stream;
  }

  @Override
  public final void close() throws IOException {
    this.stream.close();
  }

  @Override
  public final void write(final int b) throws IOException {
    this.stream.write(b);
  }

  @Override
  public final void write(final byte[] b) throws IOException {
    this.stream.write(b);
  }

  @Override
  public final void write(final byte[] b, final int off, final int len) throws IOException {
    this.stream.write(b, off, len);
  }

  @Override
  public final void writeBoolean(final boolean v) throws IOException {
    this.stream.writeBoolean(v);
  }

  @Override
  public final void writeByte(final int v) throws IOException {
    this.stream.writeByte(v);
  }

  @Override
  public final void writeShort(final int v) throws IOException {
    this.stream.writeShort(Short.reverseBytes((short) v));
  }

  @Override
  public final void writeChar(final int v) throws IOException {
    this.stream.writeChar(Character.reverseBytes((char) v));
  }

  @Override
  public void writeInt(final int v) throws IOException {
    this.stream.writeInt(Integer.reverseBytes(v));
  }

  @Override
  public void writeLong(final long v) throws IOException {
    this.stream.writeLong(Long.reverseBytes(v));
  }

  @Override
  public final void writeFloat(final float v) throws IOException {
    this.stream.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
  }

  @Override
  public final void writeDouble(final double v) throws IOException {
    this.stream.writeLong(Long.reverseBytes(Double.doubleToLongBits(v)));
  }

  @Override
  public final void writeBytes(@NotNull final String s) throws IOException {
    this.stream.writeBytes(s);
  }

  @Override
  public final void writeChars(@NotNull final String s) throws IOException {
    this.stream.writeChars(s);
  }

  @Override
  public void writeUTF(@NotNull final String s) throws IOException {
    final var bytes = s.getBytes(StandardCharsets.UTF_8);
    this.writeShort(bytes.length);
    this.write(bytes);
  }
}
