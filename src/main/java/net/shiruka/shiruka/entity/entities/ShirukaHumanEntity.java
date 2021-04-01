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

package net.shiruka.shiruka.entity.entities;

import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.entity.HumanEntity;
import net.shiruka.api.permission.PermissibleBase;
import net.shiruka.api.permission.Permission;
import net.shiruka.api.permission.PermissionAttachment;
import net.shiruka.api.permission.PermissionAttachmentInfo;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract class that represents human entity.
 */
public abstract class ShirukaHumanEntity extends ShirukaEntity implements HumanEntity {

  /**
   * the permissible.
   */
  @NotNull
  protected final PermissibleBase permissible;

  /**
   * the profile.
   */
  @NotNull
  @Getter
  private final GameProfile profile;

  /**
   * the op.
   */
  private boolean op;

  /**
   * ctor.
   *
   * @param world the world.
   * @param profile the profile.
   */
  protected ShirukaHumanEntity(@NotNull final World world, @NotNull final GameProfile profile) {
    super(EntityTypes.PLAYER, world);
    this.profile = profile;
    this.permissible = new PermissibleBase(this);
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                            final boolean value) {
    return this.permissible.addAttachment(plugin, name, value);
  }

  @NotNull
  @Override
  public PermissionAttachment addAttachment(@NotNull final Plugin plugin) {
    return this.permissible.addAttachment(plugin);
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, @NotNull final String name,
                                                      final boolean value, final long ticks) {
    return this.permissible.addAttachment(plugin, name, value, ticks);
  }

  @NotNull
  @Override
  public Optional<PermissionAttachment> addAttachment(@NotNull final Plugin plugin, final long ticks) {
    return this.permissible.addAttachment(plugin, ticks);
  }

  @NotNull
  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    return this.permissible.getEffectivePermissions();
  }

  @Override
  public boolean hasPermission(@NotNull final String name) {
    return this.permissible.hasPermission(name);
  }

  @Override
  public boolean hasPermission(@NotNull final Permission perm) {
    return this.permissible.hasPermission(perm);
  }

  @Override
  public boolean isPermissionSet(@NotNull final String name) {
    return this.permissible.isPermissionSet(name);
  }

  @Override
  public boolean isPermissionSet(@NotNull final Permission perm) {
    return this.permissible.isPermissionSet(perm);
  }

  @Override
  public void recalculatePermissions() {
    this.permissible.recalculatePermissions();
  }

  @Override
  public void removeAttachment(@NotNull final PermissionAttachment attachment) {
    this.permissible.removeAttachment(attachment);
  }

  @NotNull
  @Override
  public Text getName() {
    return this.getProfile().getName();
  }

  @Override
  public boolean isOp() {
    return this.op;
  }

  @Override
  public void setOp(final boolean value) {
    this.op = value;
    this.permissible.recalculatePermissions();
  }
}
