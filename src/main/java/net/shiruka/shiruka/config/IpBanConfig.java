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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.shiruka.api.base.BanEntry;
import net.shiruka.shiruka.ban.IpBanEntry;
import net.shiruka.shiruka.ban.ShirukaIpBanEntry;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents ip ban config.
 */
public final class IpBanConfig implements ConfigHolder {

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
  public static void addBanEntry(@NotNull final IpBanEntry entry) {
    Optional.ofNullable(entry.getKey()).ifPresent(key -> {
      IpBanConfig.configuration.set(key, entry.serialize());
      IpBanConfig.loader.save();
    });
  }

  /**
   * obtains all the ban entries.
   *
   * @return all the ban entries.
   */
  @NotNull
  public static Set<BanEntry> getBanEntries() {
    return IpBanConfig.configuration.getKeys(false).stream()
      .flatMap(key -> IpBanConfig.getBanEntry(key).stream())
      .collect(Collectors.toSet());
  }

  /**
   * gets the ban entry from the {@code target}.
   *
   * @param target the target to get.
   *
   * @return ip ban entry instance.
   */
  @NotNull
  public static Optional<BanEntry> getBanEntry(@NotNull final String target) {
    //noinspection unchecked
    return Optional.ofNullable(IpBanConfig.configuration.get(target))
      .filter(Map.class::isInstance)
      .map(o -> (Map<String, Object>) o)
      .map(IpBanEntry::new)
      .filter(entry -> !entry.hasExpired())
      .map(entry -> new ShirukaIpBanEntry(target, entry));
  }

  /**
   * checks if the given target is banned.
   *
   * @param target the target to check.
   *
   * @return {@code true} if the target is banned, {@code false} otherwise.
   */
  public static boolean isBanned(@NotNull final InetSocketAddress target) {
    return IpBanConfig.configuration.contains(IpBanConfig.convert(target));
  }

  /**
   * checks if the given target is banned.
   *
   * @param target the target to check.
   *
   * @return {@code true} if the target is banned, {@code false} otherwise.
   */
  public static boolean isBanned(@NotNull final String target) {
    return IpBanConfig.isBanned(InetSocketAddress.createUnresolved(target, 0));
  }

  /**
   * removes the given {@code target} from the section.
   *
   * @param target the target to remove.
   */
  public static void remove(@NotNull final String target) {
    IpBanConfig.configuration.set(target, null);
    IpBanConfig.loader.save();
  }

  /**
   * converts the given {@code address} to string.
   *
   * @param address the address to convert.
   *
   * @return converted address string.
   */
  @NotNull
  private static String convert(@NotNull final SocketAddress address) {
    var var1 = address.toString();
    if (var1.contains("/")) {
      var1 = var1.substring(var1.indexOf(47) + 1);
    }
    if (var1.contains(":")) {
      var1 = var1.substring(0, var1.indexOf(58));
    }
    return var1;
  }

  /**
   * the ignored class.
   */
  public void ignored() {
    // ignored.
  }
}
