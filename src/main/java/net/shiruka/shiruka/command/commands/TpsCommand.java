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
import java.util.Arrays;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.text.ChatColor;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents tps command.
 */
public final class TpsCommand extends CommandHelper {

  /**
   * the message key from the Shiru ka's language properties file.
   */
  private static final String MESSAGE = "shiruka.command.tps_command.show_tps";

  /**
   * ctor.
   */
  private TpsCommand() {
    super("tps", "Gets the current ticks per second for the server.",
      "shiruka.command.tps");
  }

  /**
   * registers the stop command.
   */
  public static void init() {
    new TpsCommand().register();
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
        if (value > 18.0d) {
          color = ChatColor.GREEN;
        } else if (value > 16.0d) {
          color = ChatColor.YELLOW;
        } else {
          color = ChatColor.RED;
        }
        final var putStar = value > 21.0 ? "*" : "";
        return color + putStar + Math.min(Math.round(value * 100.0) / 100.0, 20.0) + ChatColor.RESET;
      })
      .toArray(String[]::new);
  }

  /**
   * registers the command.
   */
  @NotNull
  @Override
  protected LiteralBuilder build() {
    return super.build()
      .executes(context -> {
        final var tpsAvg = TpsCommand.getTps();
        CommandHelper.sendTranslated(context, TpsCommand.MESSAGE, tpsAvg[0], tpsAvg[1], tpsAvg[2]);
        return of();
      });
  }
}
