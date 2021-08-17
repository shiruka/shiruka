package io.github.shiruka.shiruka;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

/**
 * an utility class that contains constant values.
 */
final class Constants {

  /**
   * the art.
   */
  private static final String ART = """
    ███████ ██   ██ ██ ██████  ██    ██     ██   ██  █████ \s
    ██      ██   ██ ██ ██   ██ ██    ██     ██  ██  ██   ██\s
    ███████ ███████ ██ ██████  ██    ██     █████   ███████\s
         ██ ██   ██ ██ ██   ██ ██    ██     ██  ██  ██   ██\s
    ███████ ██   ██ ██ ██   ██  ██████      ██   ██ ██   ██
    """;

  /**
   * the here.
   */
  private static final String HERE = System.getProperty("user.dir");

  /**
   * the here path.
   */
  private static final Path HERE_PATH = Path.of(Constants.HERE);

  /**
   * the libs path.
   */
  private static final Path LIBS_PATH = Constants.HERE_PATH.resolve("libs");

  /**
   * the versions.
   */
  private static final String VERSION = "@version@";

  /**
   * ctor.
   */
  private Constants() {
  }

  /**
   * obtains the here path.
   *
   * @return here path.
   */
  @NotNull
  static Path getHerePath() {
    return Constants.HERE_PATH;
  }

  /**
   * obtains the libs path.
   *
   * @return libs path.
   */
  @NotNull
  @SneakyThrows
  static Path getLibsPath() {
    if (Files.notExists(Constants.LIBS_PATH)) {
      Files.createDirectories(Constants.LIBS_PATH);
    }
    return Constants.LIBS_PATH;
  }

  /**
   * obtains the version.
   *
   * @return version.
   */
  @NotNull
  static String getVersion() {
    return Constants.VERSION;
  }

  /**
   * prints the art.
   */
  static void printArt() {
    System.out.println(Constants.ART);
  }
}
