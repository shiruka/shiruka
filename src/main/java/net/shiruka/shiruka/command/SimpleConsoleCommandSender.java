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

import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.command.sender.ConsoleCommandSender;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionAttachment;
import net.shiruka.api.permission.PermissionAttachmentInfo;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.console.ShirukaConsole;
import org.jetbrains.annotations.NotNull;

/**
 * a simple console implementation for {@link ConsoleCommandSender}.
 */
@RequiredArgsConstructor
public final class SimpleConsoleCommandSender implements ConsoleCommandSender {

  /**
   * the console name.
   */
  private static final Text CONSOLE_NAME = () -> "CONSOLE";

  /**
   * the console.
   */
  @NotNull
  private final ShirukaConsole console;

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin, @NotNull final String name, final boolean value) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#addAttachment(Plugin, String, boolean).");
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#addAttachment(Plugin).");
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, @NotNull final String name, final boolean value, final long ticks) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#addAttachment(Plugin, String, boolean, long).");
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, final long ticks) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#addAttachment(Plugin, long).");
  }

  @NotNull
  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#getEffectivePermissions.");
  }

  @Override
  public boolean hasPermission(@NotNull final String name) {
    return true;
  }

  @Override
  public boolean hasPermission(@NotNull final Permission perm) {
    return true;
  }

  @Override
  public boolean isPermissionSet(@NotNull final String name) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#isPermissionSet(String).");
  }

  @Override
  public boolean isPermissionSet(@NotNull final Permission perm) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#isPermissionSet(Permission).");
  }

  @Override
  public void recalculatePermissions() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#recalculatePermissions.");
  }

  @Override
  public void removeAttachment(@NotNull final PermissionAttachment attachment) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleConsoleCommandSender#removeAttachment.");
  }

  @NotNull
  @Override
  public Text getName() {
    return SimpleConsoleCommandSender.CONSOLE_NAME;
  }

  @Override
  public boolean isOp() {
    return true;
  }

  @Override
  public void setOp(final boolean value) {
  }

  @Override
  public void sendMessage(@NotNull final String message) {
    System.out.println(message);
  }
}
