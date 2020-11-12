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

import io.github.shiruka.api.Server;
import io.github.shiruka.common.fragment.FragmentDescription;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link Server}.
 */
public final class ShirukaServer implements Server {

  /**
   * obtains the Shiru ka server's version
   */
  @NotNull
  public static final String VERSION = "1.0.0";

  @Override
  @NotNull
  public Map<String, FragmentDescription> filterFragments(
    @NotNull final Map<String, FragmentDescription> descriptions) {
    return descriptions;
  }

  @Override
  public void runCommand(@NotNull final String command) {
  }

  @Override
  public boolean isInShutdownState() {
    return false;
  }
}
