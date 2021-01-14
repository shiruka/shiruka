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

import com.google.common.base.Preconditions;
import io.github.shiruka.api.language.LanguageManager;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
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
    if (key.startsWith("shiruka.")) {
      Preconditions.checkArgument(Languages.SHIRU_KA_KEYS.contains(key),
        String.format("The key called %s is not found!", key));
    } else {
      Preconditions.checkArgument(Languages.VANILLA_KEYS.contains(key),
        String.format("The key called %s is not found!", key));
    }
  }

  @NotNull
  @Override
  public Optional<Locale> getLanguage(@NotNull final String code) {
    if (!Languages.AVAILABLE_LANGUAGES.contains(code)) {
      return Optional.empty();
    }
    final var split = code.split("_");
    final var locale = new Locale(split[0], split[1]);
    return Optional.of(locale);
  }

  @NotNull
  @Override
  public Locale getServerLanguage() {
    return this.serverLanguage;
  }

  @NotNull
  @Override
  public String translate(@NotNull final Locale locale, @NotNull final String key, @NotNull final Object... params) {
    this.check(key);
    final var code = locale.getLanguage() + "_" + locale.getCountry();
    final Properties properties;
    if (key.startsWith("shiruka.")) {
      properties = Objects.requireNonNull(Languages.SHIRU_KA_VARIABLES.get(code), "the language not found!");
    } else {
      properties = Objects.requireNonNull(Languages.VANILLA_VARIABLES.get(code), "the language not found!");
    }
    return MessageFormat.format(properties.getProperty(key), params);
  }
}
