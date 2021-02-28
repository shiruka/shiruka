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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.shiruka.api.world.ChunkData;
import net.shiruka.api.world.World;
import net.shiruka.api.world.WorldCreator;
import net.shiruka.api.world.WorldManager;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link WorldManager}.
 */
public final class SimpleWorldManager implements WorldManager {

  @NotNull
  @Override
  public ChunkData createChunkData(@NotNull final World world) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleWorldManager#createChunkData.");
  }

  @NotNull
  @Override
  public ChunkData createNativeChunkData(@NotNull final World world, final int x, final int z) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleWorldManager#createNativeChunkData.");
  }

  @NotNull
  @Override
  public Optional<World> createWorld(@NotNull final WorldCreator worldCreator) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleWorldManager#createWorld.");
  }

  @NotNull
  @Override
  public Optional<World> getWorld(@NotNull final UUID uniqueId) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleWorldManager#getWorld.");
  }

  @NotNull
  @Override
  public List<World> getWorlds() {
    throw new UnsupportedOperationException(" @todo #1:10m Implement SimpleWorldManager#getWorlds.");
  }
}
