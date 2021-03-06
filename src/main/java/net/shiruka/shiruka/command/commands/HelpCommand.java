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

package net.shiruka.shiruka.command.commands;

import static net.shiruka.api.command.CommandResult.of;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents help command.
 *
 * @todo #1:30m Implement help command.
 */
public final class HelpCommand extends CommandHelper {

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  private HelpCommand(@NotNull final ShirukaServer server) {
    super(new String[]{"?"}, "help", "Shows the help menu", "shiruka.command.help", server);
    this.server = server;
  }

  /**
   * registers the command.
   *
   * @param server the server.
   */
  public static void init(@NotNull final ShirukaServer server) {
    new HelpCommand(server).register();
  }

  @NotNull
  @Override
  protected LiteralBuilder build() {
    return super.build()
      .executes(context -> of());
  }
}
