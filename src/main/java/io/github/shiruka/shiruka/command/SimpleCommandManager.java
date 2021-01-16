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

package io.github.shiruka.shiruka.command;

import io.github.shiruka.api.command.CommandManager;
import io.github.shiruka.api.command.CommandNode;
import io.github.shiruka.api.command.CommandSender;
import io.github.shiruka.api.plugin.Plugin;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link CommandManager}.
 */
public final class SimpleCommandManager implements CommandManager {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("SimpleCommandManager");

  @Override
  public void execute(@NotNull final String command, @NotNull final CommandSender sender) {
    SimpleCommandManager.LOGGER.info("{} -> {}", sender.getName().asString(), command);
  }

  @Override
  public void register(@NotNull final Plugin plugin, @NotNull final CommandNode... commands) {
  }

  @NotNull
  @Override
  public Map<String, CommandNode> registered(@NotNull final Plugin plugin) {
    return Collections.emptyMap();
  }

  @Override
  public void unregister(@NotNull final String... commands) {
  }
}
