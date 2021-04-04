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

package net.shiruka.shiruka.world;

import java.util.UUID;
import net.shiruka.api.Shiruka;
import net.shiruka.api.registry.Resourced;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains utility method for dimensions.
 */
public final class Dimensions {

  /**
   * ctor.
   */
  private Dimensions() {
  }

  /**
   * gives a {@link Resourced} instance from the given {@code tag}.
   *
   * @param tag the tag to get.
   *
   * @return the resource instance.
   */
  @NotNull
  public static Resourced fromTag(@Nullable final CompoundTag tag) {
    if (tag == null) {
      return World.OVER_WORLD;
    }
    if (tag.containsKey("WorldUUIDMost") && tag.containsKey("WorldUUIDLeast")) {
      final var worldUniqueId = new UUID(tag.getLong("WorldUUIDMost").orElseThrow(),
        tag.getLong("WorldUUIDLeast").orElseThrow());
      return Shiruka.getWorldManager().getWorldByUniqueId(worldUniqueId)
        .map(World::getDimensionKey)
        .orElse(World.OVER_WORLD);
    }
    final var dimension = tag.get("Dimension");
    if (dimension.isEmpty() || !dimension.get().isNumber()) {
      return World.OVER_WORLD;
    }
    final var dimensionId = dimension.get()
      .asNumber()
      .intValue();
    if (dimensionId == -1) {
      return World.THE_NETHER;
    }
    if (dimensionId == 0) {
      return World.OVER_WORLD;
    }
    if (dimensionId == 1) {
      return World.THE_END;
    }
    return World.OVER_WORLD;
  }
}
