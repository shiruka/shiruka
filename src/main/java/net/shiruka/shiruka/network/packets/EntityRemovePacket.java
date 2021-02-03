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

package net.shiruka.shiruka.network.packets;

import net.shiruka.shiruka.network.ShirukaPacket;

/**
 * sent by the server to remove an entity that currently exists in the world from the client-side.
 */
public final class EntityRemovePacket extends ShirukaPacket {

  /**
   * the entity id.
   */
  private final long entityId;

  /**
   * ctor.
   *
   * @param entityId the entity id.
   */
  public EntityRemovePacket(final long entityId) {
    super(ShirukaPacket.ID_REMOVE_ENTITY);
    this.entityId = entityId;
  }

  @Override
  public void encode() {
    this.writeLong(this.entityId);
  }

  /**
   * obtains the entity id.
   *
   * @return entity id.
   */
  public long getEntityId() {
    return this.entityId;
  }
}
