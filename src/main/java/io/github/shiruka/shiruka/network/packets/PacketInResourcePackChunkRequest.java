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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents resource pack chunk request packets.
 */
public final class PacketInResourcePackChunkRequest extends PacketIn {

  /**
   * ctor.
   */
  public PacketInResourcePackChunkRequest() {
    super(PacketInResourcePackChunkRequest.class);
  }

  @Override
  public void read(@NotNull final ByteBuf buf, @NotNull final PlayerConnection connection) {
    final var packInfo = VarInts.readString(buf).split("_");
    final var packId = UUID.fromString(packInfo[0]);
    @Nullable final String version;
    if (packInfo.length > 1) {
      version = packInfo[1];
    } else {
      version = null;
    }
    final var chunkSize = buf.readIntLE();
    final var resourcePack = Shiruka.getPackManager().getPack(packId + "_" + version);
    if (resourcePack.isEmpty()) {
      connection.disconnect(TranslatedText.get("disconnectionScreen.resourcePack").asString());
      return;
    }
    final var pack = resourcePack.get();
    final var packet = new PacketOutResourcePackChunkData(chunkSize, pack.getChunk(1048576 * chunkSize, 1048576),
      packId, version, 1048576L * chunkSize);
    connection.sendPacket(packet);
  }
}
