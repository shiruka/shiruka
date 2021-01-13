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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import io.github.shiruka.api.config.Config;
import io.github.shiruka.shiruka.config.ServerConfig;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains language operations.
 */
public final class Languages {

  /**
   * the available languages.
   */
  public static final Set<String> AVAILABLE_LANGUAGES = new HashSet<>();

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Languages");

  /**
   * ctor.
   */
  private Languages() {
  }

  /**
   * starts the choosing language sequence if it has never chosen before.
   *
   * @return the chosen server language.
   *
   * @throws IOException if something went wrong when reading the file.
   */
  @NotNull
  public static Locale startSequence() throws IOException {
    final var classLoader = Objects.requireNonNull(Thread.currentThread().getContextClassLoader(),
      "class loader");
    final var resource = Objects.requireNonNull(classLoader.getResourceAsStream("lang/languages.json"),
      "resource");
    final var stream = new InputStreamReader(resource);
    final var parse = Json.parse(stream);
    final var languages = parse.asArray();
    Languages.AVAILABLE_LANGUAGES.addAll(languages.values().stream()
      .map(JsonValue::asString)
      .collect(Collectors.toUnmodifiableSet()));
    final var serverLanguage = ServerConfig.SERVER_LANGUAGE.getValue();
    final Locale serverLocale;
    if (serverLanguage.isEmpty() || serverLanguage.get() == Locale.ROOT) {
      Languages.LOGGER.info("§aChoose one of the available languages");
      final var languageFormat = "§7{}";
      Languages.AVAILABLE_LANGUAGES.forEach(s -> Languages.LOGGER.info(languageFormat, s));
      serverLocale = Languages.loop();
      ServerConfig.SERVER_LANGUAGE.setValue(serverLocale);
      ServerConfig.get().ifPresent(Config::save);
    } else {
      serverLocale = serverLanguage.get();
    }
    return serverLocale;
  }

  /**
   * starts the loop to get chosen language.
   *
   * @return chosen language.
   */
  @NotNull
  private static Locale loop() {
    final var scanner = new Scanner(System.in);
    final var chosenLanguage = scanner.nextLine();
    final var split = chosenLanguage.split("_");
    if (split.length != 2) {
      Languages.LOGGER.error("§cPlease write a valid language!");
      return Languages.loop();
    }
    return new Locale(split[0], split[1]);
  }
}
