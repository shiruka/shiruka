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

package io.github.shiruka.shiruka.world;

import io.github.shiruka.api.world.ChunkData;
import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.WorldCreator;
import io.github.shiruka.api.world.WorldManager;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link WorldManager}.
 */
public final class SimpleWorldManager implements WorldManager {

  @NotNull
  @Override
  public ChunkData createChunkData(@NotNull final World world) {
    return null;
  }

  @NotNull
  @Override
  public ChunkData createNativeChunkData(@NotNull final World world, final int x, final int z) {
    return null;
  }

  @NotNull
  @Override
  public Optional<World> createWorld(@NotNull final WorldCreator worldCreator) {
    return Optional.empty();
  }
}
