package io.github.shiruka.shiruka;

import java.io.IOException;

/**
 * a class that represents bootstrap for running the Shiru ka.
 */
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
    if (Dependencies.load()) {
      Console.init(args);
    }
  }
}
