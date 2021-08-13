package io.github.shiruka.shiruka;

import io.github.shiruka.api.Provider;
import io.github.shiruka.api.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * a class that represents Shiru ka server.
 */
public final class ShirukaServer implements Server {
  /**
   * the logger.
   */
  private final Logger logger = LogManager.getLogger("Shiru ka");
  /**
   * the provider.
   */
  private final Provider provider = Provider.create();

  /**
   * the logger.
   */
  public Logger logger() {
    return this.logger;
  }

  /**
   * the provider.
   */
  public Provider provider() {
    return this.provider;
  }
}
