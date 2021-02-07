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

package net.shiruka.shiruka.language;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
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
   * the shiruka keys.
   */
  static final Collection<String> SHIRUKA_KEYS = new HashSet<>();

  /**
   * the vanilla keys.
   */
  static final Collection<String> VANILLA_KEYS = new HashSet<>();

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("Languages");

  /**
   * the shiruka variables.
   */
  private static final Map<String, Properties> SHIRUKA_VARIABLES = new ConcurrentHashMap<>();

  /**
   * the vanilla variables.
   */
  private static final Map<String, Properties> VANILLA_VARIABLES = new ConcurrentHashMap<>();

  /**
   * ctor.
   */
  private Languages() {
  }

  /**
   * adds the given language to the loaded languages and loads its variables.
   *
   * @param locale the locale to load.
   */
  public static void addLoadedLanguage(@NotNull final String locale) {
    if (Languages.setLoadedLanguage(locale)) {
      Languages.loadVariables(locale);
      ServerConfig.getInstance().save();
    }
  }

  /**
   * obtains the shiruka variables from the given {@code language}.
   *
   * @param language the language to get.
   *
   * @return shiruka variables.
   */
  @NotNull
  public static Properties getShirukaVariables(@NotNull final Locale language) {
    return Objects.requireNonNullElse(Languages.SHIRUKA_VARIABLES.get(Languages.toString(language)),
      Languages.SHIRUKA_VARIABLES.get("en_US"));
  }

  /**
   * obtains the vanilla variables from the given {@code language}.
   *
   * @param language the language to get.
   *
   * @return vanilla variables.
   */
  @NotNull
  public static Properties getVanillaVariables(@NotNull final Locale language) {
    return Objects.requireNonNullElse(Languages.VANILLA_VARIABLES.get(Languages.toString(language)),
      Languages.VANILLA_VARIABLES.get("en_US"));
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
    Languages.loadKeys();
    final var serverLanguage = Languages.loadServerLanguage();
    Languages.loadLoadedLanguages();
    return serverLanguage;
  }

  /**
   * converts the given {@code locale} to {@link Locale} instance.
   *
   * @param locale the locale to convert.
   *
   * @return {@link String} as {@link Locale}.
   */
  @NotNull
  public static Locale toLocale(@NotNull final String locale) {
    final var split = locale.split("_");
    return new Locale(split[0], split[1]);
  }

  /**
   * converts the given {@code locale} to {@link String} instance.
   *
   * @param locale the locale to convert.
   *
   * @return {@link Locale} as {@link String}.
   */
  @NotNull
  public static String toString(@NotNull final Locale locale) {
    return locale.getLanguage() + "_" + locale.getCountry();
  }

  /**
   * starts the loop to get chosen language.
   *
   * @return chosen language.
   */
  @NotNull
  private static Locale choosingLanguageLoop() {
    final var scanner = new Scanner(System.in);
    final var chosenLanguage = scanner.nextLine();
    final var split = chosenLanguage.split("_");
    if (!Languages.AVAILABLE_LANGUAGES.contains(chosenLanguage) || split.length != 2) {
      Languages.LOGGER.error("§cPlease write a valid language!");
      return Languages.choosingLanguageLoop();
    }
    return Languages.toLocale(chosenLanguage);
  }

  /**
   * cleans the caches.
   */
  private static void clean() {
    Languages.AVAILABLE_LANGUAGES.clear();
    Languages.SHIRUKA_KEYS.clear();
    Languages.VANILLA_KEYS.clear();
    Languages.SHIRUKA_VARIABLES.clear();
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
    return Objects.requireNonNull(classLoader.getResourceAsStream(resourcePath),
      String.format("The resource in %s not found", resourcePath));
  }

  /**
   * loads the available languages and keys.
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
      Languages.SHIRUKA_VARIABLES.put(s, new Properties());
      Languages.VANILLA_VARIABLES.put(s, new Properties());
    });
  }

  /**
   * loads {@link #SHIRUKA_KEYS} and {@link #VANILLA_KEYS}.
   */
  private static void loadKeys() throws IOException {
    final var shirukaResource = new InputStreamReader(Languages.getResource("lang/shiruka/en_US.properties"),
      StandardCharsets.UTF_8);
    final var shirukaKeys = new Properties();
    shirukaKeys.load(shirukaResource);
    shirukaKeys.keySet().forEach(o ->
      Languages.SHIRUKA_KEYS.add((String) o));
    final var vanillaResource = new InputStreamReader(Languages.getResource("lang/vanilla/en_US.properties"),
      StandardCharsets.UTF_8);
    final var vanillaKeys = new Properties();
    vanillaKeys.load(vanillaResource);
    vanillaKeys.keySet().forEach(o ->
      Languages.VANILLA_KEYS.add((String) o));
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
    if (serverLanguage.isPresent() && serverLanguage.get() != Locale.ROOT) {
      final var locale = serverLanguage.get();
      if (Languages.setLoadedLanguage(Languages.toString(locale))) {
        ServerConfig.getInstance().save();
      }
      return locale;
    }
    Languages.LOGGER.info("§aChoose one of the available languages");
    final var languageFormat = "§7{}";
    Languages.AVAILABLE_LANGUAGES.forEach(s -> Languages.LOGGER.info(languageFormat, s));
    final var serverLocale = Languages.choosingLanguageLoop();
    ServerConfig.SERVER_LANGUAGE.setValue(serverLocale);
    Languages.setLoadedLanguage(Languages.toString(serverLocale));
    ServerConfig.getInstance().save();
    return serverLocale;
  }

  /**
   * loads the given locale's variables from the file.
   *
   * @param locale the locale to load.
   */
  private static void loadVariables(@NotNull final String locale) {
    final var shirukaFile = String.format("lang/shiruka/%s.properties", locale);
    final var vanillaFile = String.format("lang/vanilla/%s.properties", locale);
    Optional.ofNullable(Languages.SHIRUKA_VARIABLES.get(locale)).ifPresent(properties ->
      JiraExceptionCatcher.run(() ->
      properties.load(new InputStreamReader(Languages.getResource(shirukaFile), StandardCharsets.UTF_8))));
    Optional.ofNullable(Languages.VANILLA_VARIABLES.get(locale)).ifPresent(properties ->
      JiraExceptionCatcher.run(() ->
        properties.load(new InputStreamReader(Languages.getResource(vanillaFile), StandardCharsets.UTF_8))));
  }

  /**
   * adds the given language to the loaded languages.
   *
   * @param locale the locale to load.
   *
   * @return {@code true} if the language is loaded.
   */
  private static boolean setLoadedLanguage(@NotNull final String locale) {
    if (!Languages.AVAILABLE_LANGUAGES.contains(locale)) {
      return false;
    }
    final var value = ServerConfig.LOADED_LANGUAGES.getValue()
      .orElse(new ArrayList<>());
    if (value.contains(locale)) {
      return false;
    }
    value.add(locale);
    ServerConfig.LOADED_LANGUAGES.setValue(value);
    return true;
  }
}
