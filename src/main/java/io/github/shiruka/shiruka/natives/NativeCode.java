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

package io.github.shiruka.shiruka.natives;

import com.google.common.io.ByteStreams;
import io.github.shiruka.api.log.Loggers;
import java.io.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.PlatformEnum;
import oshi.SystemInfo;

/**
 * a class that loads native codes.
 */
public final class NativeCode {

  /**
   * the prefix.
   */
  public static final String PREFIX = SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS ? "" : "lib";

  /**
   * the suffix.
   */
  public static final String SUFFIX = SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS ? ".dll" : ".so";

  /**
   * the name.
   */
  @NotNull
  private final String name;

  /**
   * if it's loaded.
   */
  private boolean loaded;

  /**
   * ctor.
   *
   * @param name the name.
   */
  public NativeCode(@NotNull final String name) {
    this.name = name;
  }

  /**
   * gives the input from the given name.
   *
   * @param name the name to get.
   *
   * @return an input stream from the resource path.
   */
  @Nullable
  private static InputStream getInput(@NotNull final String name) {
    var in = NativeCode.class.getClassLoader().getResourceAsStream(
      NativeCode.PREFIX + name + NativeCode.SUFFIX);
    if (in == null) {
      try {
        in = new FileInputStream("./src/main/resources/" + NativeCode.PREFIX + name + NativeCode.SUFFIX);
      } catch (final FileNotFoundException ignored) {
      }
    }
    return in;
  }

  /**
   * checks if the current platform is supported by native code or not.
   *
   * @return {@code true} when supported, false when not.
   */
  private static boolean isSupported() {
    return (SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS ||
      SystemInfo.getCurrentPlatformEnum() == PlatformEnum.LINUX) &&
      "amd64".equals(System.getProperty("os.arch"));
  }

  /**
   * tries to load the native implementation.
   *
   * @return {@code true} when the native implementation loaded, false otherwise.
   */
  public boolean load() {
    if (this.loaded || !NativeCode.isSupported()) {
      return this.loaded;
    }
    final var fullName = "shiruka-" + this.name;
    try {
      System.loadLibrary(fullName);
      this.loaded = true;
    } catch (final Throwable ignored) {
    }
    if (this.loaded) {
      return true;
    }
    try (final var soFile = NativeCode.getInput(this.name)) {
      if (soFile == null) {
        this.loaded = false;
        return false;
      }
      final var temp = File.createTempFile(fullName, NativeCode.SUFFIX);
      temp.deleteOnExit();
      try (final var outputStream = new FileOutputStream(temp)) {
        ByteStreams.copy(soFile, outputStream);
      }
      System.load(temp.getPath());
      this.loaded = true;
    } catch (final IOException ignored) {
    } catch (final UnsatisfiedLinkError ex) {
      Loggers.error("Could not load native library: %s", ex.getMessage());
    }
    return this.loaded;
  }
}
