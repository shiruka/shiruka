package io.github.shiruka.shiruka;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.infumialib.transformer.TransformedObject;
import tr.com.infumia.infumialib.transformer.TransformerPool;
import tr.com.infumia.infumialib.transformer.resolvers.Snakeyaml;

final class Config extends TransformedObject {

  public static void loadConfig(@NotNull Path folder) {
    TransformerPool.create(new Config())
      .withFile(folder.resolve("shiruka.yml"))
      .withResolver(new Snakeyaml())
      .initiate();
  }
}
