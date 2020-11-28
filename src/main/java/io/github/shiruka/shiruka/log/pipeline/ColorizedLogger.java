/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
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
package io.github.shiruka.shiruka.log.pipeline;

import io.github.shiruka.api.chat.ChatColor;
import io.github.shiruka.shiruka.log.ConsoleColors;
import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.PipelineLogger;
import org.jetbrains.annotations.NotNull;

/**
 * colorized logger is a pipeline logger which replaces
 * color codes with the ANSI equivalents before passing to
 * the next logger.
 */
public final class ColorizedLogger extends PipelineLoggerBase {

  /**
   * ctor
   *
   * @param next the next logger in the pipeline
   */
  public ColorizedLogger(@NotNull final PipelineLogger next) {
    super(next);
  }

  /**
   * shorthand method for handling the input message to log.
   */
  @NotNull
  private static LogMessage handle(@NotNull final String color, @NotNull final LogMessage msg) {
    msg.setMessage(color + msg.getMessage() + ConsoleColors.RESET);
    return msg;
  }

  @NotNull
  @Override
  public LogMessage handle(@NotNull final LogMessage msg) {
    final var sq = msg.getMessage().toCharArray();
    final var builder = new StringBuilder();
    for (int index = 0; index < sq.length; index++) {
      final var c = sq[index];
      if (c != ChatColor.ESCAPE) {
        builder.append(c);
        continue;
      }
      final var codeIdx = ++index;
      if (codeIdx >= sq.length) {
        break;
      }
      switch (ChatColor.of(sq[codeIdx])) {
        case BLACK:
          builder.append(ConsoleColors.BLACK);
          break;
        case DARK_BLUE:
        case BLUE:
          builder.append(ConsoleColors.BLUE);
          break;
        case DARK_GREEN:
        case GREEN:
          builder.append(ConsoleColors.GREEN);
          break;
        case DARK_AQUA:
        case AQUA:
          builder.append(ConsoleColors.CYAN);
          break;
        case DARK_RED:
        case RED:
          builder.append(ConsoleColors.RED);
          break;
        case DARK_PURPLE:
        case LIGHT_PURPLE:
          builder.append(ConsoleColors.PURPLE);
          break;
        case GOLD:
        case YELLOW:
          builder.append(ConsoleColors.YELLOW);
          break;
        case GRAY:
        case WHITE:
        case DARK_GRAY:
          builder.append(ConsoleColors.WHITE);
          break;
        case OBFUSCATED:
          break;
        case BOLD:
          builder.append(ConsoleColors.BOLD);
          break;
        case STRIKETHROUGH:
          builder.append(ConsoleColors.STRIKETHROUGH);
          break;
        case UNDERLINE:
          builder.append(ConsoleColors.UNDERLINE);
          break;
        case ITALIC:
          builder.append(ConsoleColors.ITALICS);
          break;
        case RESET:
          builder.append(ConsoleColors.RESET);
          break;
        default:
          builder.append(ChatColor.ESCAPE).append(sq[codeIdx]);
      }
    }
    msg.setMessage(builder + ConsoleColors.RESET);
    return msg;
  }

  @Override
  public void log(@NotNull final LogMessage msg) {
    this.next().log(this.handle(msg));
  }

  @Override
  public void success(@NotNull final LogMessage msg) {
    this.next().success(ColorizedLogger.handle(ConsoleColors.GREEN, msg));
  }

  @Override
  public void warn(@NotNull final LogMessage msg) {
    this.next().warn(ColorizedLogger.handle(ConsoleColors.YELLOW, msg));
  }

  @Override
  public void error(@NotNull final LogMessage msg) {
    this.next().error(ColorizedLogger.handle(ConsoleColors.RED, msg));
  }

  @Override
  public void debug(@NotNull final LogMessage msg) {
    this.next().debug(ColorizedLogger.handle(ConsoleColors.WHITE, msg));
  }
}
