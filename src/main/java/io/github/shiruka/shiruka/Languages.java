package io.github.shiruka.shiruka;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents languages.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Languages {

  /**
   * the Shiru ka's language cache.
   */
  @Getter
  private static final Map<String, String> SHIRUKA_CACHE = new Object2ObjectOpenHashMap<>();

  /**
   * the Vanilla's language cache.
   */
  @Getter
  private static final Map<String, String> VANILLA_CACHE = new Object2ObjectOpenHashMap<>();

  /**
   * Shiru ka's resource bundle.
   */
  @Nullable
  @Getter
  private static ResourceBundle shiruka;

  /**
   * Vanilla's resource bundle.
   */
  @Nullable
  @Getter
  private static ResourceBundle vanilla;

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
    final var value = Languages.SHIRUKA_CACHE.computeIfAbsent(key, Languages.shiruka()::getString);
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
    final var value = Languages.VANILLA_CACHE.computeIfAbsent(key, Languages.vanilla()::getString);
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
    Languages.shiruka = shiruka;
    Languages.vanilla = vanilla;
  }

  /**
   * obtains the Shiru ka bundle.
   *
   * @return Shiru ka bundle.
   */
  @NotNull
  private static ResourceBundle shiruka() {
    return Objects.requireNonNull(Languages.shiruka, "shiruka");
  }

  /**
   * obtains the Vanilla bundle.
   *
   * @return Vanilla bundle.
   */
  @NotNull
  private static ResourceBundle vanilla() {
    return Objects.requireNonNull(Languages.vanilla, "vanilla");
  }
}
