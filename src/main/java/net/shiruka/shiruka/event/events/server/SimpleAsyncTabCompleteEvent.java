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

import java.util.Collections;
import java.util.List;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.event.events.server.AsyncTabCompleteEvent;
import net.shiruka.shiruka.event.events.SimpleCancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation of {@link AsyncTabCompleteEvent}.
 */
public final class SimpleAsyncTabCompleteEvent extends SimpleCancellableEvent implements AsyncTabCompleteEvent {

  /**
   * the sender.
   */
  @NotNull
  private final CommandSender sender;

  /**
   * the text.
   */
  @NotNull
  private final String text;

  /**
   * the completions.
   */
  @NotNull
  private List<String> completions;

  /**
   * the handled.
   */
  private boolean handled;

  /**
   * ctor.
   *
   * @param completions the completions.
   * @param sender the sender.
   * @param text the text.
   */
  public SimpleAsyncTabCompleteEvent(@NotNull final List<String> completions, @NotNull final CommandSender sender,
                                     @NotNull final String text) {
    this.completions = Collections.unmodifiableList(completions);
    this.sender = sender;
    this.text = text;
  }

  @NotNull
  @Override
  public List<String> getCompletions() {
    return Collections.unmodifiableList(this.completions);
  }

  @Override
  public void setCompletions(@NotNull final List<String> completions) {
    this.completions = Collections.unmodifiableList(completions);
  }

  @NotNull
  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public boolean isHandled() {
    return !this.completions.isEmpty() || this.handled;
  }

  @Override
  public void setHandled(final boolean handled) {
    this.handled = handled;
  }

  @NotNull
  @Override
  public CommandSender getSender() {
    return this.sender;
  }
}

