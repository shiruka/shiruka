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

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.command.Commands;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.command.context.CommandContext;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class that helps developers to create commands easily.
 */
@RequiredArgsConstructor
abstract class CommandHelper {

  /**
   * the test permission.
   */
  private static final String TEST_PERMISSION = "shiruka.command.permission";

  /**
   * the aliases.
   */
  @NotNull
  private final String[] aliases;

  /**
   * the command.
   */
  @NotNull
  private final String command;

  /**
   * the description.
   */
  @NotNull
  private final String description;

  /**
   * the permission.
   */
  @NotNull
  private final String permission;

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the permission message.
   * <p>
   * Placeholders:
   * {0} = permissions which the command sender have not.
   */
  @Nullable
  protected Text permissionMessage;

  /**
   * ctor.
   *
   * @param command the command.
   * @param description the description.
   * @param permission the permission.
   * @param server the server.
   */
  CommandHelper(@NotNull final String command, @NotNull final String description, @NotNull final String permission,
                @NotNull final ShirukaServer server) {
    this(new String[0], command, description, permission, server);
  }

  /**
   * registers the command.
   */
  protected final void register() {
    this.server.getCommandManager().registerInternal(this.build());
  }

  /**
   * sends a {@link TranslatedText} to the given {@code context}'s command sender.
   *
   * @param context the context to send.
   * @param key the key to send.
   * @param params the params to send.
   */
  protected final void sendTranslated(@NotNull final CommandContext context, @NotNull final String key,
                                      @NotNull final Object... params) {
    this.sendTranslated(context.getSender(), key, params);
  }

  /**
   * sends a {@link TranslatedText} to the given {@code context}'s command sender.
   *
   * @param context the context to send.
   * @param text the text to send.
   */
  protected final void sendTranslated(@NotNull final CommandContext context, @NotNull final TranslatedText text) {
    context.getSender().sendMessage(text);
  }

  /**
   * sends a {@link TranslatedText} to the given {@code sender}.
   *
   * @param sender the sender to send.
   * @param key the key to send.
   * @param params the params to send.
   */
  protected final void sendTranslated(@NotNull final CommandSender sender, @NotNull final String key,
                                      @NotNull final Object... params) {
    sender.sendMessage(TranslatedText.get(key, params));
  }

  /**
   * builds the command.
   *
   * @return the built command builder.
   */
  @NotNull
  protected LiteralBuilder build() {
    final var builder = Commands.literal(this.command)
      .describe(this.description)
      .permission((sender, permissions) -> {
        final var joined = Joiner.on(", ").join(permissions);
        if (this.permissionMessage == null) {
          this.sendTranslated(sender, CommandHelper.TEST_PERMISSION, joined);
        } else {
          sender.sendMessage(this.permissionMessage, joined);
        }
      }, this.permission);
    if (this.aliases.length != 0) {
      builder.aliases(this.aliases);
    }
    return builder;
  }
}
