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

import com.fasterxml.jackson.core.type.TypeReference;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.Shiruka;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.misc.JiraExceptionCatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains language operations.
 */
@Log4j2
public final class Languages {

  /**
   * the available languages.
   */
  static final Collection<String> AVAILABLE_LANGUAGES = new ObjectOpenHashSet<>();

  /**
   * the shiruka keys.
   */
  static final Collection<String> SHIRUKA_KEYS = new ObjectOpenHashSet<>();

  /**
   * the vanilla keys.
   */
  static final Collection<String> VANILLA_KEYS = new ObjectOpenHashSet<>();

  /**
   * the list type reference.
   */
  private static final TypeReference<List<String>> LIST_TYPE_REFERENCE = new TypeReference<>() {
  };

  /**
   * the shiruka variables.
   */
  private static final Map<String, Properties> SHIRUKA_VARIABLES = new Object2ObjectOpenHashMap<>();

  /**
   * the vanilla variables.
   */
  private static final Map<String, Properties> VANILLA_VARIABLES = new Object2ObjectOpenHashMap<>();

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
    if (Languages.addLoadedLanguage0(locale)) {
      Languages.loadVariables(locale);
      ServerConfig.save();
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
   * @param def the default locale comes from java arguments.
   *
   * @return the chosen server language.
   *
   * @throws IOException if something went wrong when reading the file.
   */
  @NotNull
  public static Locale startSequence(@Nullable final String def) throws IOException {
    Languages.clean();
    Languages.loadAvailableLanguages();
    Languages.loadKeys();
    final var locale = Languages.loadServerLanguage(def == null ? "_" : def);
    Languages.loadLoadedLanguages();
    return locale;
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
    if (split.length != 2) {
      return Locale.ROOT;
    }
    return new Locale(split[0], split[1].toUpperCase(Locale.ROOT));
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
   * adds the given language to the loaded languages.
   *
   * @param locale the locale to load.
   *
   * @return {@code true} if the language is loaded.
   */
  private static boolean addLoadedLanguage0(@NotNull final String locale) {
    return Languages.AVAILABLE_LANGUAGES.contains(locale) &&
      ServerConfig.addLoadedLanguage(locale);
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
    if (split.length != 2) {
      Languages.log.error("§cPlease write a valid language!");
      return Languages.choosingLanguageLoop();
    }
    final var upperChosen = Languages.secondUpper(chosenLanguage);
    if (!Languages.AVAILABLE_LANGUAGES.contains(upperChosen)) {
      Languages.log.error("§cPlease write a valid language!");
      return Languages.choosingLanguageLoop();
    }
    return Languages.toLocale(upperChosen);
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
    final var reader = new InputStreamReader(
      Languages.getResource("lang/languages.json"),
      StandardCharsets.UTF_8);
    final var languages = Shiruka.JSON_MAPPER.readValue(reader, Languages.LIST_TYPE_REFERENCE);
    Languages.AVAILABLE_LANGUAGES.addAll(languages);
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
   * loads the loaded languages of the servers.
   */
  private static void loadLoadedLanguages() {
    ServerConfig.loadedLanguages.forEach(Languages::loadVariables);
  }

  /**
   * loads the server language.
   *
   * @param def the default locale comes from java arguments.
   *
   * @return server language.
   */
  @NotNull
  private static Locale loadServerLanguage(@NotNull final String def) {
    final Locale serverLanguage;
    if (Languages.AVAILABLE_LANGUAGES.contains(Languages.secondUpper(def))) {
      final var defaultLocale = Languages.toLocale(def);
      if (defaultLocale.equals(Locale.ROOT)) {
        serverLanguage = ServerConfig.serverLanguage;
      } else {
        serverLanguage = defaultLocale;
        ServerConfig.setServerLanguage(serverLanguage);
        ServerConfig.save();
      }
    } else {
      serverLanguage = ServerConfig.serverLanguage;
    }
    if (!serverLanguage.equals(Locale.ROOT)) {
      if (Languages.addLoadedLanguage0(Languages.toString(serverLanguage))) {
        ServerConfig.save();
      }
      return serverLanguage;
    }
    Languages.AVAILABLE_LANGUAGES.forEach(s -> Languages.log.info("§7" + s));
    Languages.log.info("§aChoose one of the available languages");
    final var now = System.currentTimeMillis();
    final var locale = Languages.choosingLanguageLoop();
    ShirukaMain.START_TIME.set(ShirukaMain.START_TIME.get() + System.currentTimeMillis() - now);
    ServerConfig.setServerLanguage(locale);
    Languages.addLoadedLanguage0(Languages.toString(locale));
    ServerConfig.save();
    return locale;
  }

  /**
   * loads the given locale's variables from the file.
   *
   * @param locale the locale to load.
   */
  private static void loadVariables(@NotNull final String locale) {
    final var stream = new InputStreamReader(
      Languages.getResource(String.format("lang/shiruka/%s.properties", locale)),
      StandardCharsets.UTF_8);
    final var vanillaStream = new InputStreamReader(
      Languages.getResource(String.format("lang/vanilla/%s.properties", locale)),
      StandardCharsets.UTF_8);
    Optional.ofNullable(Languages.SHIRUKA_VARIABLES.get(locale)).ifPresent(properties ->
      JiraExceptionCatcher.run(() ->
        properties.load(stream)));
    Optional.ofNullable(Languages.VANILLA_VARIABLES.get(locale)).ifPresent(properties ->
      JiraExceptionCatcher.run(() ->
        properties.load(vanillaStream)));
  }

  /**
   * makes upper case the given {@code text}'s second part.
   *
   * @param text the text to make upper case.
   *
   * @return upper cased text.
   */
  @NotNull
  private static String secondUpper(@NotNull final String text) {
    final var split = text.split("_");
    if (split.length != 2) {
      return text;
    }
    return split[0] + "_" + split[1].toUpperCase(Locale.ROOT);
  }
}
