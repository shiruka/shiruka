package io.github.shiruka.shiruka;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.experimental.PackagePrivate;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * an utility class that contains constant paths.
 */
@UtilityClass
class Paths {

  /**
   * the here.
   */
  private final String HERE = System.getProperty("user.dir");

  /**
   * the here path.
   */
  private final Path HERE_PATH = Path.of(Paths.HERE);

  /**
   * the libs path.
   */
  private final Path LIBS_PATH = Paths.HERE_PATH.resolve("libs");

  /**
   * obtains the libs path.
   *
   * @return libs path.
   */
  @NotNull
  @SneakyThrows
  Path getLibsPath() {
    if (Files.notExists(Paths.LIBS_PATH)) {
      Files.createDirectories(Paths.LIBS_PATH);
    }
    return Paths.LIBS_PATH;
  }
}
