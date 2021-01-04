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

import io.github.shiruka.api.world.WorldLoader;
import io.github.shiruka.shiruka.world.anvil.AnvilWorldLoader;
import java.util.Arrays;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * an enum class to determine world types.
 */
public enum WorldType {
  ANVIL(AnvilWorldLoader::new, "anvil");

  /**
   * the supplier.
   */
  @NotNull
  private final Supplier<WorldLoader> supplier;

  /**
   * the type.
   */
  @NotNull
  private final String type;

  /**
   * ctor.
   *
   * @param supplier the supplier.
   * @param type the type.
   */
  WorldType(@NotNull final Supplier<WorldLoader> supplier, @NotNull final String type) {
    this.supplier = supplier;
    this.type = type;
  }

  /**
   * obtains a world type instance from the given type.
   *
   * @param type the type to create.
   *
   * @return a new {@link WorldLoader} instance.
   */
  @NotNull
  public static WorldType fromType(@NotNull final String type) {
    return Arrays.stream(WorldType.values())
      .filter(worldType -> worldType.type.equalsIgnoreCase(type))
      .findFirst()
      .orElseThrow(() ->
        new IllegalStateException(String.format("The given world type called %s is not supported!", type)));
  }

  /**
   * creates a new instance of {@link WorldLoader}.
   *
   * @return a new world loader instance.
   */
  @NotNull
  public WorldLoader create() {
    return this.supplier.get();
  }
}
