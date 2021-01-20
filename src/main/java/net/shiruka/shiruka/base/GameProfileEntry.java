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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import net.shiruka.api.base.GameProfile;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents entry of game profiles.
 */
public final class GameProfileEntry {

  /**
   * the count.
   */
  private static final AtomicLong COUNT = new AtomicLong();

  /**
   * the count.
   */
  private final long count;

  /**
   * the expires on.
   */
  @NotNull
  private final Date expiresOn;

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * ctor.
   *
   * @param expiresOn the expires on.
   * @param profile the profile.
   */
  private GameProfileEntry(@NotNull final Date expiresOn, @NotNull final GameProfile profile) {
    this.count = GameProfileEntry.COUNT.getAndIncrement();
    this.expiresOn = (Date) expiresOn.clone();
    this.profile = profile;
  }

  /**
   * creates a new game profile entry instance from given {@code map}.
   *
   * @param map the map to create.
   *
   * @return a new game profile entry instance.
   */
  @NotNull
  public static Optional<GameProfileEntry> fromMap(@NotNull final Map<String, Object> map) {
    try {
      //noinspection unchecked
      final var profile = (Map<String, Object>) map.get("profile");
      final var name = (String) profile.get("name");
      final var uniqueId = (String) profile.get("unique-id");
      final var xboxId = (String) profile.get("xbox-id");
      final var gameProfile = new GameProfile(() -> name, UUID.fromString(uniqueId), xboxId);
      final var expiresOn = DateFormat.getInstance().parse((String) map.get("expires-on"));
      return Optional.of(new GameProfileEntry(expiresOn, gameProfile));
    } catch (final ParseException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /**
   * obtains the count.
   *
   * @return count.
   */
  public long getCount() {
    return this.count;
  }

  /**
   * obtains the expires on.
   *
   * @return expires on.
   */
  @NotNull
  public Date getExpiresOn() {
    return (Date) this.expiresOn.clone();
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
   * converts {@code this} to a {@link Map}.
   *
   * @return serialized entry.
   */
  @NotNull
  public Map<String, Object> toMap() {
    final var map = new HashMap<String, Object>();
    final var profile = new HashMap<String, Object>();
    profile.put("name", this.profile.getName().asString());
    profile.put("unique-id", this.profile.getUniqueId().toString());
    profile.put("xbox-id", this.profile.getXboxId());
    map.put("profile", profile);
    map.put("expires-on", DateFormat.getInstance().format(this.expiresOn));
    return map;
  }
}
