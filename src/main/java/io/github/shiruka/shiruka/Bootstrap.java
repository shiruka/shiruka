package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogManager;
import lombok.SneakyThrows;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
public final class Bootstrap {

  /**
   * the spinner.
   */
  private static final String[] SPINNER = new String[]{"\u0008/", "\u0008-", "\u0008\\", "\u0008|"};

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
    LogManager.getLogManager().readConfiguration(
      Bootstrap.class.getResourceAsStream("/java.logger.properties"));
    if (!Bootstrap.loadDependencies()) {
      return;
    }
    Constants.printArt();
    Console.init(args);
  }

  /**
   * loads Shiru ka's dependencies.
   *
   * @return {@code true} if the libraries load successfully.
   */
  @SneakyThrows
  private static boolean loadDependencies() {
    final var libs = Constants.getLibsPath();
    System.out.print("Loading dependencies, this might take a while ");
    final var loading = new AtomicBoolean(true);
    final var index = new AtomicInteger(0);
    final var console = System.console();
    console.printf("|");
    final var thread = new Thread(() -> {
      while (loading.get()) {
        if (index.get() > Bootstrap.SPINNER.length) {
          index.set(0);
        }
        try {
          Thread.sleep(100L);
          console.printf(Bootstrap.SPINNER[index.getAndIncrement() % Bootstrap.SPINNER.length]);
        } catch (final InterruptedException ignored) {
        }
      }
    });
    thread.start();
    try {
      ApplicationBuilder.appending("Shiru ka")
        .logger(new ProcessLogger() {
          @Override
          public void log(final String message, final Object... args) {
          }

          @Override
          public void debug(final String message, final Object... args) {
          }
        })
        .downloadDirectoryPath(libs)
        .build();
      thread.interrupt();
      loading.set(false);
      new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      Runtime.getRuntime().exec("clear");
      return true;
    } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println("Shiru ka failed to load its dependencies correctly!");
      System.out.println("This error should be reported at https://github.com/shiruka/shiruka/issues");
    }
    return false;
  }
}
