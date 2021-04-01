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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.ImmutableBlockPosition;
import net.shiruka.api.base.Vector3D;
import net.shiruka.api.entity.Entity;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.api.util.MathHelper;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.entity.EntityTypes;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract implementation for {@link Entity}.
 */
public abstract class ShirukaEntity implements Entity {

  /**
   * the last entity id.
   */
  private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

  /**
   * the position lock.
   */
  public final Object positionLock = new Object();

  /**
   * the entity id.
   */
  private final long entityId = ShirukaEntity.LAST_ENTITY_ID.incrementAndGet();

  /**
   * the entity type.
   */
  @NotNull
  private final EntityTypes<? extends ShirukaEntity> entityType;

  /**
   * the last x.
   */
  public double lastX;

  /**
   * the last y.
   */
  public double lastY;

  /**
   * the last z.
   */
  public double lastZ;

  /**
   * the old x.
   */
  public double oldX;

  /**
   * the old y.
   */
  public double oldY;

  /**
   * the old z.
   */
  public double oldZ;

  /**
   * the pitch.
   */
  public float pitch;

  /**
   * the valid.
   */
  public boolean valid;

  /**
   * the world.
   */
  @NotNull
  public World world;

  /**
   * the yaw.
   */
  public float yaw;

  /**
   * the death.
   */
  private boolean death;

  /**
   * the location.
   */
  @NotNull
  @Getter
  private Vector3D location;

  /**
   * the location block.
   */
  @NotNull
  private ImmutableBlockPosition locationBlock = ImmutableBlockPosition.ZERO;

  /**
   * the motion.
   */
  @NotNull
  private Vector3D motion = Vector3D.ZERO;

  /**
   * the moved.
   */
  private boolean moved;

  /**
   * the preserve motion.
   */
  private boolean preserveMotion = true;

  /**
   * ctor.
   *
   * @param entityType the entity type.
   * @param world the world.
   */
  protected ShirukaEntity(@NotNull final EntityTypes<? extends ShirukaEntity> entityType, @NotNull final World world) {
    this.entityType = entityType;
    this.world = world;
    this.location = new Vector3D(0.0d, 0.0d, 0.0d);
  }

  @Override
  public final long getEntityId() {
    return this.entityId;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#remove.");
  }

  /**
   * obtains {@link #location}'s x value.
   *
   * @return x value of the location.
   */
  public final double getLocationAsX() {
    return this.location.getX();
  }

  /**
   * obtains {@link #location}'sy value.
   *
   * @return y value of the location.
   */
  public final double getLocationAsY() {
    return this.location.getY();
  }

  /**
   * obtains {@link #location}'s z value.
   *
   * @return z value of the location.
   */
  public final double getLocationAsZ() {
    return this.location.getZ();
  }

  /**
   * entity dies.
   */
  public void die() {
    this.death = true;
  }

  @NotNull
  @Override
  public List<MetadataValue> getMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#getMetadata.");
  }

  @Override
  public boolean hasMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#hasMetadata.");
  }

  @Override
  public void removeAllMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#removeAllMetadata.");
  }

  @Override
  public void removeMetadata(@NotNull final String key, @NotNull final Plugin plugin) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#removeMetadata.");
  }

  @Override
  public void setMetadata(@NotNull final String key, @NotNull final MetadataValue value) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#setMetadata.");
  }

  @NotNull
  @Override
  public Text getName() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#getName.");
  }

  /**
   * loads tha player's data from the given {@code tag}.
   *
   * @param tag the tag to load.
   */
  public void load(@NotNull final CompoundTag tag) {
    if (tag.isEmpty()) {
      return;
    }
    try {
      final var dataVersion = tag.hasKeyOfType("DataVersion", (byte) 1)
        ? tag.getInteger("DataVersion").orElse(0)
        : -1;
      final var positions = tag.getListTag("Pos", (byte) 6).orElse(Tag.createList());
      final var motion = tag.getListTag("Motion", (byte) 6).orElse(Tag.createList());
      final var rotation = tag.getListTag("Rotation", (byte) 5).orElse(Tag.createList());
    } catch (final Throwable t) {
      JiraExceptionCatcher.serverException(t);
    }
  }

  /**
   * sets the current and old position of the entity.
   *
   * @param x the x to set.
   * @param y the y to set.
   * @param z the z to set.
   */
  public void setCurrentAndOldPosition(final double x, final double y, final double z) {
    this.setPositionRaw(x, y, z);
    this.lastX = x;
    this.lastY = y;
    this.lastZ = z;
    this.oldX = x;
    this.oldY = y;
    this.oldZ = z;
  }

  /**
   * sets the position of the entity.
   *
   * @param x the x to set.
   * @param y the y to set.
   * @param z the z to set.
   */
  public void setPosition(final double x, final double y, final double z) {
    this.setPositionRaw(x, y, z);
    if (this.valid) {
      this.world.chunkCheck(this);
    }
  }

  /**
   * sets the position with raw values.
   *
   * @param x the x to set.
   * @param y the y to set.
   * @param z the z to set.
   */
  public void setPositionRaw(final double x, final double y, final double z) {
    if (!(this instanceof ShirukaHangingEntity) &&
      (this.location.getX() != x || this.location.getY() != y || this.location.getZ() != z)) {
      this.setBoundingBox(this.size.a(x, y, z));
    }
    if (this.location.getX() != x ||
      this.location.getY() != y ||
      this.location.getZ() != z) {
      synchronized (this.positionLock) {
        this.location = new Vector3D(x, y, z);
      }
      final var floorX = MathHelper.floor(x);
      final var floorY = MathHelper.floor(y);
      final var floorZ = MathHelper.floor(z);
      if (floorX != this.locationBlock.getX() ||
        floorY != this.locationBlock.getY() ||
        floorZ != this.locationBlock.getZ()) {
        this.locationBlock = new ImmutableBlockPosition(floorX, floorY, floorZ);
      }
      this.moved = true;
    }
  }

  /**
   * sets the position and rotation of the entity.
   *
   * @param x the x to set.
   * @param y the y to set.
   * @param z the z to set.
   * @param yaw the yaw to set.
   * @param pitch the pitch to set.
   */
  public void setPositionRotation(final double x, final double y, final double z, final float yaw, final float pitch) {
    if (!this.preserveMotion) {
      this.motion = Vector3D.ZERO;
    } else {
      this.preserveMotion = false;
    }
    this.setCurrentAndOldPosition(x, y, z);
    this.yaw = yaw;
    this.pitch = pitch;
    this.syncPosition();
  }

  /**
   * sets the position and rotation of the entity.
   *
   * @param position the position to set.
   * @param yaw the yaw to set.
   * @param pitch the pitch to set.
   */
  public void setPositionRotation(@NotNull final ImmutableBlockPosition position, final float yaw, final float pitch) {
    this.setPositionRotation((double) position.getX() + 0.5D, position.getY(), (double) position.getZ() + 0.5D, yaw, pitch);
  }

  @Override
  public void tick() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#tick.");
  }

  /**
   * entity spawns in the given {@code world}.
   *
   * @param world the world to spawn.
   */
  protected void spawnIn(@Nullable final World world) {
    if (world == null) {
      this.die();
      this.world = Shiruka.getWorldManager().getWorlds().get(0);
    } else {
      this.world = world;
    }
  }

  /**
   * syncs the position.
   */
  protected void syncPosition() {
    this.setPosition(this.location.getX(), this.location.getY(), this.location.getZ());
  }
}
