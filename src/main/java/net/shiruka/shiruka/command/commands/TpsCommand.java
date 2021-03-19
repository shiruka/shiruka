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
import net.shiruka.shiruka.ShirukaServer;
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
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * ctor.
   *
   * @param server the server.
   */
  private TpsCommand(@NotNull final ShirukaServer server) {
    super("tps", "Gets the current ticks per second for the server.", "shiruka.command.tps", server);
    this.server = server;
  }

  /**
   * registers the command.
   *
   * @param server the server to init.
   */
  public static void init(@NotNull final ShirukaServer server) {
    new TpsCommand(server).register();
  }

  /**
   * registers the command.
   */
  @NotNull
  @Override
  protected LiteralBuilder build() {
    return super.build()
      .executes(context -> {
        final var tpsAvg = this.getTps();
        this.sendTranslated(context, TpsCommand.MESSAGE, tpsAvg[0], tpsAvg[1], tpsAvg[2]);
        return of();
      });
  }

  /**
   * obtains the tps numbers as {@link String} array which has 3 elements.
   *
   * @return tps numbers as string array.
   */
  @NotNull
  private String[] getTps() {
    return Arrays.stream(this.server.getTick().getTps())
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
}
