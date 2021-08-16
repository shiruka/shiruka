package io.github.shiruka.shiruka;

import io.github.shiruka.api.Provider;
import io.github.shiruka.api.Server;
import io.github.shiruka.shiruka.event.ShirukaEventManager;
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
   * starts the server.
   */
  void startServer() {
  }

  /**
   * registers default providers.
   */
  private void registerDefaultProviders() {
    this.provider.register(new ShirukaEventManager());
  }
}
