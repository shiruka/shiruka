package io.github.shiruka.shiruka;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.infumialib.transformer.TransformedObject;
import tr.com.infumia.infumialib.transformer.TransformerPool;
import tr.com.infumia.infumialib.transformer.resolvers.Snakeyaml;

/**
 * a class that represents Shiru ka's config file.
 */
final class Config extends TransformedObject {

  /**
   * loads the config.
   *
   * @param folder the folder to load.
   */
  public static void loadConfig(@NotNull final Path folder) {
    TransformerPool.create(new Config())
      .withFile(folder.resolve("shiruka.yml"))
      .withResolver(new Snakeyaml())
      .initiate();
  }
}
