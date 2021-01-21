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

package net.shiruka.shiruka.network.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.shiruka.shiruka.network.Connection;
import net.shiruka.shiruka.network.Socket;
import org.jetbrains.annotations.NotNull;

/**
 * a class that covers server socket's methods.
 */
public interface ServerSocket extends Socket {

  /**
   * adds the given channel into the channel list.
   *
   * @param channel the channel to add into the list.
   */
  void addChannel(@NotNull Channel channel);

  /**
   * adds the given id and handler into the exception handler map.
   *
   * @param id the id to add.
   * @param handler the handler to add.
   */
  void addExceptionHandler(@NotNull String id, @NotNull Consumer<Throwable> handler);

  /**
   * blocks the given address.
   *
   * @param address the address to block.
   * @param time the time to unblock.
   * @param unit the unit to parse the time.
   */
  void blockAddress(@NotNull InetAddress address, long time, @NotNull TimeUnit unit);

  /**
   * blocks the given address.
   *
   * @param address the address to block.
   * @param time the time to unblock.
   */
  default void blockAddress(@NotNull final InetAddress address, final long time) {
    this.blockAddress(address, time, TimeUnit.MILLISECONDS);
  }

  /**
   * clears all the exception handlers.
   */
  void clearExceptionHandlers();

  /**
   * create a new connection and if it's first time for the given recipient the method also
   * sends open connection reply 1 packet.
   *
   * @param recipient the recipient to create.
   * @param ctx the context to create.
   * @param mtu the mtu to create.
   * @param protocolVersion the protocol version to create.
   */
  void createNewConnection(@NotNull InetSocketAddress recipient, @NotNull ChannelHandlerContext ctx, int mtu,
                           short protocolVersion);

  /**
   * list of the blocked addresses.
   *
   * @return the key is block address and, the value is when the blocked address will unblock.
   */
  @NotNull
  Map<InetAddress, Long> getBlockedAddresses();

  /**
   * the channel list.
   *
   * @return the channels set.
   */
  @NotNull
  Set<Channel> getChannels();

  /**
   * list of the connection by address.
   *
   * @return the key is connection's address and, the value is connection itself.
   */
  @NotNull
  Map<InetSocketAddress, Connection<ServerSocket>> getConnectionsByAddress();

  /**
   * obtains all exception handlers.
   *
   * @return all exception handlers.
   */
  @NotNull
  Map<String, Consumer<Throwable>> getExceptionHandlers();

  /**
   * maximum connection amount of the server.
   *
   * @return maximum connection count to limit the server.
   */
  int getMaxConnections();

  /**
   * socket's listener.
   *
   * @return the listener to handler socket's events.
   */
  @NotNull
  ServerListener getServerListener();

  /**
   * removes the given channel from the channel list.
   *
   * @param channel the channel to remove from the list.
   */
  void removeChannel(@NotNull Channel channel);

  /**
   * removes the connection from {@link ServerSocket#getConnectionsByAddress()}.
   *
   * @param address the address to remove.
   * @param connection the connection to remove.
   */
  void removeConnection(@NotNull InetSocketAddress address, @NotNull Connection<ServerSocket> connection);

  /**
   * removes the exception handler from the given id
   *
   * @param id the id to remove.
   */
  void removeExceptionHandler(@NotNull String id);

  /**
   * unblock the given address.
   *
   * @param address the address to unblock.
   */
  void unblockAddress(@NotNull InetAddress address);
}
