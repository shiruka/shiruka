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
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine compound tags which contain map of {@link Tag}.
 */
public interface CompoundTag extends Tag, StoredTag<String>, Map<String, Tag> {

  @Override
  default boolean isCompound() {
    return true;
  }

  @NotNull
  @Override
  default CompoundTag asCompound() {
    return this;
  }

  @Override
  default void write(@NotNull final DataOutput output) throws IOException {
    for (final var entry : this.entrySet()) {
      final var tag = entry.getValue();
      output.writeByte(tag.id());
      if (tag.id() != Tag.END.id()) {
        output.writeUTF(entry.getKey());
        tag.write(output);
      }
    }
    output.writeByte(BinaryTagTypes.END.id());
  }

  @Override
  @Nullable
  default String getString(@NotNull final String key) {
    if (this.containsKey(key)) {
      return this.get(key).asString()
        .value();
    }
    return null;
  }

  @Override
  default void setString(@NotNull final String key, @NotNull final String tag) {
    this.put(key, Tag.createString(tag));
  }
}
