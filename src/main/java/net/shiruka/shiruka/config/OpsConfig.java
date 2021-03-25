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

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.configuration.FileConfiguration;
import net.shiruka.shiruka.base.OpEntry;
import org.jetbrains.annotations.NotNull;

/**
 * list of server operators.
 */
public final class OpsConfig implements ConfigHolder {

  /**
   * the configuration.
   */
  private static FileConfiguration configuration;

  /**
   * adds the given {@code entry} to the op list.
   *
   * @param entry the entry to add.
   */
  public static void addOp(@NotNull final OpEntry entry) {
    OpsConfig.configuration.set(entry.getProfile().getUniqueId().toString(), entry.serialize());
  }

  /**
   * checks if the unique id contains in the file.
   *
   * @param uniqueId the unique id to check.
   *
   * @return {@code true} if the unique id contains in the file.
   */
  public static boolean contains(@NotNull final String uniqueId) {
    return OpsConfig.configuration.contains(uniqueId);
  }

  /**
   * remove the given {@code entry} from the op list.
   *
   * @param entry the entry to remove.
   */
  public static void removeOp(@NotNull final OpEntry entry) {
    OpsConfig.configuration.set(entry.getProfile().getUniqueId().toString(), null);
  }

  /**
   * the ignored class.
   */
  public void ignored() {
    // ignored.
  }
}
