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
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
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
  static final Collection<String> AVAILABLE_LANGUAGES = new HashSet<>();

  /**
   * the shiru ka keys.
   */
  static final Collection<String> SHIRU_KA_KEYS = new HashSet<>();

  /**
   * the shiruka variables.
   */
  static final Map<String, Properties> SHIRU_KA_VARIABLES = new ConcurrentHashMap<>();

  /**
   * the vanilla keys.
   */
  static final Collection<String> VANILLA_KEYS = new HashSet<>();

  /**
   * the vanilla variables.
   */
  static final Map<String, Properties> VANILLA_VARIABLES = new ConcurrentHashMap<>();

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
    Languages.clean();
    Languages.loadAvailableLanguages();
    Languages.loadLoadedLanguages();
    return Languages.loadServerLanguage();
  }

  /**
   * adds the given language to the loaded languages and loads its variables.
   *
   * @param locale the locale to load.
   */
  private static void addLoadedLanguage(@NotNull final String locale) {
    if (!Languages.AVAILABLE_LANGUAGES.contains(locale)) {
      return;
    }
    final var value = ServerConfig.LOADED_LANGUAGES.getValue()
      .orElse(new ArrayList<>());
    if (value.contains(locale)) {
      return;
    }
    value.add(locale);
    ServerConfig.LOADED_LANGUAGES.setValue(value);
    Languages.loadVariables(locale);
  }

  /**
   * starts the loop to get chosen language.
   *
   * @return chosen language.
   */
  @NotNull
  private static Locale choosingLanguageLoop() {
    final var scanner = new Scanner(System.in);
    final var chosenLanguage = scanner.nextLine().toLowerCase(Locale.ROOT);
    final var split = chosenLanguage.split("_");
    if (!Languages.AVAILABLE_LANGUAGES.contains(chosenLanguage) || split.length != 2) {
      Languages.LOGGER.error("§cPlease write a valid language!");
      return Languages.choosingLanguageLoop();
    }
    return new Locale(split[0], split[1]);
  }

  /**
   * cleans the caches.
   */
  private static void clean() {
    Languages.AVAILABLE_LANGUAGES.clear();
    Languages.SHIRU_KA_KEYS.clear();
    Languages.VANILLA_KEYS.clear();
    Languages.SHIRU_KA_VARIABLES.clear();
    Languages.VANILLA_VARIABLES.clear();
  }

  /**
   * obtains the resource as input stream from the given {@code resourcePath}.
   *
   * @param resourcePath the resource path to get.
   *
   * @return obtained input stream.
   */
  @NotNull
  private static InputStream getResource(@NotNull final String resourcePath) {
    final var classLoader = Objects.requireNonNull(Thread.currentThread().getContextClassLoader(),
      "class loader");
    return Objects.requireNonNull(classLoader.getResourceAsStream(resourcePath), "resource");
  }

  /**
   * loads the available languages.
   *
   * @throws IOException if something went wrong when reading the file.
   */
  private static void loadAvailableLanguages() throws IOException {
    final var resource = Languages.getResource("lang/languages.json");
    final var stream = new InputStreamReader(resource, StandardCharsets.UTF_8);
    final var parse = Json.parse(stream);
    final var languages = parse.asArray();
    Languages.AVAILABLE_LANGUAGES.addAll(languages.values().stream()
      .map(JsonValue::asString)
      .collect(Collectors.toUnmodifiableSet()));
    Languages.AVAILABLE_LANGUAGES.forEach(s -> {
      Languages.SHIRU_KA_VARIABLES.put(s, new Properties());
      Languages.VANILLA_VARIABLES.put(s, new Properties());
    });
  }

  /**
   * loads the loaded languages of the servers
   */
  private static void loadLoadedLanguages() {
    ServerConfig.LOADED_LANGUAGES.getValue().ifPresent(strings ->
      strings.forEach(Languages::loadVariables));
  }

  /**
   * loads the server language.
   *
   * @return server language.
   */
  @NotNull
  private static Locale loadServerLanguage() {
    final var serverLanguage = ServerConfig.SERVER_LANGUAGE.getValue();
    if (serverLanguage.isPresent() && serverLanguage.get() == Locale.ROOT) {
      return serverLanguage.get();
    }
    Languages.LOGGER.info("§aChoose one of the available languages");
    final var languageFormat = "§7{}";
    Languages.AVAILABLE_LANGUAGES.forEach(s -> Languages.LOGGER.info(languageFormat, s));
    final var serverLocale = Languages.choosingLanguageLoop();
    ServerConfig.SERVER_LANGUAGE.setValue(serverLocale);
    ServerConfig.get().ifPresent(Config::save);
    return serverLocale;
  }

  /**
   * loads the given locale's variables from the file.
   *
   * @param locale the locale to load.
   */
  private static void loadVariables(@NotNull final String locale) {
    Optional.ofNullable(Languages.SHIRU_KA_VARIABLES.get(locale)).ifPresent(properties -> {
      final var stream = new InputStreamReader(Languages.getResource("lang/shiruka/" + locale + ".lang"),
        StandardCharsets.UTF_8);
      JiraExceptionCatcher.run(() -> properties.load(stream));
    });
    Optional.ofNullable(Languages.VANILLA_VARIABLES.get(locale)).ifPresent(properties -> {
      final var stream = new InputStreamReader(Languages.getResource("lang/vanilla/" + locale + ".lang"),
        StandardCharsets.UTF_8);
      JiraExceptionCatcher.run(() -> properties.load(stream));
    });
  }
}
