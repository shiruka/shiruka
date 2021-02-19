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

package net.shiruka.shiruka.entities;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import net.shiruka.api.base.Vector;
import net.shiruka.api.entity.Entity;
import net.shiruka.api.entity.Player;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.base.ShirukaViewable;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import net.shiruka.shiruka.nbt.CompoundTag;
import net.shiruka.shiruka.nbt.ListTag;
import net.shiruka.shiruka.nbt.Tag;
import net.shiruka.shiruka.network.packets.EntityRemovePacket;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link Entity}.
 */
public abstract class ShirukaEntity implements Entity, ShirukaViewable {

  /**
   * the last entity id.
   */
  private static final AtomicInteger LAST_ENTITY_ID = new AtomicInteger();

  /**
   * the viewers.
   */
  protected final Set<Player> viewers = new CopyOnWriteArraySet<>();

  /**
   * the entity id.
   */
  private final long entityId;

  /**
   * the motion.
   */
  @NotNull
  private Vector motion = new Vector(0.0d, 0.0d, 0.0d);

  /**
   * ctor.
   */
  protected ShirukaEntity() {
    this.entityId = ShirukaEntity.LAST_ENTITY_ID.incrementAndGet();
  }

  @Override
  public final long getEntityId() {
    return this.entityId;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#remove.");
  }

  @Override
  public boolean addViewer(@NotNull final Player player) {
    final var result = this.viewers.add(player);
    if (!result) {
      return false;
    }
    if (player instanceof ShirukaPlayer) {
      ((ShirukaPlayer) player).viewableEntities.add(this);
    }
    return true;
  }

  @NotNull
  @Override
  public Set<Player> getViewers() {
    return Collections.unmodifiableSet(this.viewers);
  }

  @Override
  public boolean removeViewer(@NotNull final Player player) {
    if (!this.viewers.remove(player)) {
      return false;
    }
    if (player instanceof ShirukaPlayer) {
      final var packet = new EntityRemovePacket(this.entityId);
      final var shirukaPlayer = (ShirukaPlayer) player;
      shirukaPlayer.getConnection().sendPacket(packet);
      shirukaPlayer.viewableEntities.remove(this);
    }
    return true;
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
    try {
      final var dataVersion = tag.hasKeyOfType("DataVersion", (byte) 1)
        ? tag.getInteger("DataVersion").orElse(0)
        : -1;
      final var positions = tag.getListTag("Pos", (byte) 6).orElse(Tag.createList());
      final var motion = tag.getListTag("Motion", (byte) 6).orElse(Tag.createList());
      final var rotation = tag.getListTag("Pos", (byte) 5).orElse(Tag.createList());
      this.setMotion(motion);
    } catch (final Throwable t) {
      JiraExceptionCatcher.serverException(t);
    }
  }

  /**
   * sets the entity's motion.
   *
   * @param motion the motion to set.
   */
  public void setMotion(@NotNull final ListTag motion) {
    final var motion0 = motion.getDouble(0).orElse(0.0d);
    final var motion1 = motion.getDouble(1).orElse(0.0d);
    final var motion2 = motion.getDouble(2).orElse(0.0d);
    this.setMotion(Math.abs(motion0) > 10.0D ? 0.0D : motion0,
      Math.abs(motion1) > 10.0D ? 0.0D : motion1,
      Math.abs(motion2) > 10.0D ? 0.0D : motion2);
  }

  /**
   * sets the entity's motion.
   *
   * @param motion0 the motion 0 to set.
   * @param motion1 the motion 1 to set.
   * @param motion2 the motion 2 to set.
   */
  public void setMotion(final double motion0, final double motion1, final double motion2) {
    this.setMotion(new Vector(motion0, motion1, motion2));
  }

  /**
   * sets the entity's motion.
   *
   * @param motion the motion to set.
   */
  public void setMotion(@NotNull final Vector motion) {
    this.motion = motion;
  }

  @Override
  public void tick() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#tick.");
  }
}
