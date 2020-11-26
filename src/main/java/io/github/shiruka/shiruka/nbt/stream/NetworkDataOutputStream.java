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

import io.github.shiruka.shiruka.misc.VarInts;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link LittleEndianDataOutputStream}.
 */
public final class NetworkDataOutputStream extends LittleEndianDataOutputStream {

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public NetworkDataOutputStream(@NotNull final OutputStream stream) {
    super(stream);
  }

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public NetworkDataOutputStream(final DataOutputStream stream) {
    super(stream);
  }

  @Override
  public void writeInt(final int v) throws IOException {
    VarInts.writeInt(this.stream, v);
  }

  @Override
  public void writeLong(final long v) throws IOException {
    VarInts.writeLong(this.stream, v);
  }

  @Override
  public void writeUTF(final String s) throws IOException {
    final var bytes = s.getBytes(StandardCharsets.UTF_8);
    VarInts.writeUnsignedInt(this.stream, bytes.length);
    this.write(bytes);
  }
}
