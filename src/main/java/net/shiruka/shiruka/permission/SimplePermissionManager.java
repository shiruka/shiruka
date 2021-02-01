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

package net.shiruka.shiruka.permission;

import java.util.Optional;
import java.util.Set;
import net.shiruka.api.permission.Permissible;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionManager;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link PermissionManager}.
 */
public final class SimplePermissionManager implements PermissionManager {

  @Override
  public void addPermission(@NotNull final Permission perm) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#addPermission.");
  }

  @NotNull
  @Override
  public Set<Permissible> getDefaultPermSubscriptions(final boolean op) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#getDefaultPermSubscriptions.");
  }

  @NotNull
  @Override
  public Set<Permission> getDefaultPermissions(final boolean op) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#getDefaultPermissions.");
  }

  @NotNull
  @Override
  public Optional<Permission> getPermission(@NotNull final String name) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#getPermission.");
  }

  @NotNull
  @Override
  public Set<Permissible> getPermissionSubscriptions(@NotNull final String permission) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#getPermissionSubscriptions.");
  }

  @NotNull
  @Override
  public Set<Permission> getPermissions() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#getPermissions.");
  }

  @Override
  public void recalculatePermissionDefaults(@NotNull final Permission perm) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#recalculatePermissionDefaults.");
  }

  @Override
  public void removePermission(@NotNull final Permission perm) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#removePermission(Permission).");
  }

  @Override
  public void removePermission(@NotNull final String name) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#removePermission(String).");
  }

  @Override
  public void subscribeToDefaultPerms(final boolean op, @NotNull final Permissible permissible) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#subscribeToDefaultPerms.");
  }

  @Override
  public void subscribeToPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#subscribeToPermission.");
  }

  @Override
  public void unsubscribeFromDefaultPerms(final boolean op, @NotNull final Permissible permissible) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#unsubscribeFromDefaultPerms.");
  }

  @Override
  public void unsubscribeFromPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#unsubscribeFromPermission.");
  }
}
