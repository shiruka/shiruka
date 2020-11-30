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

import io.github.shiruka.api.world.generators.GeneratorProvider;
import io.github.shiruka.api.world.options.GeneratorOptions;
import io.github.shiruka.api.world.options.LevelType;
import io.github.shiruka.api.world.options.WorldCreateSpec;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import io.github.shiruka.shiruka.world.generators.FlatGeneratorProvider;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation of {@link GeneratorOptions}.
 */
public final class ShirukaGeneratorOptions implements GeneratorOptions {

  /**
   * the seed source for the current instance of the server.
   */
  private static final Random SEED_SRC = new Random();

  private final boolean allowFeatures;

  @NotNull
  private final LevelType levelType;

  @NotNull
  private final String optionString;

  @NotNull
  private final GeneratorProvider provider;

  private final long seed;

  /**
   * ctor.
   *
   * @param spec the world spec.
   */
  public ShirukaGeneratorOptions(@NotNull final WorldCreateSpec spec) {
    if (spec.isDefault()) {
      this.provider = FlatGeneratorProvider.INSTANCE;
      this.levelType = LevelType.FLAT;
      this.optionString = "";
      this.allowFeatures = true;
      this.seed = ShirukaGeneratorOptions.verifySeed(0);
      return;
    }
    this.provider = spec.getProvider() == null ? FlatGeneratorProvider.INSTANCE : spec.getProvider();
    this.levelType = spec.getLevelType();
    this.optionString = spec.getOptionString();
    this.allowFeatures = spec.isAllowFeatures();
    this.seed = ShirukaGeneratorOptions.verifySeed(spec.getSeed());
  }

  /**
   * ctor.
   *
   * @param compound the compound to read from.
   */
  public ShirukaGeneratorOptions(@NotNull final CompoundTag compound) {
    final var providerClass = compound.get("TridentProvider");
    if (providerClass.isPresent() && providerClass.get().isString()) {
      try {
        this.provider = (GeneratorProvider) Class.forName(providerClass.get().asString().value()).newInstance();
      } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      this.provider = FlatGeneratorProvider.INSTANCE;
    }
    this.seed = compound.getLong("RandomSeed").orElseThrow();
    this.levelType = LevelType.from(compound.getString("generatorName").orElseThrow());
    this.optionString = compound.getString("generatorOptions").orElseThrow();
    this.allowFeatures = compound.getByte("MapFeatures").orElseThrow() == 1;
  }

  /**
   * verifies the seed, ensuring that it is not 0 as that will fuck with the RNG functions. If it is 0, tries
   * to calculate a new one.
   *
   * @param seed a possibly non-zero seed.
   *
   * @return a non-zero seed.
   */
  private static long verifySeed(final long seed) {
    if (seed == 0) {
      long potentialSeed;
      while ((potentialSeed = ShirukaGeneratorOptions.SEED_SRC.nextLong()) == 0) {
      }
      return potentialSeed;
    } else {
      return seed;
    }
  }

  @NotNull
  @Override
  public LevelType getLevelType() {
    return this.levelType;
  }

  @NotNull
  @Override
  public String getOptionString() {
    return this.optionString;
  }

  @NotNull
  @Override
  public GeneratorProvider getProvider() {
    return this.provider;
  }

  @Override
  public long getSeed() {
    return this.seed;
  }

  @Override
  public boolean isAllowFeatures() {
    return this.allowFeatures;
  }

  /**
   * saves the world options as NBT data.
   *
   * @param compound the data which represents. the data which is to be saved.
   */
  public void write(@NotNull final CompoundTag compound) {
    if (this.provider != FlatGeneratorProvider.INSTANCE) {
      compound.setString("TridentProvider", this.provider.getClass().getName());
    }
    compound.setLong("RandomSeed", this.seed);
    compound.setString("generatorName", this.levelType.toString());
    compound.setString("generatorOptions", this.optionString);
    compound.setByte("MapFeatures", (byte) (this.allowFeatures ? 1 : 0));
  }
}
