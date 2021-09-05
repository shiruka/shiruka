package io.github.shiruka.shiruka;

import io.github.slimjar.app.builder.ApplicationBuilder;
import io.github.slimjar.logging.ProcessLogger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;

/**
 * a class that represents to load Shiru ka's dependencies.
 */
final class Dependencies {

  /**
   * ctor.
   */
  private Dependencies() {
  }

  /**
   * loads Shiru ka's dependencies.
   *
   * @return {@code true} if the libraries load successfully.
   */
  @SneakyThrows
  static boolean load() {
    final var libs = Constants.getLibsPath();
    System.out.print("Loading dependencies, this might take a while ");
    final var loading = new AtomicBoolean(true);
    final var index = new AtomicInteger(0);
    final var console = System.console();
    if (console != null) {
      console.printf("|");
    }
    final var thread = new Thread(() -> {
      final var spinner = Constants.getSpinner();
      while (loading.get()) {
        if (index.get() > spinner.length) {
          index.set(0);
        }
        try {
          Thread.sleep(100L);
          if (console != null) {
            console.printf(spinner[index.getAndIncrement() % spinner.length]);
          }
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
      if (System.getProperty("os.name").startsWith("Windows")) {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } else {
        Runtime.getRuntime().exec("clear");
      }
      return true;
    } catch (final IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println("Shiru ka failed to load its dependencies correctly!");
      System.out.println("This error should be reported at https://github.com/shiruka/shiruka/issues");
    }
    return false;
  }
}
