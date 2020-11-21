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

package io.github.shiruka.shiruka.nbt;

import java.lang.reflect.Array;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NbtList<E> extends AbstractList<E> {

  public static final NbtList<Void> EMPTY = new NbtList<>(NbtType.END);

  @NotNull
  private final NbtType<E> type;

  @NotNull
  private final E[] array;

  private transient boolean hashCodeGenerated;

  private transient int hashCode;

  @SuppressWarnings("unchecked")
  public NbtList(@NotNull final NbtType<E> type, @NotNull final Collection<E> collection) {
    this.type = type;
    final var newArray = (E[]) Array.newInstance(type.getTagClass(), collection.size());
    this.array = collection.toArray(newArray);
  }

  @SafeVarargs
  public NbtList(@NotNull final NbtType<E> tagClass, @NotNull final E... array) {
    this.type = tagClass;
    this.array = Arrays.copyOf(array, array.length);
  }

  @NotNull
  public NbtType<E> getType() {
    return this.type;
  }

  @Nullable
  @Override
  public E get(final int index) {
    if (index < 0 || index >= this.array.length) {
      throw new ArrayIndexOutOfBoundsException("Expected 0-" + (this.array.length - 1) + ". Got " + index);
    }
    return Nbt.copy(this.array[index]);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof List)) {
      return false;
    }
    final var e1 = this.listIterator();
    final var e2 = ((List<?>) o).listIterator();
    while (e1.hasNext() && e2.hasNext()) {
      final var o1 = e1.next();
      final var o2 = e2.next();
      if (!Objects.deepEquals(o1, o2)) {
        return false;
      }
    }
    return !(e1.hasNext() || e2.hasNext());
  }

  @Override
  public int hashCode() {
    if (this.hashCodeGenerated) {
      return this.hashCode;
    }
    var result = Objects.hash(super.hashCode(), this.type);
    result = 31 * result + Arrays.deepHashCode(this.array);
    this.hashCode = result;
    this.hashCodeGenerated = true;
    return result;
  }

  @Override
  public int size() {
    return this.array.length;
  }

  @Override
  public String toString() {
    final var it = this.iterator();
    if (!it.hasNext()) {
      return "[]";
    }
    final var sb = new StringBuilder();
    sb.append('[').append('\n');
    for (; ; ) {
      final var text = Nbt.toString(it.next());
      sb.append(Nbt.indent(text));
      if (!it.hasNext()) {
        return sb.append('\n').append(']').toString();
      }
      sb.append(',').append('\n');
    }
  }
}
