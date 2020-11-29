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

package io.github.shiruka.shiruka.world;

import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.Dimension;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link World}
 */
public abstract class ShirukaWorld implements World {

  /**
   * the dimension.
   */
  @NotNull
  protected final Dimension dimension;

  /**
   * the directory.
   */
  @NotNull
  protected final Path directory;

  /**
   * the name.
   */
  @NotNull
  protected final String name;

  /**
   * ctor.
   *
   * @param dimension the spec.
   * @param directory the enclosing.
   * @param name the name.
   */
  protected ShirukaWorld(@NotNull final Dimension dimension, @NotNull final Path directory, @NotNull final String name) {
    this.dimension = dimension;
    this.directory = directory;
    this.name = name;
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void loadSpawnChunks() {
  }
}
