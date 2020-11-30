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

package io.github.shiruka.shiruka.world.options;

import io.github.shiruka.api.util.Vector;
import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.WorldCreateSpec;
import io.github.shiruka.api.world.options.WorldOptions;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link WorldOptions}.
 */
public final class ShirukaWorldOptions implements WorldOptions {

  /**
   * the spawn.
   */
  private final AtomicReference<Vector> spawn = new AtomicReference<>();

  /**
   * the world.
   */
  @NotNull
  private final World world;

  /**
   * ctor.
   *
   * @param world the world possessing these options.
   * @param spec the world spec.
   */
  public ShirukaWorldOptions(@NotNull final World world, @NotNull final WorldCreateSpec spec) {
    this.world = world;
    if (!spec.isDefault()) {
      this.spawn.set(spec.getSpawn() == null ? this.randVector() : spec.getSpawn());
    } else {
      this.spawn.set(this.randVector());
    }
  }

  /**
   * ctor.
   *
   * @param world the world to create options for.
   * @param compound the compound to read data from.
   */
  public ShirukaWorldOptions(@NotNull final World world, @NotNull final CompoundTag compound) {
    this.world = world;
    this.spawn.set(Vector.create(
      compound.getInteger("SpawnX").orElseThrow(),
      compound.getInteger("SpawnY").orElseThrow(),
      compound.getInteger("SpawnZ").orElseThrow()));
  }

  @Override
  @NotNull
  public Vector getSpawn() {
    return this.spawn.get();
  }

  @Override
  public void setSpawn(@NotNull final Vector spawn) {
    this.spawn.set(spawn);
  }

  /**
   * saves the world options as NBT data.
   *
   * @param compound the compound to write to.
   */
  public void write(@NotNull final CompoundTag compound) {
    compound.setInteger("SpawnX", (int) this.spawn.get().getX());
    compound.setInteger("SpawnY", (int) this.spawn.get().getY());
    compound.setInteger("SpawnZ", (int) this.spawn.get().getZ());
  }

  /**
   * generates a random vector.
   *
   * @return a random vector.
   */
  @NotNull
  private Vector randVector() {
    final var r = ThreadLocalRandom.current();
    final var x = r.nextInt() % 1000;
    final var z = r.nextInt() % 1000;
    return Vector.create(x, this.world.getHighestY(x, z) + 1, z);
  }
}
