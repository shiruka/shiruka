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

package net.shiruka.shiruka.config;

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import io.github.portlek.configs.configuration.FileConfiguration;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import net.shiruka.api.base.GameProfile;
import net.shiruka.shiruka.ShirukaMain;
import org.jetbrains.annotations.NotNull;

/**
 * list of server operators.
 */
public final class UserCacheConfig implements ConfigHolder {

  /**
   * the configuration.
   */
  private static FileConfiguration configuration;

  /**
   * the loader.
   */
  private static ConfigLoader loader;

  /**
   * adds the given {@code profile} to the file.
   *
   * @param profile the profile to add.
   */
  public static void addProfile(@NotNull final GameProfile profile) {
    UserCacheConfig.addProfile(profile, false);
  }

  /**
   * adds the given {@code profile} to the file.
   *
   * @param profile the profile to add.
   * @param asyncSave the async save to add.
   */
  public static void addProfile(@NotNull final GameProfile profile, final boolean asyncSave) {
    UserCacheConfig.configuration.set(profile.getUniqueId().toString(), profile.serialize());
    if (asyncSave) {
      ShirukaMain.ASYNC_EXECUTOR.execute(UserCacheConfig::save);
    } else {
      UserCacheConfig.save();
    }
  }

  /**
   * gives the profile instance from the given {@code name}.
   *
   * @param name the name to get.
   *
   * @return game profile instance.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByName(@NotNull final String name) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement UserCacheConfig#getProfileByName.");
  }

  /**
   * gives the profile instance from the given {@code uniqueId}.
   *
   * @param uniqueId the uniqueId to get.
   *
   * @return game profile instance.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByUniqueId(@NotNull final UUID uniqueId) {
    var section = UserCacheConfig.configuration.getConfigurationSection(uniqueId.toString());
    if (section == null) {
      section = UserCacheConfig.configuration.createSection(uniqueId.toString());
    }
    return GameProfile.deserialize(section.getMapValues(false));
  }

  /**
   * gives the profile instance from the given {@code xboxUniqueId}.
   *
   * @param xboxUniqueId the xboxUniqueId to get.
   *
   * @return game profile instance.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByXboxUniqueId(@NotNull final String xboxUniqueId) {
    throw new UnsupportedOperationException(" @todo #1:10m Implement UserCacheConfig#getProfileByXboxUniqueId.");
  }

  /**
   * saves the file.
   */
  public static void save() {
    try {
      UserCacheConfig.loader.save();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
