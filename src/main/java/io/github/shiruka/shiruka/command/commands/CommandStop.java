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

package io.github.shiruka.shiruka.command.commands;

import static io.github.shiruka.api.command.CommandResult.succeed;
import static io.github.shiruka.api.command.Commands.literal;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.command.SimpleCommandManager;

/**
 * a class that represents stop command.
 */
public final class CommandStop {

  /**
   * ctor.
   */
  private CommandStop() {
  }

  /**
   * registers the stop command.
   */
  public static void register() {
    SimpleCommandManager.registerInternal(literal("stop")
      .requires(commandSender -> commandSender.hasPermission("shiruka.command.stop"))
      .executes(context -> {
        context.getSender().sendMessage(TranslatedText.get("shiruka.commmand.add_confirm"));
        return succeed();
      })
      .then(literal("confirm")
        .executes(context -> {
          Shiruka.getServer().stopServer();
          return succeed();
        })));
  }
}
