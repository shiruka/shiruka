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

import java.io.DataOutput;
import java.io.IOException;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine list tags which contain list of {@link Tag}.
 */
public interface ListTag extends Tag, StoredTag<Integer>, Iterable<Tag> {

  @Override
  default boolean isList() {
    return true;
  }

  @NotNull
  @Override
  default ListTag asList() {
    return this;
  }

  @Override
  default byte id() {
    return 9;
  }

  @Override
  default void write(@NotNull final DataOutput output) throws IOException {
    output.writeByte(this.listType());
    output.writeInt(this.size());
    for (final var tag : this) {
      tag.write(output);
    }
  }

  @Override
  default boolean containsKey(@NotNull final Integer key) {
    return this.size() > key;
  }

  /**
   * adds the given tag.
   *
   * @param tag the tag to add.
   */
  void add(@NotNull Tag tag);

  /**
   * creates a stream of the tags contained within this list.
   *
   * @return a new stream.
   */
  @NotNull
  Stream<Tag> stream();

  /**
   * obtains list's inside id of the tags.
   *
   * @return list's inside id of the tags
   */
  byte listType();
}
