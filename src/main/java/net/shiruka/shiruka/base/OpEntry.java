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

package net.shiruka.shiruka.base;

import java.util.Map;
import java.util.Optional;
import net.shiruka.api.base.GameProfile;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents op entries.
 */
public final class OpEntry {

  /**
   * the bypasses player limit.
   */
  private final boolean bypassesPlayerLimit;

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * ctor.
   *
   * @param profile the profile.
   * @param bypassesPlayerLimit the bypasses player limit.
   */
  public OpEntry(@NotNull final GameProfile profile, final boolean bypassesPlayerLimit) {
    this.profile = profile;
    this.bypassesPlayerLimit = bypassesPlayerLimit;
  }

  /**
   * creates a new op entry instance from given {@code map}.
   *
   * @param map the map to create.
   *
   * @return a new op entry instance.
   */
  @NotNull
  public static Optional<OpEntry> deserialize(@NotNull final Map<String, Object> map) {
    try {
      final var bypassesPlayerLimit = (boolean) map.get("bypasses-player-limit");
      return GameProfile.deserialize(map)
        .map(profile -> new OpEntry(profile, bypassesPlayerLimit));
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /**
   * obtains the profile.
   *
   * @return profile.
   */
  @NotNull
  public GameProfile getProfile() {
    return this.profile;
  }

  /**
   * obtains the bypasses player limit.
   *
   * @return bypasses player limit.
   */
  public boolean canBypassesPlayerLimit() {
    return this.bypassesPlayerLimit;
  }

  /**
   * converts {@code this} to a {@link Map}.
   *
   * @return serialized entry.
   */
  @NotNull
  public Map<String, Object> serialize() {
    final var map = this.profile.serialize();
    map.put("bypasses-player-limit", this.bypassesPlayerLimit);
    return map;
  }
}
