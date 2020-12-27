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

import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.options.Weather;
import io.github.shiruka.shiruka.nbt.CompoundTag;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

/**
 * the implementation of the current weather conditions in a world.
 */
public final class ShirukaWeather implements Weather {

  /**
   * the max time in ticks that can elapse before the weather toggles (3 hours).
   */
  private static final int MAX_RAND = 20 * 60 * 60 * 3;

  /**
   * amount of time in ticks until the rain toggles.
   */
  private final AtomicInteger rainTime = new AtomicInteger(ThreadLocalRandom.current().nextInt(ShirukaWeather.MAX_RAND));

  /**
   * amount of time in ticks until thunder toggles, unless it is not raining.
   */
  private final AtomicInteger thunderTime =
    new AtomicInteger(ThreadLocalRandom.current().nextInt(ShirukaWeather.MAX_RAND));

  /**
   * the current weather state.
   */
  private final AtomicReference<WeatherState> weatherState = new AtomicReference<>(WeatherState.CLEAR);

  /**
   * the world that has these weather conditions.
   */
  @NotNull
  private final World world;

  /**
   * ctr.
   *
   * @param world the world
   */
  public ShirukaWeather(@NotNull final World world) {
    this.world = world;
  }

  @Override
  public void beginRaining() {
    if (this.weatherState.compareAndSet(WeatherState.CLEAR, WeatherState.RAINING)) {
      // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutGameState(2, 0));
    }
  }

  @Override
  public void beginThunder() {
    this.weatherState.compareAndSet(WeatherState.RAINING, WeatherState.RAINING_THUNDERING);
  }

  @Override
  public void clear() {
    this.weatherState.set(WeatherState.CLEAR);
    // @todo #1:60m RecipientSelector.inWorld(this.world, new PlayOutGameState(1, 0));
  }

  @Override
  public int getClearTime() {
    return this.rainTime.get();
  }

  @Override
  public void setClearTime(int ticks) {
    if (ticks == Weather.RANDOM_TIME) {
      ticks = ThreadLocalRandom.current().nextInt(ShirukaWeather.MAX_RAND);
    }
    this.rainTime.set(ticks);
  }

  @Override
  public int getRainTime() {
    return this.rainTime.get();
  }

  @Override
  public void setRainTime(final int ticks) {
    var tmp = ticks;
    if (ticks == Weather.RANDOM_TIME) {
      tmp = ThreadLocalRandom.current().nextInt(ShirukaWeather.MAX_RAND);
    }
    this.rainTime.set(tmp);
  }

  @Override
  public int getThunderTime() {
    return this.thunderTime.get();
  }

  @Override
  public void setThunderTime(int ticks) {
    if (ticks == Weather.RANDOM_TIME) {
      ticks = ThreadLocalRandom.current().nextInt(ShirukaWeather.MAX_RAND);
    }
    this.thunderTime.set(ticks);
  }

  @Override
  public boolean isClear() {
    return this.weatherState.get() == WeatherState.CLEAR;
  }

  @Override
  public boolean isRaining() {
    final var state = this.weatherState.get();
    return state == WeatherState.RAINING || state == WeatherState.RAINING_THUNDERING;
  }

  @Override
  public boolean isThundering() {
    return this.weatherState.get() == WeatherState.RAINING_THUNDERING;
  }

  @Override
  public void stopThunder() {
    this.weatherState.compareAndSet(WeatherState.RAINING_THUNDERING, WeatherState.RAINING);
  }

  public void read(@NotNull final CompoundTag compound) {
    this.rainTime.set(compound.getInteger("rainTime").orElseThrow());
    this.thunderTime.set(compound.getInteger("thunderTime").orElseThrow());
    if (compound.getByte("thundering").orElseThrow() == 1) {
      this.weatherState.set(WeatherState.RAINING_THUNDERING);
    } else {
      if (compound.getByte("raining").orElseThrow() == 1) {
        this.weatherState.set(WeatherState.RAINING);
      }
    }
  }

  @Override
  public void tick() {
    // @todo #1:15m tick operations.
  }

  public void write(@NotNull final CompoundTag compound) {
    final var state = this.weatherState.get();
    final var rainTime = this.rainTime.get();
    compound.setByte("raining", (byte) (state == WeatherState.RAINING || state == WeatherState.RAINING_THUNDERING ? 1 : 0));
    compound.setInteger("rainTime", rainTime);
    compound.setByte("thundering", (byte) (state == WeatherState.RAINING_THUNDERING ? 1 : 0));
    compound.setInteger("thunderTime", this.thunderTime.get());
    compound.setInteger("clearWeatherTime", rainTime);
  }

  /**
   * the state of the weather in the world.
   */
  private enum WeatherState {
    CLEAR, RAINING, RAINING_THUNDERING
  }
}
