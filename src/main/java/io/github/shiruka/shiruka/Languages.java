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
   * the instance.
   */
  @Nullable
  private static Languages instance;

  /**
   * the language cache.
   */
  @Getter
  private final Map<String, String> cache = new Object2ObjectOpenHashMap<>();

  /**
   * lazy-init resource bundle.
   */
  @NotNull
  @Getter
  private final ResourceBundle resource;

  /**
   * obtains the language value.
   *
   * @param key the key to get.
   * @param params the params to get.
   *
   * @return language value.
   */
  @NotNull
  public static String getLanguageValue(@NotNull final String key, @NotNull final Object... params) {
    final var languages = Languages.getInstance();
    final var value = languages.cache.computeIfAbsent(key, languages.resource::getString);
    if (params.length == 0) {
      return value;
    }
    return MessageFormat.format(value, params);
  }

  /**
   * initiates the languages.
   *
   * @param bundle the bundle to initiate.
   */
  static void init(@NotNull final ResourceBundle bundle) {
    if (Languages.instance != null) {
      throw new IllegalStateException(Languages.getLanguageValue("cannot-initiate-twice"));
    }
    Languages.instance = new Languages(bundle);
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
