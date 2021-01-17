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

package io.github.shiruka.shiruka.config.paths;

import io.github.shiruka.api.config.ConfigPath;
import io.github.shiruka.api.config.path.advanced.ApMapList;
import io.github.shiruka.shiruka.config.UserCacheConfig;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents game profile entries {@link ConfigPath} implementation.
 */
public final class ApGameProfileEntries extends ApMapList<UserCacheConfig.GameProfileEntry> {

  public ApGameProfileEntries(@NotNull final String path, @Nullable final List<UserCacheConfig.GameProfileEntry> def) {
    //noinspection unchecked
    super(path, def,
      maps -> Optional.of(maps.stream()
        .map(map -> (Map<String, Object>) map)
        .map(UserCacheConfig.GameProfileEntry::fromMap)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList())),
      entries -> Optional.of(entries.stream()
        .map(UserCacheConfig.GameProfileEntry::toMap)
        .collect(Collectors.toList())));
  }
}
