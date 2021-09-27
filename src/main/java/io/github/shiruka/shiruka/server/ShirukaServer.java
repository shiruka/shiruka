package io.github.shiruka.shiruka.server;

import io.github.shiruka.api.Provider;
import io.github.shiruka.api.Server;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.plugin.InvalidDescriptionException;
import io.github.shiruka.api.plugin.Plugin;
import io.github.shiruka.api.plugin.PluginManager;
import io.github.shiruka.api.plugin.java.JavaPluginLoader;
import io.github.shiruka.shiruka.Constants;
import io.github.shiruka.shiruka.event.ShirukaEventManager;
import io.github.shiruka.shiruka.scheduler.AsyncScheduler;
import io.github.shiruka.shiruka.scheduler.SyncScheduler;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents Shiru ka server.
 */
@Accessors(fluent = true)
public final class ShirukaServer implements Server {

  /**
   * the internal plugin.
   */
  @Nullable
  private static Plugin.Container internalPlugin;

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

  /**
   * ctor.
   *
   * @param pluginsPath the plugins path.
   */
  public ShirukaServer(@NotNull final Path pluginsPath) {
    Shiruka.server(this);
    ShirukaServer.prepareInternalPlugin();
    this.provider.register(new PluginManager(pluginsPath));
    this.provider.register(new ShirukaEventManager());
    this.provider.register(new SyncScheduler());
    this.provider.register(new AsyncScheduler());
  }

  /**
   * obtains the internal plugin.
   *
   * @return internal plugin.
   */
  @NotNull
  public static Plugin.Container getInternalPlugin() {
    return Objects.requireNonNull(ShirukaServer.internalPlugin, "internal plugin");
  }

  /**
   * prepares the internal plugin to use.
   */
  private static void prepareInternalPlugin() {
    try {
      ShirukaServer.internalPlugin = new Plugin.Container(
        ShirukaServer.class.getClassLoader(),
        Constants.herePath(),
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
        Constants.herePath().toFile()
      );
    } catch (final InvalidDescriptionException e) {
      throw new RuntimeException(e);
    }
  }
}
