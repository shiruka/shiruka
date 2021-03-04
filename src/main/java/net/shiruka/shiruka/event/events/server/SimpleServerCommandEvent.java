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

package net.shiruka.shiruka.event.events.server;

import lombok.Getter;
import lombok.Setter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.event.events.server.ServerCommandEvent;
import net.shiruka.shiruka.event.events.SimpleCancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link ServerCommandEvent}.
 */
public final class SimpleServerCommandEvent extends SimpleCancellableEvent implements ServerCommandEvent {

  /**
   * the async.
   */
  @Getter
  private final boolean async = !Shiruka.isPrimaryThread();

  /**
   * the sender.
   */
  @NotNull
  @Getter
  private final CommandSender sender;

  /**
   * the command.
   */
  @NotNull
  @Getter
  @Setter
  private String command;

  /**
   * ctor.
   *
   * @param sender the sender.
   * @param command the command.
   */
  public SimpleServerCommandEvent(@NotNull final CommandSender sender, @NotNull final String command) {
    this.sender = sender;
    this.command = command;
  }
}
