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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

final class NbtTest {

  @Test
  void createReader() {
    final var stream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    Nbt.createReader(stream);
  }

  @Test
  void createReaderLE() {
    final var stream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    Nbt.createReaderLE(stream);
  }

  @Test
  void createGZIPReader() throws IOException {
    final var stream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
//    Nbt.createGZIPReader(stream);
  }

  @Test
  void createNetworkReader() {
    final var stream = new ByteArrayInputStream(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    Nbt.createNetworkReader(stream);
  }

  @Test
  void createWriter() {
    final var stream = new ByteArrayOutputStream();
    Nbt.createWriter(stream);
  }

  @Test
  void createWriterLE() {
    final var stream = new ByteArrayOutputStream();
    Nbt.createWriterLE(stream);
  }

  @Test
  void createGZIPWriter() throws IOException {
    final var stream = new ByteArrayOutputStream();
    Nbt.createGZIPWriter(stream);
  }

  @Test
  void createNetworkWriter() {
    final var stream = new ByteArrayOutputStream();
    Nbt.createNetworkWriter(stream);
  }

  @Test
  void testToString() {
    MatcherAssert.assertThat(
      "Couldn't parse the double value into a string!",
      Nbt.toString(10.0d),
      new IsEqual<>("10.0d"));
  }

  @Test
  void copy() {
    Nbt.copy(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
  }

  @Test
  void indent() {
    Nbt.indent("Test string for indent");
  }

  @Test
  void printHexBinary() {
    Nbt.printHexBinary(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
  }
}