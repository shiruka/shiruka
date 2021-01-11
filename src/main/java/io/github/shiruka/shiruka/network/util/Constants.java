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

package io.github.shiruka.shiruka.network.util;

/**
 * a class that contains magic numbers.
 *
 * @todo #1:5m Add more JavaDocs for constants.
 */
public final class Constants {

  /**
   * the batch magic.
   */
  public static final int BATCH_MAGIC = 0xfe;

  public static final long CC_ADDITIONAL_VARIANCE = 30;

  public static final long CC_MAXIMUM_THRESHOLD = 2000;

  public static final long CC_SYN = 10;

  public static final long CONNECTION_TIMEOUT_MS = 10000L;

  /**
   * the datagram header size.
   */
  public static final int DATAGRAM_HEADER_SIZE = 4;

  public static final byte DATA_HEADER_BYTE_LENGTH = 9;

  /**
   * it's for checking the packet is an ACK flag.
   */
  public static final byte FLAG_ACK = 0x40;

  public static final byte FLAG_CONTINUOUS_SEND = 0b00001000;

  /**
   * it's for checking the packet is an NACK flag.
   */
  public static final byte FLAG_NACK = 0x20;

  /**
   * using for check when a packet has the RakNet packet.
   */
  public static final short FLAG_VALID = 0x80;

  /**
   * the maximum encapsulated header size.
   */
  public static final int MAXIMUM_ENCAPSULATED_HEADER_SIZE = 28;

  public static final short MAXIMUM_MTU_SIZE = 1400;

  /**
   * maximum amount of ordering channels as defined in vanilla RakNet.
   */
  public static final byte MAXIMUM_ORDERING_CHANNELS = 16;

  public static final byte MAX_LOCAL_IPS = 10;

  public static final byte MAX_MESSAGE_HEADER_BYTE_LENGTH = 23;

  /**
   * the protocol version of the Minecraft game.
   */
  public static final short MINECRAFT_PROTOCOL_VERSION = 422;

  /**
   * the version of the Minecraft game.
   */
  public static final String MINECRAFT_VERSION = "1.16.201";

  public static final short MINIMUM_MTU_SIZE = 576;

  public static final byte NUM_ORDERING_CHANNELS = 32;

  /**
   * header size of the udp packets.
   */
  public static final byte UDP_HEADER_SIZE = 8;

  /**
   * the protocol version from the Mojang.
   */
  static final byte MOJANG_PROTOCOL_VERSION = 10;

  /**
   * magic number from the RakNet itself.
   */
  static final byte[] UNCONNECTED_MAGIC = new byte[]{
    0, -1, -1, 0, -2, -2, -2, -2, -3, -3, -3, -3, 18, 52, 86, 120
  };

  /**
   * ctor.
   */
  private Constants() {
  }
}
