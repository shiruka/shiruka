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
import net.shiruka.api.entity.Entity;
import net.shiruka.api.entity.Player;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.base.ShirukaViewable;
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

  @Override
  public void tick() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement ShirukaEntity#tick.");
  }
}
