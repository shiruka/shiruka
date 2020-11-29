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
package io.github.shiruka.shiruka.network.server;

import io.github.shiruka.shiruka.log.Loggers;
import io.github.shiruka.shiruka.network.*;
import io.netty.channel.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * the main server class to bind a server using Netty.
 */
public final class NetServerSocket extends NetSocket implements ServerSocket {

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
  private final ConcurrentMap<InetSocketAddress, Connection<ServerSocket, ServerConnectionHandler>>
    connectionByAddress = new ConcurrentHashMap<>();

  /**
   * the exception handlers.
   */
  private final Map<String, Consumer<Throwable>> exceptionHandlers = new HashMap<>();

  /**
   * maximum connection amount for the server.
   */
  private final int maxConnections;

  /**
   * ctor.
   *
   * @param ip the ip of the server
   * @param port the port to of the server
   * @param socketListener the listener to handle custom events when a server does anything.
   * @param maxConnections the maximum connection to limit maximum connections for the server.
   */
  private NetServerSocket(@NotNull final String ip, final int port, @NotNull final SocketListener socketListener,
                          final int maxConnections) {
    super(new InetSocketAddress(ip, port), socketListener);
    this.maxConnections = maxConnections;
  }

  /**
   * initiates and execs the server.
   *
   * @param ip the ip of the server
   * @param port the port to of the server
   * @param socketListener the listener to handle custom events when a server does anything.
   * @param maxConnections the maximum connection to limit maximum connections for the server.
   *
   * @return a new {@link ServerSocket} instance.
   */
  @NotNull
  public static ServerSocket init(@NotNull final String ip, final int port,
                                  @NotNull final SocketListener socketListener, final int maxConnections) {
    Loggers.debug("Initiating the server socket...");
    final var socket = new NetServerSocket(ip, port, socketListener, maxConnections);
    socket.addExceptionHandler("DEFAULT", t ->
      Loggers.error("An exception occurred in Network system", t));
    socket.bind();
    return socket;
  }

  /**
   * initiates and execs the server.
   *
   * @param ip the ip of the server
   * @param port the port to of the server
   * @param socketListener the listener to handle custom events when a server does anything.
   *
   * @return a new {@link ServerSocket} instance.
   */
  @NotNull
  public static ServerSocket init(@NotNull final String ip, final int port,
                                  @NotNull final SocketListener socketListener) {
    return NetServerSocket.init(ip, port, socketListener, 1024);
  }

  /**
   * initiates and execs the server.
   *
   * @param ip the ip of the server
   * @param socketListener the listener to handle custom events when a server does anything.
   *
   * @return a new {@link ServerSocket} instance.
   */
  @NotNull
  public static ServerSocket init(@NotNull final String ip, @NotNull final SocketListener socketListener) {
    return NetServerSocket.init(ip, 19132, socketListener);
  }

  /**
   * initiates and execs the server.
   *
   * @param socketListener the listener to handle custom events when a server does anything.
   *
   * @return a new {@link ServerSocket} instance.
   */
  @NotNull
  public static ServerSocket init(@NotNull final SocketListener socketListener) {
    return NetServerSocket.init("127.0.0.1", socketListener);
  }

  @Override
  public void addChannel(final @NotNull Channel channel) {
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
    // check if the connection created first time.
    if (this.connectionByAddress.putIfAbsent(recipient, connection) == null) {
      connection.getConnectionHandler().sendConnectionReply1();
      this.getSocketListener().onConnectionCreation(connection);
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
  public Map<InetSocketAddress, Connection<ServerSocket, ServerConnectionHandler>> getConnectionsByAddress() {
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

  @Override
  public void removeChannel(@NotNull final Channel channel) {
    this.channels.remove(channel);
  }

  @Override
  public void removeConnection(@NotNull final InetSocketAddress address,
                               @NotNull final Connection<ServerSocket, ServerConnectionHandler> connection) {
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
    Loggers.debug("Binding the server...");
    final var completableFuture = new CompletableFuture<>();
    this.getBootstrap()
      .handler(new NetServerSocketHandler(this))
      .bind(this.getAddress())
      .addListener((ChannelFutureListener) future -> {
        if (future.cause() != null) {
          Loggers.error("An error occurs");
          Loggers.error(future.cause().getMessage());
          completableFuture.completeExceptionally(future.cause());
        }
        Loggers.debug("The server bound.");
        completableFuture.complete(future.channel());
      });
    return CompletableFuture.allOf(completableFuture);
  }

  @Override
  public void onTick() {
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
