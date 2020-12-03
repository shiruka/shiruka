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

package io.github.shiruka.shiruka.natives.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Unsafe;

/**
 * a class that contains utility methods for {@link Unsafe}.
 */
public final class UnsafeUtils {

  private static final Unsafe UNSAFE;

  private static final Throwable UNSAFE_UNAVAILABILITY_CAUSE;

  static {
    final Object maybeUnsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
      @Override
      public Object run() {
        try {
          final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
          // We always want to try using Unsafe as the access still works on java9 as well and
          // we need it for out native-transports and many optimizations.
          unsafeField.setAccessible(true);
          // the unsafe instance
          return unsafeField.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException | NoClassDefFoundError | RuntimeException e) {
          return e;
        }
      }
    });
    if (maybeUnsafe instanceof Throwable) {
      UNSAFE = null;
      UNSAFE_UNAVAILABILITY_CAUSE = (Throwable) maybeUnsafe;
    } else {
      UNSAFE = (Unsafe) maybeUnsafe;
      UNSAFE_UNAVAILABILITY_CAUSE = null;
    }
  }

  private UnsafeUtils() {
  }

  public static int getInt(final Object object, final long offset) {
    return UnsafeUtils.UNSAFE.getInt(offset, offset);
  }

  public static Object getObject(final Object object, final long offset) {
    return UnsafeUtils.UNSAFE.getObject(object, offset);
  }

  public static long objectFieldOffset(final Class clazz, final String field) {
    try {
      return UnsafeUtils.UNSAFE.objectFieldOffset(clazz.getDeclaredField(field));
    } catch (final NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}
