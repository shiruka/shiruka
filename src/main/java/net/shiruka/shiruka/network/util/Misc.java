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

package net.shiruka.shiruka.network.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains misc. methods and fields.
 */
public final class Misc {

  static final InetSocketAddress[] LOCAL_IP_ADDRESSES_V4 = new InetSocketAddress[20];

  static final InetSocketAddress[] LOCAL_IP_ADDRESSES_V6 = new InetSocketAddress[20];

  /**
   * the working directory as a string
   */
  private static final String HOME = System.getProperty("user.dir");

  /**
   * the Path directory to the working dir
   */
  public static final Path HOME_PATH = Paths.get(Misc.HOME);

  private static final InetSocketAddress LOOPBACK_V4 = new InetSocketAddress(Inet4Address.getLoopbackAddress(), 19132);

  private static final InetSocketAddress LOOPBACK_V6 = new InetSocketAddress(Inet6Address.getLoopbackAddress(), 19132);

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
   * flips the given byte array.
   *
   * @param bytes the bytes to flip.
   */
  static void flip(final byte[] bytes) {
    IntStream.range(0, bytes.length)
      .forEach(i -> bytes[i] = (byte) (~bytes[i] & 0xFF));
  }

  /**
   * obtains the given address's ip header.
   *
   * @param address the address to obtain header.
   *
   * @return ip header of the given address.
   */
  private static byte getIpHeader(@NotNull final InetAddress address) {
    final byte ipHeader;
    if (address instanceof Inet6Address) {
      ipHeader = 40;
    } else {
      ipHeader = 20;
    }
    return ipHeader;
  }
}
