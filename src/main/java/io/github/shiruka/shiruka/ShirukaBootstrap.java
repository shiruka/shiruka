package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
public final class ShirukaBootstrap {
  /**
   * the java logger.
   */
  private static final Logger LOGGER = Logger.getLogger("Shiru ka");

  /**
   * ctor.
   */
  private ShirukaBootstrap() {
  }

  /**
   * runs first when the application starts.
   *
   * @param args the args to pass into application.
   *
   * @throws IOException if something goes wrong when reading property files.
   */
  public static void main(final String[] args) throws IOException {
    LogManager.getLogManager().readConfiguration(ShirukaBootstrap.class.getResourceAsStream("/java.logger.properties"));
    ShirukaBootstrap.loadDependencies();
  }

  /**
   * loads Shiru ka's dependencies.
   */
  private static void loadDependencies() {
    try {
      final var libs = Paths.getLibsPath();
      ShirukaBootstrap.LOGGER.info("Loading dependencies, this might take a while...");
      try {
        ApplicationBuilder.appending("Shiru ka").logger(new ProcessLogger() {
          @Override
          public void log(final String message, final Object... args) {
            ShirukaBootstrap.LOGGER.info(MessageFormat.format(message, args));
          }
          @Override
          public void debug(final String message, final Object... args) {
            ShirukaBootstrap.LOGGER.config(MessageFormat.format(message, args));
          }
        }).downloadDirectoryPath(libs).build();
      } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
        e.printStackTrace();
        ShirukaBootstrap.LOGGER.warning("Shiru ka failed to load its dependencies correctly!");
        ShirukaBootstrap.LOGGER.warning("This error should be reported at https://github.com/shiruka/shiruka/issues");
      }
    } catch (final java.lang.Throwable $ex) {
      throw lombok.Lombok.sneakyThrow($ex);
    }
  }
}
