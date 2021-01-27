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

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.shiruka.api.base.GameProfile;
import net.shiruka.api.config.Config;
import net.shiruka.api.config.config.PathableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * list of server operators.
 */
public final class UserCacheConfig extends PathableConfig {

  /**
   * the instance.
   */
  @Nullable
  private static UserCacheConfig instance;

  /**
   * ctor.
   *
   * @param origin the origin.
   */
  private UserCacheConfig(@NotNull final Config origin) {
    super(origin);
  }

  /**
   * obtians the instance.
   *
   * @return instance.
   */
  @NotNull
  public static UserCacheConfig getInstance() {
    return Objects.requireNonNull(UserCacheConfig.instance);
  }

  /**
   * gives the profile instance from the given {@code name}.
   *
   * @param name the name to get.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByName(@NotNull final String name) {
    return Optional.empty();
  }

  /**
   * gives the profile instance from the given {@code xboxUniqueId}.
   *
   * @param xboxUniqueId the xboxUniqueId to get.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByXboxUniqueId(@NotNull final String xboxUniqueId) {
    return Optional.empty();
  }

  /**
   * gives the profile instance from the given {@code uniqueId}.
   *
   * @param uniqueId the uniqueId to get.
   */
  @NotNull
  public static Optional<GameProfile> getProfileByUniqueId(@NotNull final UUID uniqueId) {
    return Optional.empty();
  }

  /**
   * initiates the server config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    Config.fromFile(file)
      .map(UserCacheConfig::new)
      .ifPresent(config -> {
        config.save();
        UserCacheConfig.instance = config;
      });
  }
}
