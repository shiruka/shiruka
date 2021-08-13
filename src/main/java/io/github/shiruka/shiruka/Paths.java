package io.github.shiruka.shiruka;

import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * an utility class that contains constant paths.
 */
final class Paths {
  /**
   * the here.
   */
  private static final String HERE = System.getProperty("user.dir");
  /**
   * the here path.
   */
  private static final Path HERE_PATH = Path.of(Paths.HERE);
  /**
   * the libs path.
   */
  private static final Path LIBS_PATH = Paths.HERE_PATH.resolve("libs");

  /**
   * ctor.
   */
  private Paths() {
  }

  /**
   * obtains the libs path.
   *
   * @return libs path.
   */
  @NotNull
  static Path getLibsPath() {
    try {
      if (Files.notExists(Paths.LIBS_PATH)) {
        Files.createDirectories(Paths.LIBS_PATH);
      }
      return Paths.LIBS_PATH;
    } catch (final java.lang.Throwable $ex) {
      throw lombok.Lombok.sneakyThrow($ex);
    }
  }
}
