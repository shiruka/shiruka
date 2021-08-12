package io.github.shiruka.shiruka.log;

import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.AbstractPatternConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * replaces Minecraft formatting codes in the result of a pattern with appropriate ANSI escape codes.
 */
@Plugin(name = "shirukaFormatting", category = PatternConverter.CATEGORY)
@ConverterKeys({"shirukaFormatting"})
@PerformanceSensitive("allocation")
public final class ShirukaFormatter extends LogEventPatternConverter {

  /**
   * the ansi reset code.
   */
  private static final String ANSI_RESET = "\u001B[39;0m";

  /**
   * the ansi codes.
   */
  private static final String[] ANSI_CODES = new String[]{
    "\u001B[0;30;22m",
    "\u001B[0;34;22m",
    "\u001B[0;32;22m",
    "\u001B[0;36;22m",
    "\u001B[0;31;22m",
    "\u001B[0;35;22m",
    "\u001B[0;33;22m",
    "\u001B[0;37;22m",
    "\u001B[0;30;1m",
    "\u001B[0;34;1m",
    "\u001B[0;32;1m",
    "\u001B[0;36;1m",
    "\u001B[0;31;1m",
    "\u001B[0;35;1m",
    "\u001B[0;33;1m",
    "\u001B[0;37;1m",
    "\u001B[5m",
    "\u001B[21m",
    "\u001B[9m",
    "\u001B[4m",
    "\u001B[3m",
    ShirukaFormatter.ANSI_RESET,
  };

  /**
   * the color character.
   */
  private static final String COLOR_CHAR = "§";

  /**
   * the lookup.
   */
  private static final String LOOKUP = "0123456789abcdefklmnor";

  /**
   * the ansi.
   */
  private final boolean ansi;

  /**
   * the formatters.
   */
  @NotNull
  private final List<PatternFormatter> formatters;

  /**
   * Construct the converter.
   *
   * @param formatters The pattern formatters to generate the text to manipulate
   * @param strip If true, the converter will strip all formatting codes
   */
  private ShirukaFormatter(@NotNull final List<PatternFormatter> formatters, final boolean strip) {
    super("shirukaFormatting", null);
    this.formatters = Collections.unmodifiableList(formatters);
    this.ansi = !strip;
  }

  /**
   * creates a new instance of {@code this}.
   *
   * @param config the config to create.
   * @param options the options to create.
   *
   * @return a new instance of {@code this}.
   */
  @Nullable
  public static ShirukaFormatter newInstance(final Configuration config, final String[] options) {
    if (options.length < 1 || options.length > 2) {
      AbstractPatternConverter.LOGGER.error("Incorrect number of options on shirukaFormatting.");
      AbstractPatternConverter.LOGGER.error("Expected at least 1, max 2 received {}", options.length);
      return null;
    }
    if (options[0] == null) {
      AbstractPatternConverter.LOGGER.error("No pattern supplied on shirukaFormatting");
      return null;
    }
    return new ShirukaFormatter(
      PatternLayout.createPatternParser(config).parse(options[0]),
      options.length > 1 && "strip".equals(options[1]));
  }

  /**
   * formats the given message.
   *
   * @param message the message to format.
   * @param result the result to format.
   * @param start the start to format.
   * @param ansi the ansi to format.
   */
  private static void format(@NotNull final String message, @NotNull final StringBuilder result, final int start,
                             final boolean ansi) {
    var next = message.indexOf(ShirukaFormatter.COLOR_CHAR);
    final var last = message.length() - 1;
    if (next == -1 || next == last) {
      return;
    }
    result.setLength(start + next);
    var pos = next;
    do {
      if (pos != next) {
        result.append(message, pos, next);
      }
      final var format = ShirukaFormatter.LOOKUP.indexOf(message.charAt(next + 1));
      if (format != -1) {
        if (ansi) {
          result.append(ShirukaFormatter.ANSI_CODES[format]);
        }
        pos = next += 2;
      } else {
        next++;
      }
      next = message.indexOf(ShirukaFormatter.COLOR_CHAR, next);
    } while (next != -1 && next < last);
    result.append(message, pos, message.length());
    if (ansi) {
      result.append(ShirukaFormatter.ANSI_RESET);
    }
  }

  @Override
  public void format(final LogEvent event, final StringBuilder toAppendTo) {
    final var start = toAppendTo.length();
    this.formatters.forEach(formatter -> formatter.format(event, toAppendTo));
    if (toAppendTo.length() != start) {
      ShirukaFormatter.format(toAppendTo.substring(start), toAppendTo, start, this.ansi);
    }
  }
}
