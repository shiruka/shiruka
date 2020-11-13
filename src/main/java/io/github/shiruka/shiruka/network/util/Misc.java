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

package io.github.shiruka.shiruka.network.util;

import io.github.shiruka.shiruka.network.misc.IntRange;
import io.netty.buffer.ByteBuf;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains misc. methods and fields.
 */
public final class Misc {

  public static final InetSocketAddress LOOPBACK_V4 = new InetSocketAddress(Inet4Address.getLoopbackAddress(), 19132);

  public static final InetSocketAddress LOOPBACK_V6 = new InetSocketAddress(Inet6Address.getLoopbackAddress(), 19132);

  public static final InetSocketAddress[] LOCAL_IP_ADDRESSES_V4 = new InetSocketAddress[20];

  public static final InetSocketAddress[] LOCAL_IP_ADDRESSES_V6 = new InetSocketAddress[20];

  static {
    Misc.LOCAL_IP_ADDRESSES_V4[0] = Misc.LOOPBACK_V4;
    Misc.LOCAL_IP_ADDRESSES_V6[0] = Misc.LOOPBACK_V6;
    for (int i = 1; i < 20; i++) {
      Misc.LOCAL_IP_ADDRESSES_V4[i] = new InetSocketAddress("0.0.0.0", 19132);
      Misc.LOCAL_IP_ADDRESSES_V6[i] = new InetSocketAddress("::0", 19132);
    }
  }

  /**
   * ctor.
   */
  private Misc() {
  }

  /**
   * obtains the given address's ip header.
   *
   * @param address the address to obtain header.
   *
   * @return ip header of the given address.
   */
  public static byte getIpHeader(@NotNull final InetSocketAddress address) {
    return Misc.getIpHeader(address.getAddress());
  }

  /**
   * obtains the given address's ip header.
   *
   * @param address the address to obtain header.
   *
   * @return ip header of the given address.
   */
  public static byte getIpHeader(@NotNull final InetAddress address) {
    final byte ipHeader;
    if (address instanceof Inet6Address) {
      ipHeader = 40;
    } else {
      ipHeader = 20;
    }
    return ipHeader;
  }

  /**
   * flips the given byte array.
   *
   * @param bytes the bytes to flip.
   */
  public static void flip(@NotNull final byte[] bytes) {
    IntStream.range(0, bytes.length)
      .forEach(i -> bytes[i] = (byte) (~bytes[i] & 0xFF));
  }

  /**
   * calculates the power of two ceiling.
   *
   * @param value the value to calculate.
   *
   * @return the power of two ceiling.
   */
  public static int powerOfTwoCeiling(int value) {
    value--;
    value |= value >> 1;
    value |= value >> 2;
    value |= value >> 4;
    value |= value >> 8;
    value |= value >> 16;
    value++;
    return value;
  }

  /**
   * write the given int range to the buffer.
   *
   * @param buffer the buffer to write.
   * @param ackQueue the ackQueue to write
   * @param mtu the mtu to write.
   */
  public static void writeIntRanges(@NotNull final ByteBuf buffer, @NotNull final Queue<IntRange> ackQueue, int mtu) {
    final var lengthIndex = buffer.writerIndex();
    buffer.writeZero(2);
    mtu -= 2;
    int count = 0;
    IntRange ackRange;
    while ((ackRange = ackQueue.poll()) != null) {
      IntRange nextRange;
      while ((nextRange = ackQueue.peek()) != null &&
        ackRange.getMaximum() + 1 == nextRange.getMinimum()) {
        ackQueue.remove();
        ackRange.setMaximum(nextRange.getMaximum());
      }
      if (ackRange.getMinimum() == ackRange.getMaximum()) {
        if (mtu < 4) {
          break;
        }
        mtu -= 4;
        buffer.writeBoolean(true);
        buffer.writeMediumLE(ackRange.getMinimum());
      } else {
        if (mtu < 7) {
          break;
        }
        mtu -= 7;
        buffer.writeBoolean(false);
        buffer.writeMediumLE(ackRange.getMinimum());
        buffer.writeMediumLE(ackRange.getMaximum());
      }
      count++;
    }
    final var finalIndex = buffer.writerIndex();
    buffer.writerIndex(lengthIndex);
    buffer.writeShort(count);
    buffer.writerIndex(finalIndex);
  }
}