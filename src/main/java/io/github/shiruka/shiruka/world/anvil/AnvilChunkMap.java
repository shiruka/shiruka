/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
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

package io.github.shiruka.shiruka.world.anvil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an anvil chunk map.
 */
public final class AnvilChunkMap implements Iterable<AnvilChunk> {

  /**
   * the actual map of chunks.
   */
  private final Map<Long, AnvilChunk> chunks = new ConcurrentHashMap<>();

  /**
   * the lock guarding the chunk map.
   */
  private final Object lock = new Object();

  /**
   * the world holding the chunks in this map.
   */
  @NotNull
  private final AnvilWorld world;

  /**
   * ctor.
   *
   * @param world the world to hold chunks.
   */
  protected AnvilChunkMap(@NotNull final AnvilWorld world) {
    this.world = world;
  }

  /**
   * obtains the chunk at the given location and determines whether a chunk will be generated if it does not exist yet.
   *
   * @param x the x coordinate.
   * @param z the z coordinate.
   * @param gen {@code true} to generate if non-existent.
   *
   * @return the chunk, or {@code null}.
   */
  @Nullable
  public final AnvilChunk get(final int x, final int z, final boolean gen) {
    final var key = (long) x << 32 | z & 0xFFFFFFFFL;
    boolean doGenerate = false;
    AnvilChunk chunk;
    synchronized (this.lock) {
      chunk = this.chunks.get(key);
      if ((chunk == null || !chunk.canUse()) && gen) {
        chunk = new AnvilChunk(this.world, x, z);
        this.chunks.put(key, chunk);
        doGenerate = true;
      }
    }
    if (doGenerate) {
      chunk.generate();
    }
    if (chunk != null) {
      return chunk.waitReady();
    }
    return null;
  }

  @NotNull
  @Override
  public Iterator<AnvilChunk> iterator() {
    return this.values().iterator();
  }

  @Override
  public void forEach(@NotNull final Consumer<? super AnvilChunk> action) {
    synchronized (this.lock) {
      for (final var chunk : this) {
        action.accept(chunk);
      }
    }
  }

  /**
   * removes the chunk at the given coordinates.
   *
   * @param x the chunk X coordinate.
   * @param z the chunk Z coordinate.
   *
   * @return the chunk that was removed, or {@code null} if nothing happened.
   */
  @Nullable
  public AnvilChunk remove(final int x, final int z) {
    final long key = (long) x << 32 | z & 0xFFFFFFFFL;
    synchronized (this.lock) {
      return this.chunks.remove(key);
    }
  }

  /**
   * all of the loaded chunks.
   *
   * @return the values of the chunk map.
   */
  @NotNull
  public Collection<AnvilChunk> values() {
    synchronized (this.lock) {
      return this.chunks.values();
    }
  }
}
