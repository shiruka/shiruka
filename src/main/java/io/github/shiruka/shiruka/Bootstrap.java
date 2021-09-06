package io.github.shiruka.shiruka;

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
   */
  public static void main(final String[] args) {
    if (Dependencies.load()) {
      Console.init(args);
    }
  }
}
