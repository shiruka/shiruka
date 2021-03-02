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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.shiruka.api.base.ChainData;
import net.shiruka.api.entity.Player;
import net.shiruka.api.event.events.player.PlayerAsyncLoginEvent;
import net.shiruka.api.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation for {@link PlayerAsyncLoginEvent}.
 */
@RequiredArgsConstructor
public final class SimplePlayerAsyncLoginEvent implements PlayerAsyncLoginEvent {

  /**
   * the actions that will run after player initialization.
   */
  private final List<Consumer<Player>> actions = new ObjectArrayList<>();

  /**
   * the chain data.
   */
  @NotNull
  @Getter
  private final ChainData chainData;

  /**
   * the kick message.
   */
  @Nullable
  @Setter
  private Text kickMessage;

  /**
   * the login result.
   */
  @NotNull
  @Getter
  @Setter
  private LoginResult loginResult = LoginResult.ALLOWED;

  @Override
  public void addAction(@NotNull final Consumer<Player> action) {
    this.actions.add(action);
  }

  @NotNull
  @Override
  public List<Consumer<Player>> getActions() {
    return Collections.unmodifiableList(this.actions);
  }

  @Override
  public void removeAction(@NotNull final Consumer<Player> action) {
    this.actions.remove(action);
  }

  @NotNull
  @Override
  public Optional<Text> getKickMessage() {
    return Optional.ofNullable(this.kickMessage);
  }
}
