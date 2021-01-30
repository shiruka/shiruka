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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class representing base ban entries.
 *
 * @param <K> type of the key.
 */
public abstract class BaseBanEntry<K> {

  /**
   * the date format.
   */
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

  /**
   * the created.
   */
  @NotNull
  private final Date created;

  /**
   * the expires.
   */
  @Nullable
  private final Date expires;

  /**
   * the key.
   */
  @Nullable
  private final K key;

  /**
   * the reason.
   */
  @NotNull
  private final String reason;

  /**
   * the source.
   */
  @NotNull
  private final String source;

  /**
   * ctor.
   *
   * @param key the key.
   * @param map the map.
   */
  protected BaseBanEntry(@Nullable final K key, @NotNull final Map<String, Object> map) {
    this.key = BaseBanEntry.checkExpiry(key, map);
    final var createdOrNot = map.get("created");
    final var expiresOrNot = map.get("expires");
    Date created;
    try {
      created = createdOrNot == null
        ? new Date()
        : BaseBanEntry.DATE_FORMAT.parse(createdOrNot.toString());
    } catch (final ParseException e) {
      created = new Date();
    }
    Date expires;
    try {
      expires = expiresOrNot == null
        ? new Date()
        : BaseBanEntry.DATE_FORMAT.parse(expiresOrNot.toString());
    } catch (final ParseException e) {
      expires = null;
    }
    this.created = created;
    this.expires = expires;
    this.source = map.getOrDefault("source", "Unknown").toString();
    this.reason = map.getOrDefault("reason", "Banned by an operator.").toString();
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
  protected BaseBanEntry(@NotNull final K key, @Nullable final Date created, @Nullable final String source,
                         @Nullable final Date expires, @Nullable final String reason) {
    this.key = key;
    this.created = created == null ? new Date() : (Date) created.clone();
    this.source = source == null ? "(Unknown)" : source;
    this.expires = expires == null ? null : (Date) expires.clone();
    this.reason = reason == null ? "Banned by an operator." : reason;
  }

  /**
   * checks expiry.
   *
   * @param key the key to check.
   * @param map the map to check.
   * @param <K> type of the key.
   *
   * @return {@code true} if the key is valid.
   */
  @Nullable
  private static <K> K checkExpiry(@Nullable final K key, @NotNull final Map<String, Object> map) {
    Date expires = null;
    try {
      expires = map.containsKey("expires")
        ? BaseBanEntry.DATE_FORMAT.parse(map.get("expires").toString())
        : null;
    } catch (final ParseException ignored) {
    }
    if (expires == null || expires.after(new Date())) {
      return key;
    }
    return null;
  }

  /**
   * obtains the created.
   *
   * @return created.
   */
  @NotNull
  public final Date getCreated() {
    return (Date) this.created.clone();
  }

  /**
   * obtains the expires.
   *
   * @return expires.
   */
  @Nullable
  public final Date getExpires() {
    return this.expires == null
      ? null
      : (Date) this.expires.clone();
  }

  /**
   * obtains the key.
   *
   * @return key.
   */
  @Nullable
  public final K getKey() {
    return this.key;
  }

  /**
   * obtains the reason.
   *
   * @return reason.
   */
  @NotNull
  public final String getReason() {
    return this.reason;
  }

  /**
   * obtains the source.
   *
   * @return source.
   */
  @NotNull
  public final String getSource() {
    return this.source;
  }

  /**
   * checks if the ban is expired.
   *
   * @return {@code true} if the ban is expired.
   */
  public final boolean hasExpired() {
    return this.expires != null &&
      this.expires.before(new Date());
  }

  @NotNull
  public Map<String, Object> serialize() {
    final var map = new HashMap<String, Object>();
    map.put("created", BaseBanEntry.DATE_FORMAT.format(this.created));
    map.put("source", this.source);
    map.put("expires", this.expires == null ? "forever" : BaseBanEntry.DATE_FORMAT.format(this.expires));
    map.put("reason", this.reason);
    return map;
  }
}
