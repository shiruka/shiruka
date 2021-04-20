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

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.registry.Resourced;
import net.shiruka.api.world.World;
import net.shiruka.api.world.WorldManager;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link WorldManager}.
 */
@RequiredArgsConstructor
public final class SimpleWorldManager implements WorldManager {

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the worlds by {@link String}.
   */
  private final Map<String, World> worldsByName = new Object2ObjectLinkedOpenHashMap<>();

  /**
   * the worlds by {@link Resourced}.
   */
  private final Map<Resourced, World> worldsByResource = new Object2ObjectLinkedOpenHashMap<>();

  /**
   * the worlds by {@link UUID}.
   */
  private final Map<UUID, World> worldsByUniqueId = new Object2ObjectLinkedOpenHashMap<>();

  /**
   * gets the default world.
   *
   * @return default world.
   */
  @NotNull
  public World getDefaultWorld() {
    return this.getWorldByResource(World.OVER_WORLD).orElseThrow(() ->
      // @todo #1:5m Add language support for "Default world not found!".
      new IllegalStateException("Default world not found!"));
  }

  @NotNull
  @Override
  public Optional<World> getWorldByName(@NotNull final String name) {
    return Optional.ofNullable(this.worldsByName.get(name));
  }

  @NotNull
  @Override
  public Optional<World> getWorldByResource(@NotNull final Resourced resource) {
    return Optional.ofNullable(this.worldsByResource.get(resource));
  }

  @NotNull
  @Override
  public Optional<World> getWorldByUniqueId(@NotNull final UUID uniqueId) {
    return Optional.of(this.worldsByUniqueId.get(uniqueId));
  }

  @NotNull
  @Override
  public Collection<World> getWorlds() {
    return this.worldsByResource.values();
  }
}
