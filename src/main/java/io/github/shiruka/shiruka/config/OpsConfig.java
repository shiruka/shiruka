/*
 * MIT License
 *
 * Copyright (c) 2020 Shiru ka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.shiruka.shiruka.config;

import io.github.shiruka.api.config.Config;
import io.github.shiruka.api.config.ConfigPath;
import io.github.shiruka.api.config.Paths;
import io.github.shiruka.api.config.config.PathableConfig;
import java.io.File;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * list of server operators.
 */
public final class OpsConfig extends PathableConfig {

  /**
   * op list of the server.
   */
  public static final ConfigPath<List<UUID>> OPS = Paths.listUniqueIdPath("ops", List.of());

  /**
   * ctor.
   *
   * @param origin the origin.
   */
  private OpsConfig(@NotNull final Config origin) {
    super(origin);
  }

  /**
   * initiates the server config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    Config.fromFile(file)
      .map(OpsConfig::new)
      .ifPresent(Config::save);
  }
}
