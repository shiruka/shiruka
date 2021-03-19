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

import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.protocol.Reliability;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.Tick;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.entity.entities.ShirukaPlayer;
import net.shiruka.shiruka.text.TranslatedTexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents network connections.
 */
@RequiredArgsConstructor
public final class NetworkManager implements Tick {

  /**
   * the join attempts this tick.
   */
  private static int joinAttemptsThisTick;

  /**
   * the old tick.
   */
  private static int oldTick;

  /**
   * the connection.
   */
  @NotNull
  @Getter
  private final RakNetClientPeer client;

  /**
   * the login listener.
   */
  @Getter
  private final LoginListener loginListener = new LoginListener(this);

  /**
   * the packet handler.
   */
  private final AtomicReference<PacketHandler> packetHandler = new AtomicReference<>(this.loginListener);

  /**
   * the queued packets.
   */
  private final PriorityQueue<QueuedPacket> queuedPackets = new ObjectArrayFIFOQueue<>();

  /**
   * the server.
   */
  @NotNull
  @Getter
  private final ShirukaServer server;

  /**
   * the disconnect message.
   */
  @Nullable
  private Text disconnectMessage;

  /**
   * the disconnection handled.
   */
  private boolean disconnectionHandled;

  /**
   * closes the network connection.
   *
   * @param closeMessage the close message to close.
   */
  public void close(@Nullable final Text closeMessage) {
    this.queuedPackets.clear();
    if (this.client.getChannel().isOpen()) {
      this.client.getChannel().close();
      this.disconnectMessage = closeMessage;
    }
  }

  /**
   * obtains the packet handler.
   *
   * @return packet handler.
   */
  @NotNull
  public PacketHandler getPacketHandler() {
    return this.packetHandler.get();
  }

  /**
   * sets the {@link #packetHandler}.
   *
   * @param handler the handler to set.
   */
  public void setPacketHandler(@NotNull final PacketHandler handler) {
    this.packetHandler.set(handler);
  }

  /**
   * gets the player.
   *
   * @return player.
   */
  @NotNull
  public Optional<ShirukaPlayer> getPlayer() {
    final var handler = this.getPacketHandler();
    if (handler instanceof PlayerConnection) {
      return Optional.of(((PlayerConnection) handler).getPlayer());
    }
    return Optional.empty();
  }

  /**
   * obtains the address.
   *
   * @return address.
   */
  @NotNull
  public InetSocketAddress getSocketAddress() {
    return this.client.getAddress();
  }

  /**
   * handles the disconnection of the client.
   */
  public void handleDisconnection() {
    if (this.isConnected()) {
      return;
    }
    if (this.disconnectionHandled) {
      return;
    }
    this.disconnectionHandled = true;
    final var handler = this.getPacketHandler();
    handler.onDisconnect(Objects.requireNonNullElse(this.disconnectMessage, TranslatedTexts.DISCONNECTED_NO_REASON));
    this.queuedPackets.clear();
    if (handler instanceof PlayerConnection) {
      final var playerConnection = (PlayerConnection) handler;
      final var player = playerConnection.getPlayer();
      Shiruka.getEventManager().playerConnectionClose(player.getAddress(), player.getName(), player.getUniqueId(),
        player.getXboxUniqueId()).callEvent();
    } else if (handler instanceof LoginListener) {
      final var loginListener = (LoginListener) handler;
      final var profile = loginListener.getProfile();
      if (profile != null) {
        Shiruka.getEventManager().playerConnectionClose(this.getSocketAddress(), profile.getName(),
          profile.getUniqueId(), profile.getXboxUniqueId()).callEvent();
      }
    }
  }

  /**
   * polls all packets in {@link #queuedPackets} and handles them.
   */
  public void handleQueuedPackets() {
    if (this.queuedPackets.isEmpty()) {
      return;
    }
    var toBatch = new ObjectArrayList<QueuedPacket>();
    while (!this.queuedPackets.isEmpty()) {
      final var packet = this.queuedPackets.dequeue();
      if (!packet.getClass().isAnnotationPresent(NoEncryption.class)) {
        toBatch.add(packet);
        continue;
      }
      if (!toBatch.isEmpty()) {
        this.sendWrapped(toBatch);
        toBatch = new ObjectArrayList<>();
      }
      this.sendWrapped(Collections.singletonList(packet));
    }
    if (!toBatch.isEmpty()) {
      this.sendWrapped(toBatch);
    }
  }

  /**
   * runs when the player just created.
   *
   * @param player the player to initialize.
   */
  public void initialize(@NotNull final ShirukaPlayer player) {
    this.setPacketHandler(player.getPlayerConnection());
    this.server.getPlayerList().initialize(player);
  }

  /**
   * checks if the client's channel is open.
   *
   * @return {@code true} if the client's channel is open.
   */
  public boolean isConnected() {
    return this.client.getChannel().isOpen();
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   */
  public void sendPacket(@NotNull final ShirukaPacket packet) {
    this.sendPacket(new QueuedPacket(packet));
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   * @param listener the listener to send.
   */
  public void sendPacket(@NotNull final ShirukaPacket packet,
                         @Nullable final GenericFutureListener<? extends Future<? super Void>> listener) {
    this.sendPacket(new QueuedPacket(packet, listener));
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   */
  public void sendPacket(@NotNull final QueuedPacket packet) {
    this.queuedPackets.enqueue(packet);
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   */
  public void sendPacketImmediately(@NotNull final ShirukaPacket packet) {
    this.sendPacketImmediately(packet, null);
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   * @param listener the listener to send.
   */
  public void sendPacketImmediately(@NotNull final ShirukaPacket packet,
                                    @Nullable final GenericFutureListener<? extends Future<? super Void>> listener) {
    this.sendPacketImmediately(new QueuedPacket(packet, listener));
  }

  /**
   * sends the given {@code packet} to {@link #client}.
   *
   * @param packet the packet to send.
   */
  public void sendPacketImmediately(@NotNull final QueuedPacket packet) {
    this.sendWrapped(Collections.singletonList(packet));
  }

  @Override
  public void tick() {
    if (this.client.isConnected() && Shiruka.isPrimaryThread()) {
      this.handleQueuedPackets();
    }
    if (NetworkManager.oldTick != this.server.getTick().getCurrentTick()) {
      NetworkManager.oldTick = this.server.getTick().getCurrentTick();
      NetworkManager.joinAttemptsThisTick = 0;
    }
    final var handler = this.getPacketHandler();
    if (handler instanceof LoginListener &&
      NetworkManager.joinAttemptsThisTick++ < ServerConfig.maxLoginPerTick) {
      handler.tick();
    }
    if (handler instanceof PlayerConnection) {
      handler.tick();
    }
  }

  /**
   * sends the wrapped packets to the connection.
   *
   * @param packets the packets to send.
   */
  private void sendWrapped(@NotNull final List<QueuedPacket> packets) {
    final var compressed = Unpooled.buffer();
    try {
      Protocol.serialize(compressed, packets, Deflater.DEFAULT_COMPRESSION);
      final var finalListener = packets.size() == 1
        ? packets.get(0).getListener()
        : null;
      this.sendWrapped(compressed, finalListener);
    } catch (final Exception e) {
      Shiruka.getLogger().error("Unable to compress packets", e);
    } finally {
      compressed.release();
    }
  }

  /**
   * sends the given compressed packet to the connection.
   *
   * @param compressed the compressed packet to send.
   * @param listener the listener to send.
   */
  private synchronized void sendWrapped(@NotNull final ByteBuf compressed,
                                        @Nullable final GenericFutureListener<? extends Future<? super Void>> listener) {
    final var packet = Unpooled.buffer(compressed.readableBytes() + 9);
    packet.writeByte(0xfe);
    packet.writeBytes(compressed);
    this.client.sendMessage(Reliability.RELIABLE_ORDERED, packet, listener);
  }
}
