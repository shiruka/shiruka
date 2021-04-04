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

package net.shiruka.shiruka.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.util.function.Function;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.ClientToServerHandshakePacket;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkRequestPacket;
import net.shiruka.shiruka.network.packets.ResourcePackResponsePacket;
import net.shiruka.shiruka.network.packets.ViolationWarningPacket;

/**
 * a class that represents packet registry.
 */
public final class PacketRegistry {

  /**
   * the packets.
   */
  public static final Int2ReferenceOpenHashMap<Function<ByteBuf, ShirukaPacket>> PACKETS;

  static {
    PACKETS = new Int2ReferenceOpenHashMap<>() {{
      this.put(1, LoginPacket::new);
      this.put(4, ClientToServerHandshakePacket::new);
      this.put(8, ResourcePackResponsePacket::new);
      this.put(84, ResourcePackChunkRequestPacket::new);
      this.put(129, ClientCacheStatusPacket::new);
      this.put(156, ViolationWarningPacket::new);
    }};
    PacketRegistry.PACKETS.trim();
  }

  /**
   * the ctor.
   */
  private PacketRegistry() {
  }
}
