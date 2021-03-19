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

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.event.events.player.PlayerConnectionCloseEvent;
import net.shiruka.api.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerConnectionCloseEvent}.
 */
@RequiredArgsConstructor
public final class SimplePlayerConnectionCloseEvent implements PlayerConnectionCloseEvent {

  /**
   * the address.
   */
  @NotNull
  @Getter
  private final InetSocketAddress address;

  /**
   * the name.
   */
  @NotNull
  @Getter
  private final Text name;

  /**
   * the unique id.
   */
  @NotNull
  @Getter
  private final UUID uniqueId;

  /**
   * the xbox unique id.
   */
  @Nullable
  private final String xboxUniqueId;

  @NotNull
  @Override
  public Optional<String> getXboxUniqueId() {
    return Optional.ofNullable(this.xboxUniqueId);
  }
}
