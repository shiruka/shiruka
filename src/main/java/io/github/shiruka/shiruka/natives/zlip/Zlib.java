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

package io.github.shiruka.shiruka.natives.zlip;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for Zlib.
 */
public final class Zlib {

  /**
   * the chunk bytes.
   */
  static final int CHUNK_BYTES = 8192;

  /**
   * the zlib instance.
   */
  private static final Zlib ZLIB_JAVA = new Zlib(JavaInflater::new, JavaDeflater::new);

  /**
   * the zlib creator.
   */
  public static final Supplier<Zlib> CREATOR = () -> Zlib.ZLIB_JAVA;

  /**
   * the zlib 11 instance.
   */
  private static final Zlib ZLIB_JAVA_11 = new Zlib(Java11Inflater::new, Java11Deflater::new);

  /**
   * the zlib 11 creator.
   */
  public static final Supplier<Zlib> CREATOR_11 = () -> Zlib.ZLIB_JAVA_11;

  /**
   * the creator for {@link Deflated}.
   */
  @NotNull
  private final BiFunction<Integer, Boolean, Deflated> deflatedCreator;

  /**
   * the creator for {@link Inflated}.
   */
  @NotNull
  private final Function<Boolean, Inflated> inflatedCreator;

  /**
   * ctor.
   *
   * @param inflatedCreator the {@link Inflated} creator.
   * @param deflatedCreator the {@link Deflated} creator.
   */
  private Zlib(@NotNull final Function<Boolean, Inflated> inflatedCreator,
               @NotNull final BiFunction<Integer, Boolean, Deflated> deflatedCreator) {
    this.inflatedCreator = inflatedCreator;
    this.deflatedCreator = deflatedCreator;
  }

  @NotNull
  public Inflated create(final boolean nowrap) {
    return this.inflatedCreator.apply(nowrap);
  }

  @NotNull
  public Deflated create(final int level, final boolean nowrap) {
    return this.deflatedCreator.apply(level, nowrap);
  }
}
