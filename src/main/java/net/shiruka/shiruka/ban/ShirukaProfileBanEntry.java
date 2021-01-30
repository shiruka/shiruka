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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import net.shiruka.api.base.BanEntry;
import net.shiruka.api.base.GameProfile;
import net.shiruka.shiruka.config.ProfileBanConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShirukaProfileBanEntry implements BanEntry {

  /**
   * the zero calendar.
   */
  private static final long ZERO_CALENDAR =
    new GregorianCalendar(0, Calendar.JANUARY, 0, 0, 0, 0).getTimeInMillis();

  /**
   * the profile.
   */
  @NotNull
  private final GameProfile profile;

  /**
   * the created.
   */
  @NotNull
  private Date created;

  /**
   * the expiration.
   */
  @Nullable
  private Date expiration;

  /**
   * the reason.
   */
  @NotNull
  private String reason;

  /**
   * the source.
   */
  @NotNull
  private String source;

  /**
   * ctor.
   *
   * @param profile the profile.
   * @param entry the entry.
   */
  public ShirukaProfileBanEntry(@NotNull final GameProfile profile, @NotNull final ProfileBanEntry entry) {
    this.profile = profile;
    this.created = new Date(entry.getCreated().getTime());
    this.source = entry.getSource();
    this.expiration = entry.getExpires() != null ? new Date(entry.getExpires().getTime()) : null;
    this.reason = entry.getReason();
  }

  @NotNull
  @Override
  public Date getCreated() {
    return (Date) this.created.clone();
  }

  @Override
  public void setCreated(@NotNull final Date created) {
    this.created = (Date) created.clone();
  }

  @NotNull
  @Override
  public Optional<Date> getExpiration() {
    return Optional.ofNullable(this.expiration);
  }

  @Override
  public void setExpiration(@Nullable final Date expiration) {
    if (expiration == null) {
      this.expiration = null;
    } else if (expiration.getTime() == ShirukaProfileBanEntry.ZERO_CALENDAR) {
      this.expiration = null;
    } else {
      this.expiration = (Date) expiration.clone();
    }
  }

  @NotNull
  @Override
  public String getReason() {
    return this.reason;
  }

  @Override
  public void setReason(@NotNull final String reason) {
    this.reason = reason;
  }

  @NotNull
  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public void setSource(@NotNull final String source) {
    this.source = source;
  }

  @NotNull
  @Override
  public String getTarget() {
    return this.profile.getName().asString();
  }

  @Override
  public void save() {
    final var entry = new ProfileBanEntry(this.profile, this.created, this.source, this.expiration, this.reason);
    ProfileBanConfig.addBanEntry(entry);
  }
}
