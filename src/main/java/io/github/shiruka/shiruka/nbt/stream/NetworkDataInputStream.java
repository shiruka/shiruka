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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link LittleEndianDataInputStream}.
 */
public final class NetworkDataInputStream extends LittleEndianDataInputStream {

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public NetworkDataInputStream(@NotNull final InputStream stream) {
    super(stream);
  }

  /**
   * ctor.
   *
   * @param stream the stream.
   */
  public NetworkDataInputStream(@NotNull final DataInputStream stream) {
    super(stream);
  }

  @Override
  public int readInt() throws IOException {
    return VarInts.readInt(this.stream);
  }

  @Override
  public long readLong() throws IOException {
    return VarInts.readLong(this.stream);
  }

  @Override
  @Nonnull
  public String readUTF() throws IOException {
    final var length = VarInts.readUnsignedInt(this.stream);
    final var bytes = new byte[length];
    this.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
