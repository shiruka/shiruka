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
import static net.shiruka.api.command.Commands.*;
import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import net.shiruka.api.command.Commands;
import net.shiruka.api.command.context.CommandContext;
import net.shiruka.shiruka.command.SimpleCommandManager;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents timings command.
 */
public final class CommandTimings extends CommandHelper {

  public static final String PERMISSION = "shiruka.command.timings";

  /**
   * the command.
   */
  private static final String COMMAND = "timings";

  /**
   * the instance.
   */
  private static final CommandTimings INSTANCE = new CommandTimings();

  private long lastResetAttempt = 0;

  /**
   * ctor.
   */
  private CommandTimings() {
  }

  /**
   * registers the stop command.
   */
  public static void init() {
    CommandTimings.INSTANCE.register();
  }

  /**
   * checks if the timings on.
   *
   * @param context the context to check.
   *
   * @return {@code true} if the timings on.
   */
  private static boolean checkTimingsOn(@NotNull final CommandContext context) {
    if (!Timings.isTimingsEnabled()) {
      // @todo #1:15m Add translation for `shiruka.command.commands.timings.check_timings_on.enable_timings`.
      context.getSender().sendMessage("Please enable timings by typing /timings on");
      return false;
    }
    return false;
  }

  /**
   * registers the stop command.
   */
  public void register() {
    SimpleCommandManager.registerInternal(literal(CommandTimings.COMMAND)
      .requires(sender -> this.testPermission(sender, CommandTimings.PERMISSION))
      .then(Commands.defaultArg("usage", termArg("help", "usage"), "help")
        .executes(context -> {
          // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.help`.
          context.getSender().sendMessage("§e/timings <reset|report|on|off|verbon|verboff>");
          return of();
        }))
      .then(literal("on")
        .executes(context -> {
          Timings.setTimingsEnabled(true);
          // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.timings_enabled`.
          context.getSender().sendMessage("§aEnabled Timings & Reset");
          return of();
        }))
      .then(literal("off")
        .executes(context -> {
          Timings.setTimingsEnabled(false);
          // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.timings_disabled`.
          context.getSender().sendMessage("§cDisabled Timings");
          return of();
        }))
      .then(literal("verbon")
        .executes(context -> {
          if (!CommandTimings.checkTimingsOn(context)) {
            return of();
          }
          Timings.setVerboseTimingsEnabled(true);
          // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.verbose_enabled`.
          context.getSender().sendMessage("§aEnabled Verbose Timings");
          return of();
        }))
      .then(literal("verboff")
        .executes(context -> {
          if (!CommandTimings.checkTimingsOn(context)) {
            return of();
          }
          final var now = System.currentTimeMillis();
          Timings.setVerboseTimingsEnabled(false);
          // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.verbose_disabled`.
          context.getSender().sendMessage("§cDisabled Verbose Timings");
          return of();
        }))
      .then(literal("reset")
        .executes(context -> {
          final var sender = context.getSender();
          final var now = System.currentTimeMillis();
          if (now - this.lastResetAttempt < 30000) {
            TimingsManager.reset();
            // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.timings_reset`.
            sender.sendMessage("§cTimings reset. Please wait 5-10 minutes before using /timings report.");
          } else {
            this.lastResetAttempt = now;
            // @todo #1:15m Add translation for `shiruka.command.commands.timings.register.reset_fail`.
            sender.sendMessage("§cWARNING: Timings v2 should not be reset. If you are encountering lag, please wait 3 minutes and then issue a report. The best timings will include 10+ minutes, with data before and after your lag period. If you really want to reset, run this command again within 30 seconds.");
          }
          return of();
        }))
      .then(arg("report", termArg("paste", "report", "get", "merged", "separate"))
        .executes(context -> {
          Timings.generateReport(context.getSender());
          return of();
        }))
    );
  }
}
