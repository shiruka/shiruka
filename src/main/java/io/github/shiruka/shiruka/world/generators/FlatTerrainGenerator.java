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

package io.github.shiruka.shiruka.world.generators;

import io.github.shiruka.api.world.generators.GeneratorContext;
import io.github.shiruka.api.world.generators.TerrainGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * this generator generates the base chunk layer used for generating flat worlds.
 */
public final class FlatTerrainGenerator implements TerrainGenerator {

  /**
   * instance of this generator.
   */
  public static final FlatTerrainGenerator INSTANCE = new FlatTerrainGenerator();

  /**
   * ctpr.
   */
  private FlatTerrainGenerator() {
  }

  @Override
  public void generate(final int chunkX, final int chunkZ, @NotNull final GeneratorContext context) {
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        context.set(x, 0, z, 7, (byte) 0);
        context.set(x, 1, z, 3, (byte) 0);
        context.set(x, 2, z, 3, (byte) 0);
        context.set(x, 3, z, 2, (byte) 0);
      }
    }
  }
}
