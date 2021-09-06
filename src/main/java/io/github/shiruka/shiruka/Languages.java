package io.github.shiruka.shiruka;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents languages.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Languages {

  /**
   * lazy-init the instance.
   */
  @Nullable
  private static Languages instance;

  /**
   * the Shiru ka's language cache.
   */
  @Getter
  private final Map<String, String> shirukaCache = new Object2ObjectOpenHashMap<>();

  /**
   * Shiru ka's resource bundle.
   */
  @NotNull
  @Getter
  private final ResourceBundle shirukaResource;

  /**
   * the Vanilla's language cache.
   */
  @Getter
  private final Map<String, String> vanillaCache = new Object2ObjectOpenHashMap<>();

  /**
   * Vanilla's resource bundle.
   */
  @NotNull
  @Getter
  private final ResourceBundle vanillaResource;

  /**
   * obtains the language value.
   *
   * @param key the key to get.
   * @param params the params to get.
   *
   * @return language value.
   */
  @NotNull
  public static String shiruka(@NotNull final String key, @NotNull final Object... params) {
    final var languages = Languages.getInstance();
    final var value = languages.shirukaCache.computeIfAbsent(key, languages.shirukaResource::getString);
    if (params.length == 0) {
      return value;
    }
    return MessageFormat.format(value, params);
  }

  /**
   * obtains the language value.
   *
   * @param key the key to get.
   * @param params the params to get.
   *
   * @return language value.
   */
  @NotNull
  public static String vanilla(@NotNull final String key, @NotNull final Object... params) {
    final var languages = Languages.getInstance();
    final var value = languages.vanillaCache.computeIfAbsent(key, languages.vanillaResource::getString);
    if (params.length == 0) {
      return value;
    }
    return String.format(value, params);
  }

  /**
   * initiates the languages.
   *
   * @param shiruka the shiruka to initiate.
   * @param vanilla the vanilla to initiate.
   */
  static void init(@NotNull final ResourceBundle shiruka, @NotNull final ResourceBundle vanilla) {
    if (Languages.instance != null) {
      throw new IllegalStateException(Languages.shiruka("cannot-initiate-twice"));
    }
    Languages.instance = new Languages(shiruka, vanilla);
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  private static Languages getInstance() {
    return Objects.requireNonNull(Languages.instance, "not initiated");
  }
}
