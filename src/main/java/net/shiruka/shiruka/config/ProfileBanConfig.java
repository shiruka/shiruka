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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.shiruka.api.base.BanEntry;
import net.shiruka.api.config.Config;
import net.shiruka.api.config.config.PathableConfig;
import net.shiruka.shiruka.ban.ProfileBanEntry;
import net.shiruka.shiruka.ban.ShirukaProfileBanEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents ip ban config.
 */
public final class ProfileBanConfig extends PathableConfig {

  /**
   * the instance.
   */
  @Nullable
  private static ProfileBanConfig instance;

  /**
   * ctor.
   *
   * @param origin the origin.
   */
  private ProfileBanConfig(@NotNull final Config origin) {
    super(origin);
  }

  /**
   * adds the given {@code entry} to the section.
   *
   * @param entry the entry to add.
   */
  public static void addBanEntry(@NotNull final ProfileBanEntry entry) {
    Optional.ofNullable(entry.getKey()).ifPresent(key ->
      ProfileBanConfig.getInstance().saveAfterDo(config ->
        config.set(key.getUniqueId().toString(), entry.serialize())));
  }

  /**
   * obtains all the ban entries.
   *
   * @return all the ban entries.
   */
  @NotNull
  public static Set<BanEntry> getBanEntries() {
    return ProfileBanConfig.getInstance().getConfiguration().getKeys(false).stream()
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
    return ProfileBanConfig.getInstance().get(target)
      .filter(Map.class::isInstance)
      .map(o -> (Map<String, Object>) o)
      .map(ProfileBanEntry::new)
      .filter(entry -> !entry.hasExpired())
      .map(entry -> new ShirukaProfileBanEntry(profile, entry));
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  public static ProfileBanConfig getInstance() {
    return Objects.requireNonNull(ProfileBanConfig.instance);
  }

  /**
   * initiates the ip ban config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    Config.fromFile(file)
      .map(ProfileBanConfig::new)
      .ifPresent(config -> {
        config.save();
        ProfileBanConfig.instance = config;
      });
  }

  /**
   * checks if the given target is banned.
   *
   * @param target the target to check.
   *
   * @return {@code true} if the target is banned, {@code false} otherwise.
   */
  public static boolean isBanned(@NotNull final String target) {
    return ProfileBanConfig.getInstance().getConfiguration().contains(target);
  }

  /**
   * removes the given {@code target} from the section.
   *
   * @param target the target to remove.
   */
  public static void remove(@NotNull final String target) {
    ProfileBanConfig.getInstance().saveAfterDo(config -> config.remove(target));
  }
}
