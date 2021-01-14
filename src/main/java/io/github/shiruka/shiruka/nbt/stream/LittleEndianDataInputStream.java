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

package io.github.shiruka.shiruka.nbt.stream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link DataInput}.
 */
public class LittleEndianDataInputStream implements DataInput, Closeable {

  /**
   * the stream.
   */
  @NotNull
  protected final DataInputStream stream;

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public LittleEndianDataInputStream(@NotNull final InputStream stream) {
    this.stream = new DataInputStream(stream);
  }

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public LittleEndianDataInputStream(@NotNull final DataInputStream stream) {
    this.stream = stream;
  }

  @Override
  public final void close() throws IOException {
    this.stream.close();
  }

  @Override
  public final void readFully(final byte[] b) throws IOException {
    this.stream.readFully(b);
  }

  @Override
  public final void readFully(final byte[] b, final int off, final int len) throws IOException {
    this.stream.readFully(b, off, len);
  }

  @Override
  public final int skipBytes(final int n) throws IOException {
    return this.stream.skipBytes(n);
  }

  @Override
  public final boolean readBoolean() throws IOException {
    return this.stream.readBoolean();
  }

  @Override
  public final byte readByte() throws IOException {
    return this.stream.readByte();
  }

  @Override
  public final int readUnsignedByte() throws IOException {
    return this.stream.readUnsignedByte();
  }

  @Override
  public final short readShort() throws IOException {
    return Short.reverseBytes(this.stream.readShort());
  }

  @Override
  public final int readUnsignedShort() throws IOException {
    return Short.toUnsignedInt(Short.reverseBytes(this.stream.readShort()));
  }

  @Override
  public final char readChar() throws IOException {
    return Character.reverseBytes(this.stream.readChar());
  }

  @Override
  public int readInt() throws IOException {
    return Integer.reverseBytes(this.stream.readInt());
  }

  @Override
  public long readLong() throws IOException {
    return Long.reverseBytes(this.stream.readLong());
  }

  @Override
  public final float readFloat() throws IOException {
    return Float.intBitsToFloat(Integer.reverseBytes(this.stream.readInt()));
  }

  @Override
  public final double readDouble() throws IOException {
    return Double.longBitsToDouble(Long.reverseBytes(this.stream.readLong()));
  }

  @Override
  @Deprecated
  public final String readLine() throws IOException {
    return this.stream.readLine();
  }

  @NotNull
  @Override
  public String readUTF() throws IOException {
    final var bytes = new byte[this.readUnsignedShort()];
    this.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
