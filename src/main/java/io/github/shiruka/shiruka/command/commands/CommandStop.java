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
import io.github.shiruka.shiruka.command.SimpleCommandManager;

/**
 * a class that represents stop command.
 */
public final class CommandStop extends CommandHelper {

  /**
   * the command.
   */
  private static final String COMMAND = "stop";

  /**
   * the confirm sub command.
   */
  private static final String CONFIRM_SUB_COMMAND = "confirm";

  /**
   * the description.
   */
  private static final String DESCRIPTION = "Stops the server.";

  /**
   * the instance.
   */
  private static final CommandStop INSTANCE = new CommandStop();

  /**
   * the message key from the Shiru ka's language properties file.
   */
  private static final String MESSAGE = "command_stop.register.add_confirm";

  /**
   * the permission.
   */
  private static final String PERMISSION = "shiruka.command.stop";

  /**
   * ctor.
   */
  private CommandStop() {
  }

  /**
   * registers the stop command.
   */
  public static void init() {
    CommandStop.INSTANCE.register();
  }

  /**
   * registers the stop command.
   */
  public void register() {
    SimpleCommandManager.registerInternal(literal(CommandStop.COMMAND)
      .describe(CommandStop.DESCRIPTION)
      .requires(commandSender -> this.testPermission(commandSender, CommandStop.PERMISSION))
      .executes(context -> {
        CommandHelper.sendTranslated(context, CommandStop.MESSAGE);
        return succeed();
      })
      .then(literal(CommandStop.CONFIRM_SUB_COMMAND)
        .executes(context -> {
          Shiruka.getServer().stopServer();
          return succeed();
        })));
  }
}
