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

package io.github.shiruka.shiruka.console;

import io.github.shiruka.api.Server;
import io.github.shiruka.api.console.ConsoleCommandSender;
import io.github.shiruka.api.permission.Permission;
import io.github.shiruka.api.permission.PermissionAttachment;
import io.github.shiruka.api.permission.PermissionAttachmentInfo;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.text.Text;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * a simple console implementation for {@link ConsoleCommandSender}.
 */
public final class SimpleConsoleCommandSender implements ConsoleCommandSender {

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  /**
   * ctor.
   *
   * @param console the console.
   */
  public SimpleConsoleCommandSender(@NotNull final ShirukaConsole console) {
    this.console = console;
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                            final boolean value) {
    return null;
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin) {
    return null;
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                                      final boolean value, final long ticks) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, final long ticks) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    return null;
  }

  @Override
  public boolean hasPermission(@NotNull final String name) {
    return false;
  }

  @Override
  public boolean hasPermission(@NotNull final Permission perm) {
    return false;
  }

  @Override
  public boolean isPermissionSet(@NotNull final String name) {
    return false;
  }

  @Override
  public boolean isPermissionSet(@NotNull final Permission perm) {
    return false;
  }

  @Override
  public void recalculatePermissions() {
  }

  @Override
  public void removeAttachment(@NotNull final PermissionAttachment attachment) {
  }

  @NotNull
  @Override
  public Text getName() {
    return () -> "CONSOLE";
  }

  @NotNull
  @Override
  public Server getServer() {
    return null;
  }

  @Override
  public void sendMessage(@NotNull final Text message, final @NotNull Object... params) {
  }

  @Override
  public boolean isOp() {
    return false;
  }

  @Override
  public void setOp(final boolean value) {
  }
}
