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

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.shiruka.api.permission.Permissible;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionManager;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link PermissionManager}.
 */
public final class SimplePermissionManager implements PermissionManager {

  /**
   * the default subscription.
   */
  private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new Object2ObjectOpenHashMap<>();

  /**
   * the default permissions.
   */
  private final Map<Boolean, Set<Permission>> defaultPerms = new Object2ObjectLinkedOpenHashMap<>() {{
    this.put(true, new ObjectLinkedOpenHashSet<>());
    this.put(false, new ObjectLinkedOpenHashSet<>());
  }};

  /**
   * the permission subscriptions.
   */
  private final Map<String, Map<Permissible, Boolean>> permSubs = new Object2ObjectOpenHashMap<>();

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
    return ImmutableSet.copyOf(this.defaultPerms.get(op));
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
    this.defSubs.computeIfAbsent(op, k -> new WeakHashMap<>())
      .put(permissible, true);
  }

  @Override
  public void subscribeToPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimplePermissionManager#subscribeToPermission.");
  }

  @Override
  public void unsubscribeFromDefaultPerms(final boolean op, @NotNull final Permissible permissible) {
    final var map = this.defSubs.get(op);
    if (map == null) {
      return;
    }
    map.remove(permissible);
    if (map.isEmpty()) {
      this.defSubs.remove(op);
    }
  }

  @Override
  public void unsubscribeFromPermission(@NotNull final String permission, @NotNull final Permissible permissible) {
    final var name = permission.toLowerCase(Locale.ENGLISH);
    final var map = this.permSubs.get(name);
    if (map == null) {
      return;
    }
    map.remove(permissible);
    if (map.isEmpty()) {
      this.permSubs.remove(name);
    }
  }
}
