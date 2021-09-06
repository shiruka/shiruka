package io.github.shiruka.shiruka;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.infumialib.transformer.TransformedObject;
import tr.com.infumia.infumialib.transformer.TransformerPool;
import tr.com.infumia.infumialib.transformer.annotations.Comment;
import tr.com.infumia.infumialib.transformer.annotations.Exclude;
import tr.com.infumia.infumialib.transformer.resolvers.Snakeyaml;

/**
 * a class that represents Shiru ka's config file.
 */
final class Config extends TransformedObject {

  /**
   * the Server's language.
   */
  @Comment("Defines the server'a language.")
  public static Locale lang = Locale.US;

  /**
   * the instance.
   */
  @Nullable
  @Exclude
  private static TransformedObject instance;

  /**
   * sets the server's language.
   *
   * @param lang the lang to set.
   */
  static void language(@NotNull final Locale lang) {
    Config.instance().set("lang", lang);
  }

  /**
   * loads the config.
   *
   * @param file the file to load.
   */
  static void loadConfig(@NotNull final Path file) {
    Config.instance = TransformerPool.create(new Config())
      .withFile(file)
      .withResolver(new Snakeyaml())
      .initiate();
  }

  /**
   * obtains Shiru ka's language bundle.
   *
   * @return Shiru ka's language bundle.
   */
  @NotNull
  static ResourceBundle shirukaLanguageBundle() {
    return ResourceBundle.getBundle("language.shiruka.Shiruka", Config.lang);
  }

  /**
   * obtains Vanilla's language bundle.
   *
   * @return Vanilla's language bundle.
   */
  @NotNull
  static ResourceBundle vanillaLanguageBundle() {
    return ResourceBundle.getBundle("language.vanilla.Vanilla", Config.lang);
  }

  /**
   * obtains the config instance.
   *
   * @return config instance.
   */
  @NotNull
  private static TransformedObject instance() {
    return Objects.requireNonNull(Config.instance, "Use #loadConfig(Path) first!");
  }
}
