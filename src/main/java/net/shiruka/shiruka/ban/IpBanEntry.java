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

package net.shiruka.shiruka.ban;

import java.util.Date;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents ip ban entries.
 */
public final class IpBanEntry extends BaseBanEntry<String> {

  /**
   * ctor.
   *
   * @param map the map.
   */
  public IpBanEntry(@NotNull final Map<String, Object> map) {
    super(IpBanEntry.getKey(map), map);
  }

  /**
   * ctor.
   *
   * @param key the key.
   * @param created the created.
   * @param source the source.
   * @param expires the expires.
   * @param reason the reason.
   */
  public IpBanEntry(@NotNull final String key, @Nullable final Date created, @Nullable final String source,
                    @Nullable final Date expires, @Nullable final String reason) {
    super(key, created, source, expires, reason);
  }

  /**
   * gets the key from the given {@code map.}
   *
   * @param map the map to get.
   *
   * @return the key.
   */
  @Nullable
  private static String getKey(@NotNull final Map<String, Object> map) {
    return map.containsKey("ip")
      ? map.get("ip").toString()
      : null;
  }

  @NotNull
  @Override
  public Map<String, Object> serialize() {
    final var map = super.serialize();
    if (this.getKey() != null) {
      map.put("ip", this.getKey());
    }
    return map;
  }
}
