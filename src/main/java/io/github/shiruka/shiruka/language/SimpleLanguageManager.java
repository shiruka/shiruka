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

package io.github.shiruka.shiruka.language;

import io.github.shiruka.api.language.LanguageManager;
import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link LanguageManager}.
 */
public final class SimpleLanguageManager implements LanguageManager {

  /**
   * the server language.
   */
  @NotNull
  private final Locale serverLanguage;

  /**
   * ctor.
   *
   * @param serverLanguage the server language.
   */
  public SimpleLanguageManager(@NotNull final Locale serverLanguage) {
    this.serverLanguage = serverLanguage;
  }

  @Override
  public void check(@NotNull final String key) {
  }

  @NotNull
  @Override
  public Optional<Locale> getLanguage(@NotNull final String code) {
    return Optional.empty();
  }

  @NotNull
  @Override
  public Locale getServerLanguage() {
    return null;
  }

  @NotNull
  @Override
  public String translate(@NotNull final Locale locale, @NotNull final String key, final @NotNull Object... params) {
    return null;
  }
}
