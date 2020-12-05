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

package io.github.shiruka.shiruka.network.packet;

import io.github.shiruka.shiruka.network.Connection;
import io.github.shiruka.shiruka.network.ConnectionState;
import io.github.shiruka.shiruka.network.ServerConnectionHandler;
import io.github.shiruka.shiruka.network.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ConditionalPacket}.
 */
public abstract class StatefulPacket extends ConditionalPacket {

  /**
   * the simple implementation for {@link Predicate} that always returns {@code true}.
   */
  @NotNull
  private static final Predicate<Connection<ServerSocket, ServerConnectionHandler>> FREE = connection -> true;

  /**
   * the check.
   */
  @NotNull
  private final Predicate<Connection<ServerSocket, ServerConnectionHandler>> check;

  /**
   * the states.
   */
  @NotNull
  private final Collection<ConnectionState> states;

  /**
   * ctor.
   *
   * @param id the id.
   * @param check the check.
   * @param states the states.
   */
  protected StatefulPacket(final int id,
                           @NotNull final Predicate<Connection<ServerSocket, ServerConnectionHandler>> check,
                           @NotNull final Collection<ConnectionState> states) {
    super(id);
    this.check = check;
    this.states = Collections.unmodifiableCollection(states);
  }

  /**
   * ctor.
   *
   * @param id the id.
   * @param check the check.
   * @param states the states.
   */
  protected StatefulPacket(final int id,
                           @NotNull final Predicate<Connection<ServerSocket, ServerConnectionHandler>> check,
                           @NotNull final ConnectionState... states) {
    this(id, check, Arrays.asList(states));
  }

  /**
   * ctpr.
   *
   * @param id the id.
   * @param states the states.
   */
  protected StatefulPacket(final int id, @NotNull final List<ConnectionState> states) {
    this(id, StatefulPacket.FREE, states);
  }

  /**
   * ctor.
   *
   * @param id the id.
   * @param states the states.
   */
  protected StatefulPacket(final int id, @NotNull final ConnectionState... states) {
    this(id, Arrays.asList(states));
  }

  @Override
  public final boolean canAccess(@NotNull final Connection<ServerSocket, ServerConnectionHandler> connection) {
    return this.states.contains(connection.getState()) && this.check.test(connection);
  }
}
