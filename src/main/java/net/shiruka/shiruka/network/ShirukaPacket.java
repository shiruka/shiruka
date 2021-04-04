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

import com.whirvis.jraknet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.shiruka.shiruka.network.packets.ClientCacheStatusPacket;
import net.shiruka.shiruka.network.packets.ClientToServerHandshakePacket;
import net.shiruka.shiruka.network.packets.DisconnectPacket;
import net.shiruka.shiruka.network.packets.EntityRemovePacket;
import net.shiruka.shiruka.network.packets.LoginPacket;
import net.shiruka.shiruka.network.packets.NetworkSettingsPacket;
import net.shiruka.shiruka.network.packets.PackInfoPacket;
import net.shiruka.shiruka.network.packets.PackStackPacket;
import net.shiruka.shiruka.network.packets.PlayStatusPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkDataPacket;
import net.shiruka.shiruka.network.packets.ResourcePackChunkRequestPacket;
import net.shiruka.shiruka.network.packets.ResourcePackDataInfoPacket;
import net.shiruka.shiruka.network.packets.ResourcePackResponsePacket;
import net.shiruka.shiruka.network.packets.ServerToClientHandshakePacket;
import net.shiruka.shiruka.network.packets.StartGamePacket;
import net.shiruka.shiruka.network.packets.ViolationWarningPacket;
import org.jetbrains.annotations.NotNull;

/**
 * a generic abstract packet for Shiru ka packets.
 */
@RequiredArgsConstructor
public abstract class ShirukaPacket extends Packet {

  /**
   * the id of the {@link NetworkSettingsPacket}.
   */
  protected static final int ID_NETWORK_SETTINGS = 143;

  /**
   * the id of the {@link ClientCacheStatusPacket}.
   */
  protected static final int ID_CLIENT_CACHE_STATUS = 129;

  /**
   * the id of the {@link ClientToServerHandshakePacket}.
   */
  protected static final int ID_CLIENT_TO_SERVER_HANDSHAKE = 4;

  /**
   * the id of the {@link DisconnectPacket}.
   */
  protected static final int ID_DISCONNECT = 5;

  /**
   * the id of the {@link LoginPacket}.
   */
  protected static final int ID_LOGIN = 1;

  /**
   * the id of the {@link PackStackPacket}.
   */
  protected static final int ID_PACK_INFO = 6;

  /**
   * the id of the {@link PackStackPacket}.
   */
  protected static final int ID_PACK_STACK = 7;

  /**
   * the id of the {@link PlayStatusPacket}.
   */
  protected static final int ID_PLAY_STATUS = 2;

  /**
   * the id of the {@link EntityRemovePacket}.
   */
  protected static final int ID_REMOVE_ENTITY = 14;

  /**
   * the id of the {@link ResourcePackChunkDataPacket}.
   */
  protected static final int ID_RESOURCE_PACK_CHUNK_DATA = 83;

  /**
   * the id of the {@link ResourcePackChunkRequestPacket}.
   */
  protected static final int ID_RESOURCE_PACK_CHUNK_REQUEST = 84;

  /**
   * the id of the {@link ResourcePackDataInfoPacket}.
   */
  protected static final int ID_RESOURCE_PACK_DATA_INFO = 82;

  /**
   * the id of the {@link ResourcePackResponsePacket}.
   */
  protected static final int ID_RESOURCE_PACK_RESPONSE = 8;

  /**
   * the id of the {@link ServerToClientHandshakePacket}.
   */
  protected static final int ID_SERVER_TO_CLIENT_HANDSHAKE = 3;

  /**
   * the id of the {@link StartGamePacket}.
   */
  protected static final int ID_START_GAME = 11;

  /**
   * the id of the {@link ViolationWarningPacket}.
   */
  protected static final int ID_VIOLATION_WARNING = 156;

  /**
   * the id.
   */
  private final int id;

  /**
   * the client id.
   */
  private int clientId;

  /**
   * the sender id.
   */
  private int senderId;

  /**
   * ctor.
   *
   * @param id the id.
   * @param original the original.
   */
  protected ShirukaPacket(final int id, @NotNull final ByteBuf original) {
    super(original);
    this.id = id;
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
   * obtains the client id.
   *
   * @return client i.d
   */
  public final int getClientId() {
    return this.clientId;
  }

  /**
   * sets the client id.
   *
   * @param clientId client id to set.
   */
  public final void setClientId(final int clientId) {
    this.clientId = clientId;
  }

  /**
   * obtains the id.
   *
   * @return id.
   */
  public final int getId() {
    return this.id;
  }

  /**
   * obtains the sender id.
   *
   * @return sender i.d
   */
  public final int getSenderId() {
    return this.senderId;
  }

  /**
   * sets the sender id.
   *
   * @param senderId sender id to set.
   */
  public final void setSenderId(final int senderId) {
    this.senderId = senderId;
  }

  /**
   * writes the give {@code packet} from the give {@code array}.
   *
   * @param array the array to write.
   * @param consumer the bi consumer to write.
   * @param <T> type of the value.
   */
  public final <T> void writeArray(@NotNull final Collection<T> array, @NotNull final Consumer<T> consumer) {
    VarInts.writeUnsignedInt(this.buffer(), array.size());
    array.forEach(consumer);
  }

  /**
   * writes the given array to the packet.
   *
   * @param array the array to write.
   * @param consumer the consumer to write.
   * @param <T> the type of the array.
   */
  public final <T> void writeArrayShortLE(@NotNull final Collection<T> array, @NotNull final Consumer<T> consumer) {
    this.writeShortLE(array.size());
    array.forEach(consumer);
  }

  /**
   * writes the given {@code bytes}.
   *
   * @param bytes the bytes to write.
   */
  public final void writeByteArray(final byte[] bytes) {
    VarInts.writeUnsignedInt(this.buffer(), bytes.length);
    this.buffer().writeBytes(bytes);
  }

  /**
   * writes the given entry into the packet.
   *
   * @param entry the entry to write.
   */
  public final void writeEntry(@NotNull final PackStackPacket.Entry entry) {
    VarInts.writeString(this.buffer(), entry.getPackId());
    VarInts.writeString(this.buffer(), entry.getPackVersion());
    VarInts.writeString(this.buffer(), entry.getSubPackName());
  }

  /**
   * writes the given entry to the packet.
   *
   * @param entry the entry to write.
   */
  public final void writeEntry(@NotNull final PackInfoPacket.Entry entry) {
    VarInts.writeString(this.buffer(), entry.getPackId());
    VarInts.writeString(this.buffer(), entry.getPackVersion());
    this.writeLongLE(entry.getPackSize());
    VarInts.writeString(this.buffer(), entry.getContentKey());
    VarInts.writeString(this.buffer(), entry.getSubPackName());
    VarInts.writeString(this.buffer(), entry.getContentId());
    this.writeBoolean(entry.isScripting());
  }

  /**
   * writes the given experiments into the packet.
   *
   * @param experiments the experiment to write.
   */
  public final void writeExperiments(@NotNull final List<PackStackPacket.ExperimentData> experiments) {
    this.writeIntLE(experiments.size());
    experiments.forEach(experiment -> {
      VarInts.writeString(this.buffer(), experiment.getName());
      this.writeBoolean(experiment.isEnabled());
    });
  }

  /**
   * writes the given resource pack entry to.
   *
   * @param entry the entry to write.
   */
  public final void writeResourcePackEntry(@NotNull final PackInfoPacket.Entry entry) {
    this.writeEntry(entry);
    this.writeBoolean(entry.isRaytracingCapable());
  }

  /**
   * decodes the packet to receive from clients.
   */
  public void decode() {
    // ignored.
  }

  /**
   * encodes the packet to send to clients.
   */
  public void encode() {
    // ignored.
  }

  /**
   * handles the packet.
   *
   * @param handler the handler to handle.
   */
  public void handle(@NotNull final PacketHandler handler) {
  }
}
