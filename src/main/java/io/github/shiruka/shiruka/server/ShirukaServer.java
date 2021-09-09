package io.github.shiruka.shiruka.server;

import io.github.shiruka.api.Provider;
import io.github.shiruka.api.Server;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.plugin.InvalidDescriptionException;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.plugin.java.JavaPluginLoader;
import io.github.shiruka.shiruka.Constants;
import io.github.shiruka.shiruka.event.ShirukaEventManager;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * a class that represents Shiru ka server.
 */
@Accessors(fluent = true)
public final class ShirukaServer implements Server {

  /**
   * the internal plugin.
   */
  public static final Plugin.Container INTERNAL_PLUGIN;

  /**
   * the logger.
   */
  @Getter
  private final Logger logger = LogManager.getLogger("Shiru ka");

  /**
   * the provider.
   */
  @Getter
  private final Provider provider = Provider.create();

  static {
    try {
      INTERNAL_PLUGIN = new Plugin.Container(
        ShirukaServer.class.getClassLoader(),
        Constants.herePath().toFile(),
        Plugin.Description.of(
          Map.of(
            "name", "Shiru ka",
            "main", "io.github.shiruka.shiruka.Bootstrap"
          )
        ),
        new JavaPluginLoader(),
        Shiruka.logger(),
        new Plugin() {
        },
        Constants.herePath().toFile());
    } catch (final InvalidDescriptionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * registers default providers.
   */
  private void registerDefaultProviders() {
    this.provider.register(new ShirukaEventManager());
  }
}
