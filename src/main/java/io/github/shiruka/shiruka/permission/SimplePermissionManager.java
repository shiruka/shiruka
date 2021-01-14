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

package io.github.shiruka.shiruka.permission;

import io.github.shiruka.api.permission.Permissible;
import io.github.shiruka.api.permission.Permission;
import io.github.shiruka.api.permission.PermissionManager;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link PermissionManager}.
 *
 * @todo #1:60m Implement permission manager methods.
 */
public final class SimplePermissionManager implements PermissionManager {

  @Override
  public void addPermission(@NotNull final Permission perm) {
  }

  @NotNull
  @Override
  public Set<Permissible> getDefaultPermSubscriptions(final boolean op) {
    return null;
  }

  @NotNull
  @Override
  public Set<Permission> getDefaultPermissions(final boolean op) {
    return null;
  }

  @NotNull
  @Override
  public Optional<Permission> getPermission(@NotNull final String name) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Set<Permissible> getPermissionSubscriptions(@NotNull final String permission) {
    return null;
  }

  @NotNull
  @Override
  public Set<Permission> getPermissions() {
    return null;
  }

  @Override
  public void recalculatePermissionDefaults(@NotNull final Permission perm) {
  }

  @Override
  public void removePermission(@NotNull final Permission perm) {
  }

  @Override
  public void removePermission(@NotNull final String name) {
  }

  @Override
  public void subscribeToDefaultPerms(final boolean op, @NotNull final Permissible permissible) {
  }

  @Override
  public void subscribeToPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
  }

  @Override
  public void unsubscribeFromDefaultPerms(final boolean op, @NotNull final Permissible permissible) {
  }

  @Override
  public void unsubscribeFromPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
  }
}
