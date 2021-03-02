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

package net.shiruka.shiruka.event.events.player;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.shiruka.api.entity.Player;
import net.shiruka.api.event.events.LoginResultEvent;
import net.shiruka.api.event.events.player.PlayerKickEvent;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.event.events.SimpleCancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerKickEvent}.
 */
@AllArgsConstructor
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
  private final LoginResultEvent.LoginResult reason;

  /**
   * the kick message.
   */
  @Nullable
  @Setter
  private Text kickMessage;

  @NotNull
  @Override
  public Optional<Text> getKickMessage() {
    return Optional.ofNullable(this.kickMessage);
  }
}
