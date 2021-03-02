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
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.base.GameProfile;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents entry of game profiles.
 */
@RequiredArgsConstructor
public final class GameProfileEntry {

  /**
   * the count.
   */
  private static final AtomicLong COUNT = new AtomicLong();

  /**
   * the count.
   */
  @Getter
  private final long count = GameProfileEntry.COUNT.getAndIncrement();

  /**
   * the expires on.
   */
  @NotNull
  @Getter
  private final Date expiresOn;

  /**
   * the profile.
   */
  @NotNull
  @Getter
  private final GameProfile profile;

  /**
   * creates a new game profile entry instance from given {@code map}.
   *
   * @param map the map to create.
   *
   * @return a new game profile entry instance.
   */
  @NotNull
  public static Optional<GameProfileEntry> deserialize(@NotNull final Map<String, Object> map) {
    try {
      final var expiresOn = DateFormat.getInstance().parse((String) map.get("expires-on"));
      return GameProfile.deserialize(map)
        .map(profile -> new GameProfileEntry(expiresOn, profile));
    } catch (final ParseException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  /**
   * converts {@code this} to a {@link Map}.
   *
   * @return serialized entry.
   */
  @NotNull
  public Map<String, Object> serialize() {
    final var map = this.profile.serialize();
    map.put("expires-on", DateFormat.getInstance().format(this.expiresOn));
    return map;
  }
}
