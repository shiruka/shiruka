package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
@Log(topic = "Shiru ka")
public final class Bootstrap {

  /**
   * ctor.
   */
  private Bootstrap() {
  }

  /**
   * runs first when the application starts.
   *
   * @param args the args to pass into application.
   *
   * @throws IOException if something goes wrong when reading property files.
   */
  public static void main(final String[] args) throws IOException {
    Constants.printArt();
    LogManager.getLogManager().readConfiguration(
      Bootstrap.class.getResourceAsStream("/java.logger.properties"));
    Bootstrap.loadDependencies();
    Console.init(args);
  }

  /**
   * loads Shiru ka's dependencies.
   */
  @SneakyThrows
  private static void loadDependencies() {
    final var libs = Constants.getLibsPath();
    Bootstrap.log.info("Loading dependencies, this might take a while...");
    try {
      ApplicationBuilder.appending("Shiru ka")
        .logger(new ProcessLogger() {
          @Override
          public void log(final String message, final Object... args) {
            System.out.print('.');
          }

          @Override
          public void debug(final String message, final Object... args) {
            System.out.print('.');
          }
        })
        .downloadDirectoryPath(libs)
        .build();
      System.out.println('\n');
    } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println('\n');
      Bootstrap.log.warning("Shiru ka failed to load its dependencies correctly!");
      Bootstrap.log.warning("This error should be reported at https://github.com/shiruka/shiruka/issues");
    }
  }
}
