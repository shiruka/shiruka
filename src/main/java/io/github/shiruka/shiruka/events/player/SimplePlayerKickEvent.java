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

package io.github.shiruka.shiruka.events.player;

import io.github.shiruka.api.entity.Player;
import io.github.shiruka.api.events.player.PlayerKickEvent;
import io.github.shiruka.api.text.Text;
import io.github.shiruka.shiruka.events.SimpleCancellableEvent;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerKickEvent}.
 */
public final class SimplePlayerKickEvent extends SimpleCancellableEvent implements PlayerKickEvent {

  /**
   * the player.
   */
  @NotNull
  private final Player player;

  /**
   * the reason.
   */
  @NotNull
  private final Reason reason;

  /**
   * the kick message.
   */
  @Nullable
  private Text kickMessage;

  public SimplePlayerKickEvent(@NotNull final Player player, @NotNull final Reason reason,
                               @Nullable final Text kickMessage) {
    this.player = player;
    this.reason = reason;
    this.kickMessage = kickMessage;
  }

  @NotNull
  @Override
  public Optional<Text> kickMessage() {
    return Optional.ofNullable(this.kickMessage);
  }

  @Override
  public void kickMessage(@Nullable final Text message) {
    this.kickMessage = message;
  }
}
