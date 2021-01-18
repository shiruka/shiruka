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
import io.github.shiruka.api.text.ChatColor;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.command.SimpleCommandManager;
import io.github.shiruka.shiruka.concurrent.ShirukaTick;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents tps command.
 */
public final class CommandTps extends CommandHelper {

  /**
   * the instance.
   */
  private static final CommandTps INSTANCE = new CommandTps();

  /**
   * ctor.
   */
  private CommandTps() {
  }

  /**
   * registers the stop command.
   */
  public static void init() {
    CommandTps.INSTANCE.register();
  }

  /**
   * obtains the tps numbers as {@link String} array which has 3 elements.
   *
   * @return tps numbers as string array.
   */
  @NotNull
  private static String[] getTps() {
    return Arrays.stream(ShirukaTick.getTps())
      .mapToObj(value -> {
        final ChatColor color;
        if (value > 18.0) {
          color = ChatColor.GREEN;
        } else if (value > 16.0) {
          color = ChatColor.YELLOW;
        } else {
          color = ChatColor.RED;
        }
        final var putStart = value > 21.0 ? "*" : "";
        return color + putStart + Math.min(Math.round(value * 100.0) / 100.0, 20.0) + ChatColor.RESET;
      })
      .toArray(String[]::new);
  }

  /**
   * registers the command.
   */
  private void register() {
    SimpleCommandManager.registerInternal(literal("tps")
      .requires(commandSender -> this.testPermission(commandSender, "shiruka.command.tps"))
      .executes(context -> {
        final var tpsAvg = CommandTps.getTps();
        CommandHelper.sendTranslated(context, "shiruka.command.commands.command_tps.register.show_tps",
          tpsAvg[0], tpsAvg[1], tpsAvg[2]);
        return succeed();
      }));
  }
}
