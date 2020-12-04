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

package io.github.shiruka.shiruka.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains Shiru ka's utility methods.
 */
public final class ShirukaUtils {

  /**
   * the operating system.
   */
  @Nullable
  private static OS os = null;

  /**
   * ctor.
   */
  private ShirukaUtils() {
  }

  /**
   * obtains the operating system.
   *
   * @return the operating system.
   */
  @NotNull
  public static OS getOS() {
    if (ShirukaUtils.os != null) {
      return ShirukaUtils.os;
    }
    final var name = System.getProperty("os.name").toLowerCase();
    if (name.contains("win")) {
      ShirukaUtils.os = OS.WINDOWS;
    } else if (name.contains("nix") ||
      name.contains("nux") ||
      name.contains("aix")) {
      ShirukaUtils.os = OS.LINUX;
    } else if (name.contains("mac")) {
      ShirukaUtils.os = OS.MAC;
    } else if (name.contains("sunos")) {
      ShirukaUtils.os = OS.SOLARIS;
    }
    return ShirukaUtils.os;
  }

  /**
   * an enum class to determine operating systems.
   */
  public enum OS {
    /**
     * the windows.
     */
    WINDOWS,
    /**
     * the linux.
     */
    LINUX,
    /**
     * the macOS.
     */
    MAC,
    /**
     * the solaris.
     */
    SOLARIS
  }
}
