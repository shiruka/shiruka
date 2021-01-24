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

import io.netty.channel.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.shiruka.shiruka.network.Connection;
import net.shiruka.shiruka.network.ConnectionState;
import net.shiruka.shiruka.network.DisconnectReason;
import net.shiruka.shiruka.network.NetSocket;
import net.shiruka.shiruka.network.util.Packets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * the main server class to bind a server using Netty.
 */
public final class NetServerSocket extends NetSocket implements ServerSocket {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("NetServerSocket");

  /**
   * blocked addresses and their unblock times.
   */
  private final ConcurrentMap<InetAddress, Long> blockedAddresses = new ConcurrentHashMap<>();

  /**
   * caches channel set.
   */
  private final Set<Channel> channels = new HashSet<>();

  /**
   * connection's address and connection itself.
   */
  private final ConcurrentMap<InetSocketAddress, Connection<ServerSocket>> connectionByAddress =
    new ConcurrentHashMap<>();

  /**
   * the exception handlers.
   */
  private final Map<String, Consumer<Throwable>> exceptionHandlers = new HashMap<>();

  /**
   * maximum connection amount for the server.
   */
  private final int maxConnections;

  /**
   * server's listener.
   */
  @NotNull
  private final ServerListener serverListener;

  /**
   * ctor.
   *
   * @param address the address of the server
   * @param serverListener the listener to handle custom events when a server does anything.
   * @param maxConnections the maximum connection to limit maximum connections for the server.
   */
  private NetServerSocket(@NotNull final InetSocketAddress address, @NotNull final ServerListener serverListener,
                          final int maxConnections) {
    super(address);
    this.maxConnections = maxConnections;
    this.serverListener = serverListener;
  }

  /**
   * initiates and execs the server.
   *
   * @param address the address of the server
   * @param serverListener the listener to handle custom events when a server does anything.
   * @param maxConnections the maximum connection to limit maximum connections for the server.
   *
   * @return a new {@link ServerSocket} instance.
   */
  @NotNull
  public static ServerSocket init(@NotNull final InetSocketAddress address,
                                  @NotNull final ServerListener serverListener, final int maxConnections) {
    NetServerSocket.LOGGER.debug("§7Initiating the server socket.");
    final var socket = new NetServerSocket(address, serverListener, maxConnections);
    socket.addExceptionHandler("DEFAULT", t ->
      NetServerSocket.LOGGER.error("§4An exception occurred in Network system", t));
    socket.bind();
    return socket;
  }

  @Override
  public void addChannel(@NotNull final Channel channel) {
    this.channels.add(channel);
  }

  @Override
  public void addExceptionHandler(@NotNull final String id, @NotNull final Consumer<Throwable> handler) {
    this.exceptionHandlers.put(id, handler);
  }

  @Override
  public void blockAddress(@NotNull final InetAddress address, final long time, @NotNull final TimeUnit unit) {
    this.blockedAddresses.put(address, System.currentTimeMillis() + unit.toMillis(time));
  }

  @Override
  public void clearExceptionHandlers() {
    this.exceptionHandlers.clear();
  }

  @Override
  public void createNewConnection(@NotNull final InetSocketAddress recipient, @NotNull final ChannelHandlerContext ctx,
                                  final int mtu, final short protocolVersion) {
    final var connection = new NetServerConnection(this, NetServerConnectionHandler::new, recipient, ctx, mtu,
      protocolVersion);
    connection.setState(ConnectionState.INITIALIZING);
    if (this.connectionByAddress.putIfAbsent(recipient, connection) == null) {
      Packets.sendConnectionReply1(connection);
      this.getServerListener().onConnectionCreation(connection);
    }
  }

  @NotNull
  @Override
  public Map<InetAddress, Long> getBlockedAddresses() {
    return Collections.unmodifiableMap(this.blockedAddresses);
  }

  @NotNull
  @Override
  public Set<Channel> getChannels() {
    return Collections.unmodifiableSet(this.channels);
  }

  @NotNull
  @Override
  public Map<InetSocketAddress, Connection<ServerSocket>> getConnectionsByAddress() {
    return Collections.unmodifiableMap(this.connectionByAddress);
  }

  @Override
  @NotNull
  public Map<String, Consumer<Throwable>> getExceptionHandlers() {
    return Collections.unmodifiableMap(this.exceptionHandlers);
  }

  @Override
  public int getMaxConnections() {
    return this.maxConnections;
  }

  @NotNull
  @Override
  public final ServerListener getServerListener() {
    return this.serverListener;
  }

  @Override
  public void removeChannel(@NotNull final Channel channel) {
    this.channels.remove(channel);
  }

  @Override
  public void removeConnection(@NotNull final InetSocketAddress address,
                               @NotNull final Connection<ServerSocket> connection) {
    this.connectionByAddress.remove(address, connection);
  }

  @Override
  public void removeExceptionHandler(@NotNull final String id) {
    this.exceptionHandlers.remove(id);
  }

  @Override
  public void unblockAddress(@NotNull final InetAddress address) {
    this.blockedAddresses.remove(address);
  }

  @Override
  public void close() {
    super.close();
    this.connectionByAddress.values().forEach(connection ->
      connection.disconnect(DisconnectReason.SHUTTING_DOWN));
    this.channels.stream()
      .map(ChannelOutboundInvoker::close)
      .forEach(ChannelFuture::syncUninterruptibly);
  }

  @NotNull
  @Override
  public CompletableFuture<Void> exec() {
    NetServerSocket.LOGGER.debug("§7Binding the server.");
    final var completableFuture = new CompletableFuture<>();
    this.getBootstrap()
      .handler(new NetServerDatagramHandler(this))
      .bind(this.getAddress())
      .addListener((ChannelFutureListener) future -> {
        if (future.cause() != null) {
          NetServerSocket.LOGGER.error("§4An error occurs");
          NetServerSocket.LOGGER.error(future.cause().getMessage());
          completableFuture.completeExceptionally(future.cause());
        }
        NetServerSocket.LOGGER.debug("§7The server bound on {}.", this.getAddress().toString());
        completableFuture.complete(future.channel());
      });
    return CompletableFuture.allOf(completableFuture);
  }

  @Override
  public void tick() {
    final var now = System.currentTimeMillis();
    this.connectionByAddress.values().forEach(connection ->
      connection.getEventLoop().execute(() -> connection.onTick(now)));
    final var it = this.blockedAddresses.values().iterator();
    long timeout;
    while (it.hasNext()) {
      timeout = it.next();
      if (timeout > 0 && timeout < now) {
        it.remove();
      }
    }
  }
}
