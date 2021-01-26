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
import java.util.Optional;
import java.util.Set;
import net.shiruka.api.base.BanEntry;
import net.shiruka.api.base.BanList;
import net.shiruka.api.text.Text;
import net.shiruka.shiruka.config.IpBanConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an implementation of {@link BanList} for IP bans.
 */
public final class IpBanList implements BanList {

  @NotNull
  @Override
  public Optional<BanEntry> addBan(@NotNull final String target, @Nullable final Text reason,
                                   @Nullable final Date expires, @Nullable final String source) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Set<BanEntry> getBanEntries() {
    return null;
  }

  @NotNull
  @Override
  public Optional<BanEntry> getBanEntry(@NotNull final String target) {
    return IpBanConfig.getBanEntry(target);
  }

  @Override
  public boolean isBanned(@NotNull final String target) {
    return IpBanConfig.isBanned(target);
  }

  @Override
  public void pardon(@NotNull final String target) {
    IpBanConfig.remove(target);
  }
}
