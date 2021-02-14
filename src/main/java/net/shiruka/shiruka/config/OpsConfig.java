/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
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

package net.shiruka.shiruka.config;

import java.io.File;
import java.util.Objects;
import net.shiruka.api.config.Config;
import net.shiruka.api.config.config.PathableConfig;
import net.shiruka.shiruka.base.OpEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * list of server operators.
 */
public final class OpsConfig extends PathableConfig {

  /**
   * the instance.
   */
  @Nullable
  private static OpsConfig instance;

  /**
   * ctor.
   *
   * @param origin the origin.
   */
  private OpsConfig(@NotNull final Config origin) {
    super(origin);
  }

  /**
   * adds the given {@code entry} to the op list.
   *
   * @param entry the entry to add.
   */
  public static void addOp(@NotNull final OpEntry entry) {
    OpsConfig.getInstance().getConfiguration().set(
      entry.getProfile().getUniqueId().toString(),
      entry.serialize());
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  public static OpsConfig getInstance() {
    return Objects.requireNonNull(OpsConfig.instance);
  }

  /**
   * initiates the server config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    Config.fromFile(file)
      .map(OpsConfig::new)
      .ifPresent(config -> {
        config.save();
        OpsConfig.instance = config;
      });
  }

  /**
   * remove the given {@code entry} from the op list.
   *
   * @param entry the entry to remove.
   */
  public static void removeOp(@NotNull final OpEntry entry) {
    OpsConfig.getInstance().getConfiguration().remove(entry.getProfile().getUniqueId().toString());
  }
}
