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

package io.github.shiruka.shiruka.log;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * a plugin class for log4j.
 */
@Plugin(
  name = "TerminalConsole",
  category = Core.CATEGORY_NAME,
  elementType = Appender.ELEMENT_TYPE,
  printObject = true
)
public final class AaTerminalConsole extends AbstractAppender {

  /**
   * the system out.
   */
  private static final PrintStream STDOUT = System.out;

  /**
   * the initialized.
   */
  private static boolean initialized;

  /**
   * the reader.
   */
  @Nullable
  private static LineReader reader;

  /**
   * the terminal.
   */
  @Nullable
  private static Terminal terminal;

  /**
   * ctor.
   *
   * @param name the name.
   * @param filter the filter.
   * @param layout the layout.
   * @param ignoreExceptions the ignore exceptions.
   */
  private AaTerminalConsole(@NotNull final String name, @Nullable final Filter filter,
                            @NotNull final Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    AaTerminalConsole.initializeTerminal();
  }

  /**
   * closes the jLine terminal when needed.
   */
  public static void close() {
    if (AaTerminalConsole.terminal != null) {
      AaTerminalConsole.terminal.reader().shutdown();
    }
  }

  /**
   * creates a factory for log4j.
   *
   * @param name the name to create.
   * @param filter the filter to create.
   * @param layout the layout to create.
   * @param ignoreExceptions the ignore exceptions to create.
   *
   * @return a new factory instance.
   */
  @PluginFactory
  public static AaTerminalConsole createAppender(
    @Required(message = "No name provided for TerminalConsoleAppender") @PluginAttribute("name") final String name,
    @PluginElement("Filter") final Filter filter,
    @PluginElement("Layout") final Layout<? extends Serializable> layout,
    @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions) {
    var finalLayout = layout;
    if (layout == null) {
      finalLayout = PatternLayout.createDefaultLayout();
    }
    return new AaTerminalConsole(name, filter, finalLayout, ignoreExceptions);
  }

  /**
   * obtains the terminal.
   *
   * @return terminal.
   */
  @Nullable
  public static Terminal getTerminal() {
    return AaTerminalConsole.terminal;
  }

  /**
   * sets the reader.
   *
   * @param reader the reader to set.
   */
  public static void setReader(@NotNull final LineReader reader) {
    Preconditions.checkArgument(reader.getTerminal() == AaTerminalConsole.terminal,
      "Reader was not created with TerminalConsoleAppender.getTerminal()");
    AaTerminalConsole.reader = reader;
  }

  /**
   * initiates the terminal.
   */
  private static void initializeTerminal() {
    if (AaTerminalConsole.initialized) {
      return;
    }
    AaTerminalConsole.initialized = true;
    try {
      AaTerminalConsole.terminal = TerminalBuilder.builder()
        .dumb(false)
        .build();
    } catch (final IllegalStateException e) {
      AbstractLifeCycle.LOGGER.warn("Not supported terminal");
    } catch (final IOException e) {
      AbstractLifeCycle.LOGGER.error("Failed to init, falling back to STDOUT");
      AbstractLifeCycle.LOGGER.debug(e);
    }
  }

  @Override
  public void append(final LogEvent event) {
    if (AaTerminalConsole.terminal == null) {
      AaTerminalConsole.STDOUT.print(this.getLayout().toSerializable(event));
      return;
    }
    if (AaTerminalConsole.reader == null) {
      AaTerminalConsole.terminal.writer().print(this.getLayout().toSerializable(event));
      AaTerminalConsole.terminal.writer().flush();
      return;
    }
    try {
      AaTerminalConsole.reader.callWidget(LineReader.CLEAR);
      AaTerminalConsole.terminal.writer().print(this.getLayout().toSerializable(event));
      AaTerminalConsole.reader.callWidget(LineReader.REDRAW_LINE);
      AaTerminalConsole.reader.callWidget(LineReader.REDISPLAY);
    } catch (final Exception e) {
      AaTerminalConsole.terminal.writer().print(this.getLayout().toSerializable(event));
    }
    AaTerminalConsole.terminal.writer().flush();
  }
}
