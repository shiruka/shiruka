package io.github.shiruka.shiruka.server;

import io.github.shiruka.api.Provider;
import io.github.shiruka.api.Server;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.event.EventManager;
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
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
   * the async scheduler.
   */
  private final AsyncScheduler asyncScheduler = new AsyncScheduler();

  /**
   * the event manager.
   */
  private final EventManager eventManager = new ShirukaEventManager();

  /**
   * the logger.
   */
  @Getter
  private final Logger logger = LogManager.getLogger("Shiru ka");

  /**
   * the plugin manager.
   */
  @NotNull
  private final Plugin.Manager pluginManager;

  /**
   * the plugin path.
   */
  @NotNull
  private final Path pluginsPath;

  /**
   * the provider.
   */
  @Getter
  private final Provider provider = Provider.create();

  /**
   * the sync scheduler.
   */
  private final SyncScheduler syncScheduler = new SyncScheduler();

  static {
    try {
      INTERNAL_PLUGIN = new Plugin.Container(
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
        Constants.herePath().toFile());
    } catch (final InvalidDescriptionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * ctor.
   *
   * @param pluginsPath the plugins path.
   */
  public ShirukaServer(@NotNull final Path pluginsPath) {
    this.pluginsPath = pluginsPath;
    this.pluginManager = new PluginManager(this.pluginsPath);
  }

  /**
   * registers default providers.
   */
  private void registerDefaultProviders() {
    this.provider
      .register(this.eventManager)
      .register(this.asyncScheduler)
      .register(this.syncScheduler)
      .register(this.pluginManager);
  }
}
