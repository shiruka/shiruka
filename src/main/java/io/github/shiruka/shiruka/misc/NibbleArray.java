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

import io.netty.buffer.ByteBuf;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * this object represents a data format that stores values into half-bytes called nibbles.
 */
public final class NibbleArray {

  /**
   * 8 bytes compressed into one long to represent 16 nibbles.
   */
  public static final int BYTES_PER_LONG = 8;

  /**
   * the array of nibbles.
   */
  @NotNull
  private final AtomicLongArray nibbles;

  /**
   * ctor.
   *
   * @param nibbles the nibbles.
   */
  public NibbleArray(@NotNull final AtomicLongArray nibbles) {
    this.nibbles = nibbles;
  }

  /**
   * ctor.
   *
   * @param length the length of the nibble array.
   */
  public NibbleArray(final int length) {
    this.nibbles = new AtomicLongArray(length / NibbleArray.BYTES_PER_LONG);
  }

  /**
   * obtains the byte nibble for the given index 0-4095.
   *
   * @param array the array to obtain the nibble.
   * @param idx the index to read the nibble.
   *
   * @return the nibble.
   */
  public static byte getNibble(final byte[] array, final int idx) {
    return (byte) ((idx & 1) == 0 ? array[idx >> 1] & 0x0F : array[idx >> 1] >> 4 & 0x0F);
  }

  /**
   * sets the nibble at the given index 0-4095 with the given nibble data.
   *
   * @param array the array to set.
   * @param idx the index to set the nibble.
   * @param nibble the nibble data to set.
   */
  public static void setNibble(final byte[] array, final int idx, final byte nibble) {
    final var i = idx >> 1;
    if ((idx & 1) == 0) {
      array[i] = (byte) (array[i] & 0xF0 | nibble & 0x0F);
    } else {
      array[i] = (byte) (array[i] & 0x0F | nibble << 4 & 0xF0);
    }
  }

  /**
   * fills all nibble indices of the array with the given nibble value.
   *
   * @param value the value to fill.
   */
  public void fill(final byte value) {
    final var newValue = value << 4 | value & 0xFF;
    final var splice = IntStream.iterate(0, i -> i < 64, i -> i + 8)
      .map(i -> newValue << i)
      .reduce(0, (a, b) -> a | b);
    IntStream.range(0, this.nibbles.length())
      .forEach(i -> this.nibbles.set(i, splice));
  }

  /**
   * obtains the byte at the given position index in the nibble array.
   *
   * @param position the nibble index.
   *
   * @return the nibble value at the indexç
   */
  public byte getByte(final int position) {
    final var nibblePosition = position / 2;
    final var splice = this.nibbles.get(nibblePosition / NibbleArray.BYTES_PER_LONG);
    final var shift = nibblePosition % NibbleArray.BYTES_PER_LONG << 3;
    final var shifted = splice >> shift;
    if ((position & 1) == 0) {
      return (byte) (shifted & 0x0F);
    }
    return (byte) (shifted >> 4 & 0x0F);
  }

  /**
   * obtains the number of nibbles multiplied by two.
   *
   * @return the length of the nibbles * 2.
   */
  public int getLength() {
    return this.nibbles.length() * NibbleArray.BYTES_PER_LONG << 1;
  }

  /**
   * loads the bytes from the given nibble array into the long striped splice array.
   *
   * @param bytes the bytes to load.
   */
  public void read(final byte[] bytes) {
    var cur = 0;
    var shift = 0;
    var splice = 0;
    for (final var b : bytes) {
      cur |= (long) b << shift;
      shift += 8;
      if (shift == 64) {
        this.nibbles.set(splice, cur);
        cur = 0;
        shift = 0;
        splice++;
      }
    }
  }

  /**
   * sets the nibble at the given position index to the given nibble value.
   *
   * @param position the nibble index.
   * @param value the nibble value.
   */
  public void setByte(final int position, final byte value) {
    final var nibblePosition = position / 2;
    final var spliceIndex = nibblePosition >> 3;
    final var shift = nibblePosition % NibbleArray.BYTES_PER_LONG << 3;
    long oldSpice; // easter egg (play Old Spice theme)
    long newSplice;
    if ((position & 1) == 0) {
      do {
        oldSpice = this.nibbles.get(spliceIndex);
        final var newByte = oldSpice >>> shift & 0xF0 | value;
        newSplice = oldSpice & ~(0xFFL << shift) | newByte << shift;
      }
      while (!this.nibbles.compareAndSet(spliceIndex, oldSpice, newSplice));
    } else {
      final var shiftedVal = value << 4;
      do {
        oldSpice = this.nibbles.get(spliceIndex);
        final var newByte = oldSpice >>> shift & 0x0F | shiftedVal;
        newSplice = oldSpice & ~(0xFFL << shift) | newByte << shift;
      }
      while (!this.nibbles.compareAndSet(spliceIndex, oldSpice, newSplice));
    }
  }

  /**
   * writes the data contained in the underlying nibble array to the given byte buffer.
   *
   * @param buf the buffer to write.
   */
  public void write(@NotNull final ByteBuf buf) {
    final var len = this.nibbles.length();
    for (var i = 0; i < len; i++) {
      final var l = this.nibbles.get(i);
      for (var shift = 0; shift < 64; shift += 8) {
        final var shifted = l >> shift;
        final var b = (byte) (shifted & 0xFF);
        buf.writeByte(b);
      }
    }
  }

  /**
   * writes the given byte array with the data stored in this spliced nibble array.
   *
   * @return the data written from this nibble array.
   */
  public byte[] write() {
    final var bytes = new byte[this.nibbles.length() * NibbleArray.BYTES_PER_LONG];
    final var len = this.nibbles.length();
    for (int i = 0; i < len; i++) {
      final var l = this.nibbles.get(i);
      var offset = 0;
      for (var shift = 0; shift < 64; shift += 8, offset++) {
        final var shifted = l >> shift;
        final var b = (byte) (shifted & 0xFF);
        bytes[i * NibbleArray.BYTES_PER_LONG + offset] = b;
      }
    }
    return bytes;
  }
}
