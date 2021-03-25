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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.shiruka.api.base.BanEntry;
import net.shiruka.shiruka.ban.ProfileBanEntry;
import net.shiruka.shiruka.ban.ShirukaProfileBanEntry;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents ip ban config.
 */
public final class ProfileBanConfig implements ConfigHolder {

  /**
   * the configuration.
   */
  private static FileConfiguration configuration;

  /**
   * the loader.
   */
  private static ConfigLoader loader;

  /**
   * adds the given {@code entry} to the section.
   *
   * @param entry the entry to add.
   */
  public static void addBanEntry(@NotNull final ProfileBanEntry entry) {
    Optional.ofNullable(entry.getKey()).ifPresent(key -> {
      ProfileBanConfig.configuration.set(key.getUniqueId().toString(), entry.serialize());
      try {
        ProfileBanConfig.loader.save();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * obtains all the ban entries.
   *
   * @return all the ban entries.
   */
  @NotNull
  public static Set<BanEntry> getBanEntries() {
    return ProfileBanConfig.configuration.getKeys(false).stream()
      .flatMap(key -> ProfileBanConfig.getBanEntry(key).stream())
      .collect(Collectors.toSet());
  }

  /**
   * gets the ban entry from the {@code target}.
   *
   * @param target the target to get.
   *
   * @return profile ban entry instance.
   */
  @NotNull
  public static Optional<BanEntry> getBanEntry(@NotNull final String target) {
    final var optional = UserCacheConfig.getProfileByXboxUniqueId(target);
    if (optional.isEmpty()) {
      return Optional.empty();
    }
    final var profile = optional.get();
    //noinspection unchecked
    return Optional.ofNullable(ProfileBanConfig.configuration.get(target))
      .filter(Map.class::isInstance)
      .map(o -> (Map<String, Object>) o)
      .map(ProfileBanEntry::new)
      .filter(entry -> !entry.hasExpired())
      .map(entry -> new ShirukaProfileBanEntry(profile, entry));
  }

  /**
   * checks if the given target is banned.
   *
   * @param target the target to check.
   *
   * @return {@code true} if the target is banned, {@code false} otherwise.
   */
  public static boolean isBanned(@NotNull final String target) {
    return ProfileBanConfig.configuration.contains(target);
  }

  /**
   * removes the given {@code target} from the section.
   *
   * @param target the target to remove.
   */
  public static void remove(@NotNull final String target) {
    ProfileBanConfig.configuration.set(target, null);
    try {
      ProfileBanConfig.loader.save();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
