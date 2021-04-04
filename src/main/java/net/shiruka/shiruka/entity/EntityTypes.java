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

package net.shiruka.shiruka.entity;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.base.Namespaced;
import net.shiruka.api.entity.Entity;
import net.shiruka.shiruka.entity.entities.ShirukaPlayerEntity;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents entity types.
 *
 * @param <E> type of the entity class.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityTypes<E extends Entity> {

  /**
   * the player entity type.
   */
  public static final EntityTypes<ShirukaPlayerEntity> PLAYER = new EntityTypes<>(
    EntityTypes.builder(),
    "player");

  /**
   * the factory.
   */
  @NotNull
  private final EntityFactory<E> factory;

  /**
   * the key.
   */
  @NotNull
  private final Namespaced key;

  /**
   * ctor.
   *
   * @param builder the builder.
   * @param key the key.
   */
  private EntityTypes(@NotNull final Builder<E> builder, @NotNull final Namespaced key) {
    this(builder.build(key), key);
  }

  /**
   * ctor.
   *
   * @param builder the builder.
   * @param key the key.
   */
  private EntityTypes(@NotNull final Builder<E> builder, @NotNull final String key) {
    this(builder, Namespaced.minecraft(key));
  }

  /**
   * creates a new builder instance.
   *
   * @param <T> type of the entity.
   *
   * @return a newly created builder instance.
   */
  @NotNull
  private static <T extends Entity> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * entity factory builder class.
   *
   * @param <T> type of the entity.
   */
  private static final class Builder<T extends Entity> {

    /**
     * builds the entity factory.
     *
     * @param key the key to build.
     *
     * @return built factory.
     */
    @NotNull
    private EntityFactory<T> build(@NotNull final Namespaced key) {
      return (type, world) -> {
        return null;
      };
    }
  }
}
