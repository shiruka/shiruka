package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.logging.Logger;
import lombok.SneakyThrows;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
public final class ShirukaBootstrap {

  /**
   * ctor.
   */
  private ShirukaBootstrap() {
  }

  /**
   * runs first when the application starts.
   *
   * @param args the args to pass into application.
   */
  public static void main(final String[] args) {
    ShirukaBootstrap.loadDependencies();
  }

  /**
   * loads Shiru ka's dependencies.
   */
  @SneakyThrows
  private static void loadDependencies() {
    final var here = Path.of(System.getProperty("user.dir"));
    final var libs = here.resolve("libs");
    if (Files.notExists(libs)) {
      Files.createDirectories(libs);
    }
    final var logger = Logger.getLogger("Shiru ka");
    logger.info("Loading dependencies, this might take a while...");
    try {
      ApplicationBuilder.appending("Shiru ka")
        .logger(new ProcessLogger() {
          @Override
          public void log(final String message, final Object... args) {
            logger.info(MessageFormat.format(message, args));
          }

          @Override
          public void debug(final String message, final Object... args) {
            logger.fine(MessageFormat.format(message, args));
          }
        })
        .downloadDirectoryPath(libs)
        .build();
    } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      logger.warning("Shiru ka failed to load its dependencies correctly!");
      logger.warning("This error should be reported at https://github.com/shiruka/shiruka/issues");
    }
  }
}
