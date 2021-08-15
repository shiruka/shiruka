package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.logging.LogManager;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
@Log(topic = "Shiru ka")
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
   *
   * @throws IOException if something goes wrong when reading property files.
   */
  public static void main(final String[] args) throws IOException {
    LogManager.getLogManager().readConfiguration(
      ShirukaBootstrap.class.getResourceAsStream("/java.logger.properties"));
    ShirukaBootstrap.loadDependencies();
  }

  /**
   * loads Shiru ka's dependencies.
   */
  @SneakyThrows
  private static void loadDependencies() {
    final var libs = Paths.getLibsPath();
    ShirukaBootstrap.log.info("Loading dependencies, this might take a while...");
    try {
      ApplicationBuilder.appending("Shiru ka")
        .logger(new ProcessLogger() {
          @Override
          public void log(final String message, final Object... args) {
            ShirukaBootstrap.log.info(MessageFormat.format(message, args));
          }

          @Override
          public void debug(final String message, final Object... args) {
            ShirukaBootstrap.log.config(MessageFormat.format(message, args));
          }
        })
        .downloadDirectoryPath(libs)
        .build();
    } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      ShirukaBootstrap.log.warning("Shiru ka failed to load its dependencies correctly!");
      ShirukaBootstrap.log.warning("This error should be reported at https://github.com/shiruka/shiruka/issues");
    }
  }
}
