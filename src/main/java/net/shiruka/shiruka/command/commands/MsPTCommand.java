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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.text.ChatColor;
import net.shiruka.shiruka.concurrent.ShirukaTick;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents milliseconds per tick command.
 */
public final class MsPTCommand extends CommandHelper {

  /**
   * the format.
   */
  private static final DecimalFormat FORMAT = new DecimalFormat("########0.0");

  /**
   * ctor.
   */
  private MsPTCommand() {
    super("mspt", "Shows how much a tick took to calculate as millisecond.", "shiruka.command.mspt");
  }

  /**
   * registers the command.
   */
  public static void init() {
    new MsPTCommand().register();
  }

  @NotNull
  private static List<String> calculate(final long[] times) {
    var min = (long) Integer.MAX_VALUE;
    var max = 0L;
    var total = 0L;
    for (final var value : times) {
      if (value > 0L && value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
      total += value;
    }
    final var average = (double) total / (double) times.length * 1.0E-6D;
    final var minimum = (double) min * 1.0E-6D;
    final var maximum = (double) max * 1.0E-6D;
    return Arrays.asList(MsPTCommand.getColor(average), MsPTCommand.getColor(minimum), MsPTCommand.getColor(maximum));
  }

  /**
   * get the color in terms of the given average value.
   *
   * @param average the average to get.
   *
   * @return a color in terms of the given average value.
   */
  @NotNull
  private static String getColor(final double average) {
    return ChatColor.ESCAPE + (average >= 50 ? "c" : average >= 40 ? "e" : "a") + MsPTCommand.FORMAT.format(average);
  }

  /**
   * calculates the tick times and convert them to string array.
   *
   * @return a string array that contains tick times with color support.
   */
  @NotNull
  private static Object[] getTickTimes() {
    final var tickTimes = new ArrayList<>();
    final var times = ShirukaTick.getTickTimes();
    tickTimes.addAll(MsPTCommand.calculate(times[0]));
    tickTimes.addAll(MsPTCommand.calculate(times[1]));
    tickTimes.addAll(MsPTCommand.calculate(times[2]));
    return tickTimes.toArray(Object[]::new);
  }

  @NotNull
  @Override
  protected LiteralBuilder build() {
    return super.build()
      .executes(context -> {
        final var tickTimes = MsPTCommand.getTickTimes();
        CommandHelper.sendTranslated(context, "shiruka.command.tick_times.first_line");
        CommandHelper.sendTranslated(context, "shiruka.command.tick_times.second_line", tickTimes);
        return of();
      });
  }
}
