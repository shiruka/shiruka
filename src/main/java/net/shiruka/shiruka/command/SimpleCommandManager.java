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

package net.shiruka.shiruka.command;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.command.CommandDispatcher;
import net.shiruka.api.command.CommandException;
import net.shiruka.api.command.CommandManager;
import net.shiruka.api.command.CommandNode;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.command.exceptions.CommandSyntaxException;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.command.commands.HelpCommand;
import net.shiruka.shiruka.command.commands.MsPTCommand;
import net.shiruka.shiruka.command.commands.StopCommand;
import net.shiruka.shiruka.command.commands.TpsCommand;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link CommandManager}.
 */
@Log4j2
@RequiredArgsConstructor
public final class SimpleCommandManager implements CommandManager {

  /**
   * the not found.
   */
  private static final String NOT_FOUND = "shiruka.command.not_found";

  /**
   * the dispatcher.
   */
  @NotNull
  @Getter
  private final CommandDispatcher dispatcher = new CommandDispatcher();

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  @Override
  public void execute(@NotNull final String command, @NotNull final CommandSender sender) {
    try {
      this.dispatcher.execute(command, sender);
    } catch (final CommandSyntaxException e) {
      if (e.getType() == CommandException.DISPATCHER_UNKNOWN_COMMAND) {
        sender.sendMessage(TranslatedText.get(SimpleCommandManager.NOT_FOUND, command));
        return;
      }
      // @todo #1:5m Add language support for An exception caught when running a command.
      SimpleCommandManager.log.error("An exception caught when running a command: ", e);
    }
  }

  @Override
  public void register(@NotNull final Plugin plugin, @NotNull final CommandNode... commands) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleCommandManager#register.");
  }

  @NotNull
  @Override
  public Map<String, CommandNode> registered(@NotNull final Plugin plugin) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleCommandManager#registered.");
  }

  @Override
  public void unregister(@NotNull final String... commands) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleCommandManager#unregister.");
  }

  /**
   * registers the default commands.
   */
  public void register() {
    StopCommand.init(this.server);
    TpsCommand.init(this.server);
    HelpCommand.init(this.server);
    MsPTCommand.init(this.server);
  }

  /**
   * registers internal(Shiru ka) commands.
   *
   * @param commands the commands to register.
   */
  public void registerInternal(@NotNull final CommandNode... commands) {
    this.dispatcher.register(commands);
  }

  /**
   * registers the given commands.
   *
   * @param builders the builders to register.
   */
  public void registerInternal(@NotNull final LiteralBuilder... builders) {
    this.dispatcher.register(builders);
  }
}
