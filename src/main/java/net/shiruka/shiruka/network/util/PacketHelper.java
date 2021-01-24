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

import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import java.net.*;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import net.shiruka.shiruka.network.packets.PacketOutPackInfo;
import net.shiruka.shiruka.network.packets.PacketOutPackStack;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for reading and writing packets.
 */
public final class PacketHelper {

  /**
   * ctor.
   */
  private PacketHelper() {
  }

  /**
   * reads the given buffer to parse as {@link InetSocketAddress}.
   *
   * @param buffer the buffer to parse.
   *
   * @return an address from the given buffer instance.
   */
  @NotNull
  public static InetSocketAddress readAddress(@NotNull final ByteBuf buffer) {
    final var type = buffer.readByte();
    final InetAddress address;
    final int port;
    try {
      if (type == 4) {
        final var addressBytes = new byte[4];
        buffer.readBytes(addressBytes);
        Misc.flip(addressBytes);
        address = InetAddress.getByAddress(addressBytes);
        port = buffer.readUnsignedShort();
      } else if (type == 6) {
        buffer.readShortLE();
        port = buffer.readUnsignedShort();
        buffer.readInt();
        final var addressBytes = new byte[16];
        buffer.readBytes(addressBytes);
        final var scopeId = buffer.readInt();
        address = Inet6Address.getByAddress(null, addressBytes, scopeId);
      } else {
        throw new UnsupportedOperationException("Unknown Internet Protocol version.");
      }
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException(e);
    }
    return new InetSocketAddress(address, port);
  }

  /**
   * reads the given buffer and returns {@link AsciiString}.
   *
   * @param buffer the buffer to read.
   *
   * @return an {@link AsciiString} instance.
   */
  @NotNull
  public static AsciiString readLEAsciiString(@NotNull final ByteBuf buffer) {
    final var length = buffer.readIntLE();
    final var bytes = new byte[length];
    buffer.readBytes(bytes);
    return new AsciiString(bytes);
  }

  /**
   * writes the give {@code buffer} from the give {@code array}.
   *
   * @param buffer the buffer to write.
   * @param array the array to write.
   * @param biConsumer the bi consumer to write.
   * @param <T> type of the value.
   */
  public static <T> void writeArray(@NotNull final ByteBuf buffer, @NotNull final Collection<T> array,
                                    @NotNull final BiConsumer<ByteBuf, T> biConsumer) {
    VarInts.writeUnsignedInt(buffer, array.size());
    array.forEach(val -> biConsumer.accept(buffer, val));
  }

  /**
   * writes the given array to the buffer.
   *
   * @param buffer the buffer to write.
   * @param array the array to write.
   * @param consumer the consumer to write.
   * @param <T> the type of the array.
   */
  public static <T> void writeArrayShortLE(@NotNull final ByteBuf buffer, @NotNull final Collection<T> array,
                                           @NotNull final BiConsumer<ByteBuf, T> consumer) {
    buffer.writeShortLE(array.size());
    array.forEach(val -> consumer.accept(buffer, val));
  }

  /**
   * writes the given entry into the buffer.
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeEntry(@NotNull final ByteBuf buffer, @NotNull final PacketOutPackStack.Entry entry) {
    VarInts.writeString(buffer, entry.getPackId());
    VarInts.writeString(buffer, entry.getPackVersion());
    VarInts.writeString(buffer, entry.getSubPackName());
  }

  /**
   * writes the given entry to the buffer.
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeEntry(@NotNull final ByteBuf buffer, @NotNull final PacketOutPackInfo.Entry entry) {
    VarInts.writeString(buffer, entry.getPackId());
    VarInts.writeString(buffer, entry.getPackVersion());
    buffer.writeLongLE(entry.getPackSize());
    VarInts.writeString(buffer, entry.getContentKey());
    VarInts.writeString(buffer, entry.getSubPackName());
    VarInts.writeString(buffer, entry.getContentId());
    buffer.writeBoolean(entry.isScripting());
  }

  /**
   * writes the given experiments into the buffer.
   *
   * @param buffer the buffer to write.
   * @param experiments the experiment to write.
   */
  public static void writeExperiments(@NotNull final ByteBuf buffer,
                                      @NotNull final List<PacketOutPackStack.ExperimentData> experiments) {
    buffer.writeIntLE(experiments.size());
    experiments.forEach(experiment -> {
      VarInts.writeString(buffer, experiment.getName());
      buffer.writeBoolean(experiment.isEnabled());
    });
  }

  /**
   * writes the given resource pack entry to
   *
   * @param buffer the buffer to write.
   * @param entry the entry to write.
   */
  public static void writeResourcePackEntry(@NotNull final ByteBuf buffer,
                                            @NotNull final PacketOutPackInfo.Entry entry) {
    PacketHelper.writeEntry(buffer, entry);
    buffer.writeBoolean(entry.isRaytracingCapable());
  }

  /**
   * writes the given address into the given buffer.
   *
   * @param buffer the buffer to write.
   * @param address the address to write.
   */
  private static void writeAddress(@NotNull final ByteBuf buffer, @NotNull final InetSocketAddress address) {
    final var addressBytes = address.getAddress().getAddress();
    if (address.getAddress() instanceof Inet4Address) {
      buffer.writeByte(4);
      Misc.flip(addressBytes);
      buffer.writeBytes(addressBytes);
      buffer.writeShort(address.getPort());
    } else if (address.getAddress() instanceof Inet6Address) {
      buffer.writeByte(6);
      buffer.writeShortLE(23);
      buffer.writeShort(address.getPort());
      buffer.writeInt(0);
      buffer.writeBytes(addressBytes);
      buffer.writeInt(((Inet6Address) address.getAddress()).getScopeId());
    } else {
      throw new UnsupportedOperationException("Unknown InetAddress instance");
    }
  }
}
