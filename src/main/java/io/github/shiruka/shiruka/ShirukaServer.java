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
import io.github.shiruka.api.world.WorldLoader;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import io.github.shiruka.shiruka.log.Loggers;
import io.github.shiruka.shiruka.network.ServerSocket;
import io.github.shiruka.shiruka.network.impl.ShirukaSocketListener;
import io.github.shiruka.shiruka.network.server.NetServerSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

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
   * the server's description.
   */
  @NotNull
  private final String description;

  /**
   * the loader.
   */
  @NotNull
  private final WorldLoader loader;

  /**
   * if the server is running.
   */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * the socket.
   */
  @NotNull
  private final ServerSocket socket;

  /**
   * the tick.
   */
  @NotNull
  private final ShirukaTick tick = new ShirukaTick(this);

  /**
   * ctor.
   *
   * @param address the server's address.
   * @param maxPlayer the server's maximum connection size.
   * @param description the server's description.
   * @param loader the world loader.
   */
  public ShirukaServer(@NotNull final InetSocketAddress address, final int maxPlayer,
                       @NotNull final String description, @NotNull final WorldLoader loader) {
    this.description = description;
    this.loader = loader;
    this.socket = NetServerSocket.init(address, new ShirukaSocketListener(this), maxPlayer);
  }

  @Override
  public int getMaxPlayerCount() {
    return this.socket.getMaxConnections();
  }

  @Override
  public int getPlayerCount() {
    return this.socket.getConnectionsByAddress().size();
  }

  @NotNull
  @Override
  public String getServerDescription() {
    return this.description;
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
    Loggers.log("Loading plugins...");
    // TODO Load plugins here.
    Loggers.log("Enabling startup plugins...");
    // TODO enable plugins which set PluginLoadOrder as STARTUP.
    Loggers.log("Loading worlds...");
    this.loader.loadAll();
    Loggers.log("Enabling post world plugins...");
    // TODO enable plugins which set PluginLoadOrder as POST_WORLD.
  }

  @Override
  public void stopServer() {
    this.running.set(false);
    try {
      this.socket.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    ServerThreadPool.shutdownAll();
    System.exit(0);
  }
}
