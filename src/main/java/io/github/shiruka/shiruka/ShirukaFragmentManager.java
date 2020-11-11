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

package io.github.shiruka.shiruka;

import io.github.shiruka.conf.Provider;
import io.github.shiruka.conf.config.YamlProvider;
import io.github.shiruka.fragment.FragmentManager;
import io.github.shiruka.log.Logger;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link FragmentManager}.
 */
public final class ShirukaFragmentManager extends FragmentManager {

  /**
   * the fragment's file name.
   */
  private static final String FRAGMENTS_FILE_NAME = "fragment.yml";

  /**
   * the fragment's file provider which is YAML.
   */
  private static final YamlProvider PROVIDER = new YamlProvider();

  /**
   * ctor.
   *
   * @param fragmentsDir the fragment directory.
   * @param logger the logger.
   */
  ShirukaFragmentManager(@NotNull final File fragmentsDir, @NotNull final Logger logger) {
    super(fragmentsDir, logger);
  }

  @NotNull
  @Override
  public String getFragmentConfigFileName() {
    return ShirukaFragmentManager.FRAGMENTS_FILE_NAME;
  }

  @NotNull
  @Override
  protected Provider<?> getConfigProvider() {
    return ShirukaFragmentManager.PROVIDER;
  }
}
