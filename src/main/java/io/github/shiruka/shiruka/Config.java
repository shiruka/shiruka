package io.github.shiruka.shiruka;

import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.infumialib.transformer.TransformedObject;
import tr.com.infumia.infumialib.transformer.TransformerPool;
import tr.com.infumia.infumialib.transformer.annotations.Comment;
import tr.com.infumia.infumialib.transformer.annotations.Names;
import tr.com.infumia.infumialib.transformer.annotations.Version;
import tr.com.infumia.infumialib.transformer.resolvers.Snakeyaml;

/**
 * a class that represents Shiru ka's config file.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Version
@Names(modifier = Names.Modifier.TO_LOWER_CASE, strategy = Names.Strategy.HYPHEN_CASE)
final class Config extends TransformedObject {

  /**
   * the Server's language.
   */
  @Comment("Defines the server'a language.")
  public static Locale lang = Locale.US;

  /**
   * loads the config.
   *
   * @param file the file to load.
   *
   * @return config.
   */
  @NotNull
  static Config loadConfig(@NotNull final Path file) {
    return TransformerPool.create(new Config(), config -> config
      .withFile(file)
      .withResolver(new Snakeyaml())
      .initiate());
  }

  /**
   * obtains Shiru ka's language bundle.
   *
   * @return Shiru ka's language bundle.
   */
  @NotNull
  private static ResourceBundle shirukaLanguageBundle() {
    return ResourceBundle.getBundle("language.shiruka.Shiruka", Config.lang);
  }

  /**
   * obtains Vanilla's language bundle.
   *
   * @return Vanilla's language bundle.
   */
  @NotNull
  private static ResourceBundle vanillaLanguageBundle() {
    return ResourceBundle.getBundle("language.vanilla.Vanilla", Config.lang);
  }

  /**
   * sets the server's language.
   *
   * @param lang the lang to set.
   */
  void language(@NotNull final Locale lang) {
    this.set("lang", lang);
    Languages.init(Config.shirukaLanguageBundle(), Config.vanillaLanguageBundle());
  }
}
