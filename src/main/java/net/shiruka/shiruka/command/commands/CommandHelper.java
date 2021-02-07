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

import java.util.stream.Stream;
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
    sender.sendMessage(TranslatedText.get(key, params));
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
   * tests the given {@code target}'s permission for the given {@code permissions}.
   *
   * @param target the target to test.
   * @param permissions the permissions to test.
   *
   * @return {@code true} if the target has the given {@code permissions}.
   */
  protected static boolean testPermissionSilent(@NotNull final CommandSender target,
                                                @NotNull final String... permissions) {
    return Stream.of(permissions).allMatch(target::hasPermission);
  }

  /**
   * registers the command.
   */
  protected final void register() {
    SimpleCommandManager.registerInternal(this.build());
  }

  /**
   * tests the given {@code target}'s permission for the given {@code permissions} and, if the target has not the given
   * {@code permissions} sends {@link #permissionMessage} if its not null, otherwise sends
   * {@code shiruka.command.commands.helper.test_permission} properties on the language files.
   *
   * @param target the target to test.
   * @param permissions the permissions to test.
   *
   * @return {@code true} if the target has the given {@code permissions}.
   *
   * @see #testPermissionSilent(CommandSender, String...)
   */
  protected final boolean testPermission(@NotNull final CommandSender target, @NotNull final String... permissions) {
    if (CommandHelper.testPermissionSilent(target, permissions)) {
      return true;
    }
    if (this.permissionMessage == null) {
      Stream.of(permissions).forEach(permission ->
        target.sendMessage(TranslatedText.get("shiruka.command.commands.helper.test_permission", permission)));
    } else {
      Stream.of(permissions).forEach(permission ->
        target.sendMessage(this.permissionMessage, permission));
    }
    return false;
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
      .requires(commandSender -> this.testPermission(commandSender, this.permission));
  }
}
