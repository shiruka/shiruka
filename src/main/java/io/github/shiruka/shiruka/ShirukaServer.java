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

package io.github.shiruka.shiruka;

import io.github.shiruka.api.Server;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.network.ServerSocket;
import io.github.shiruka.shiruka.network.SocketListener;
import io.github.shiruka.shiruka.network.impl.ShirukaSocketListener;
import io.github.shiruka.shiruka.network.server.NetServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server {

  /**
   * obtains the Shiru ka server's version
   */
  @NotNull
  public static final String VERSION = "1.0.0";

  /**
   * the socket listener.
   */
  @NotNull
  private final SocketListener listener = new ShirukaSocketListener(this);

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the tick.
   */
  @NotNull
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * the socket.
   */
  @Nullable
  private ServerSocket socket;

  /**
   * ctor.
   */
  private ShirukaServer() {
  }

  /**
   * initiates the server with ip, port and max player.
   *
   * @param ip the ip to initiate.
   * @param port the port to initiate.
   * @param maxPlayer the maximum player to initiate.
   *
   * @return a new {@link Server} instance.
   */
  public static Server init(@NotNull final String ip, final int port, final int maxPlayer) {
    final var server = new ShirukaServer();
    final var socket = NetServerSocket.init(ip, port, server.listener, maxPlayer);
    server.setSocket(socket);
    return server;
  }

  @Override
  public boolean isInShutdownState() {
    return !this.running.get();
  }

  @Override
  public void runCommand(@NotNull final String command) {
  }

  @Override
  public void startServer() {
    this.running.set(true);
    this.tick.start();
  }

  @Override
  public void stopServer() {
    this.running.set(false);
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    ServerThreadPool.shutdownAll();
    System.exit(0);
  }

  /**
   * sets the server's socket.
   *
   * @param socket the socket to set.
   */
  private void setSocket(@NotNull final ServerSocket socket) {
    this.socket = socket;
  }
}
