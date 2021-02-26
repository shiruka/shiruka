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
import net.shiruka.api.command.Commands;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.command.context.CommandContext;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.command.SimpleCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class that helps developers to create commands easily.
 */
abstract class CommandHelper {

  /**
   * the test permission.
   */
  private static final String TEST_PERMISSION = "shiruka.command.permission";

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
   * the permission message.
   * <p>
   * Placeholders:
   * {0} = permissions itself.
   */
  @Nullable
  protected Text permissionMessage;

  /**
   * ctor.
   *
   * @param command the command.
   * @param description the description.
   * @param permission the permission.
   */
  CommandHelper(@NotNull final String command, @NotNull final String description,
                @NotNull final String permission) {
    this.command = command;
    this.description = description;
    this.permission = permission;
  }

  /**
   * sends a {@link TranslatedText} to the given {@code sender}.
   *
   * @param sender the sender to send.
   * @param key the key to send.
   * @param params the params to send.
   */
  protected static void sendTranslated(@NotNull final CommandSender sender, @NotNull final String key,
                                       @NotNull final Object... params) {
    CommandHelper.sendTranslated(sender, TranslatedText.get(key, params));
  }

  /**
   * sends a {@link TranslatedText} to the given {@code sender}.
   *
   * @param sender the sender to send.
   * @param text the text to send.
   */
  protected static void sendTranslated(@NotNull final CommandSender sender, @NotNull final TranslatedText text) {
    sender.sendMessage(text);
  }

  /**
   * sends a {@link TranslatedText} to the given {@code context}'s command sender.
   *
   * @param context the context to send.
   * @param key the key to send.
   * @param params the params to send.
   */
  protected static void sendTranslated(@NotNull final CommandContext context, @NotNull final String key,
                                       @NotNull final Object... params) {
    CommandHelper.sendTranslated(context.getSender(), key, params);
  }

  /**
   * sends a {@link TranslatedText} to the given {@code context}'s command sender.
   *
   * @param context the context to send.
   * @param text the text to send.
   */
  protected static void sendTranslated(@NotNull final CommandContext context, @NotNull final TranslatedText text) {
    CommandHelper.sendTranslated(context.getSender(), text);
  }

  /**
   * registers the command.
   */
  protected final void register() {
    SimpleCommandManager.registerInternal(this.build());
  }

  /**
   * builds the command.
   *
   * @return the built command builder.
   */
  @NotNull
  protected LiteralBuilder build() {
    return Commands.literal(this.command)
      .describe(this.description)
      .permission((sender, permissions) -> {
        final var joined = Joiner.on(", ").join(permissions);
        if (this.permissionMessage == null) {
          CommandHelper.sendTranslated(sender, CommandHelper.TEST_PERMISSION, joined);
        } else {
          sender.sendMessage(this.permissionMessage, joined);
        }
      }, this.permission);
  }
}
