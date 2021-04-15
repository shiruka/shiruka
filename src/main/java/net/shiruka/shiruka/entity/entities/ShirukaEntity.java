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
import net.shiruka.api.base.BlockPosition;
import net.shiruka.api.base.Location;
import net.shiruka.api.entity.Entity;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.entity.EntityTypes;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.Tag;
import net.shiruka.shiruka.nbt.TagTypes;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Entity}.
 */
public abstract class ShirukaEntity implements Entity {

  /**
   * the last entity id.
   */
  private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

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
   * the location.
   */
  @NotNull
  @Getter
  private final Location location;

  /**
   * the location block.
   */
  @NotNull
  private final BlockPosition locationBlock = BlockPosition.ZERO;

  /**
   * the motion.
   */
  @NotNull
  private final BlockPosition motion = BlockPosition.ZERO;

  /**
   * the preserve motion.
   */
  private final boolean preserveMotion = true;

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
   * the moved.
   */
  private boolean moved;

  /**
   * ctor.
   *
   * @param entityType the entity type.
   * @param world the world.
   */
  protected ShirukaEntity(@NotNull final EntityTypes<? extends ShirukaEntity> entityType, @NotNull final World world) {
    this.entityType = entityType;
    this.world = world;
    this.location = new Location(world, 0.0d, 0.0d, 0.0d);
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
      final var dataVersion = tag.hasKeyOfType("DataVersion", TagTypes.BYTE)
        ? tag.getInteger("DataVersion").orElse(0)
        : -1;
      final var positions = tag.getListTag("Pos", TagTypes.DOUBLE).orElse(Tag.createList());
      final var motion = tag.getListTag("Motion", TagTypes.DOUBLE).orElse(Tag.createList());
      final var rotation = tag.getListTag("Rotation", TagTypes.FLOAT).orElse(Tag.createList());
    } catch (final Throwable t) {
      JiraExceptionCatcher.serverException(t);
    }
  }

  @Override
  public void tick() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#tick.");
  }
}
