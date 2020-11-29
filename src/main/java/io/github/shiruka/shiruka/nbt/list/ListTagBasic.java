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

package io.github.shiruka.shiruka.nbt.list;

import io.github.shiruka.shiruka.nbt.ListTag;
import io.github.shiruka.shiruka.nbt.Tag;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.cactoos.list.ListEnvelope;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ListTag} and {@link ListEnvelope}.
 */
public final class ListTagBasic implements ListTag {

  /**
   * the hashcode.
   */
  private final int hashCode;

  /**
   * the list id.
   */
  private byte listType;

  /**
   * the original.
   */
  @NotNull
  private List<Tag> original;

  /**
   * ctor.
   *
   * @param original the original.
   * @param listType the list type.
   */
  public ListTagBasic(@NotNull final List<Tag> original, final byte listType) {
    this.original = Collections.unmodifiableList(original);
    this.listType = listType;
    this.hashCode = original.hashCode();
  }

  /**
   * throws an exception if the given tag's id not equals to the given id.
   *
   * @param tag the tag to check.
   *
   * @throws IllegalArgumentException if the given tag's id not equals to the given id.
   */
  private static void mustBeSameType(@NotNull final Tag tag, final byte id) {
    if (tag.id() != id) {
      throw new IllegalArgumentException(String.format("Trying to add tag of type %s to list of %s", tag.id(), id));
    }
  }

  /**
   * throws an exception if the given tag's id equals to the end id.
   *
   * @param tag the tag to check.
   *
   * @throws IllegalArgumentException if the given tag's id equals to the end id.
   */
  private static void noAddEnd(@NotNull final Tag tag) {
    if (tag.id() == Tag.END.id()) {
      throw new IllegalArgumentException(String.format("Cannot add a %s to a %s", Tag.END.id(), Tag.LIST.id()));
    }
  }

  @Override
  public void add(@NotNull final Tag tag) {
    this.edit(tags -> {
      ListTagBasic.noAddEnd(tag);
      if (this.listType() != Tag.END.id()) {
        ListTagBasic.mustBeSameType(tag, this.listType);
      }
      tags.add(tag);
    }, tag.id());
  }

  @NotNull
  @Override
  public List<Tag> all() {
    return Collections.unmodifiableList(this.original);
  }

  @Override
  public byte listType() {
    return this.listType;
  }

  @NotNull
  @Override
  public Stream<Tag> stream() {
    return this.original.stream();
  }

  @Override
  public boolean contains(@NotNull final Tag tag) {
    return this.original.contains(tag);
  }

  @Override
  public Tag get(@NotNull final Integer key) {
    return this.original.get(key);
  }

  @Override
  public void remove(@NotNull final Integer index) {
    this.edit(tags -> tags.remove(index.intValue()), (byte) -1);
  }

  @Override
  public void set(@NotNull final Integer index, @NotNull final Tag tag) {
    this.edit(tags -> tags.set(index, tag), tag.id());
  }

  @Override
  public int size() {
    return this.original.size();
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj ||
      obj instanceof ListTagBasic && this.original.equals(((ListTagBasic) obj).original);
  }

  @Override
  public String toString() {
    return this.original.toString();
  }

  @Override
  public Iterator<Tag> iterator() {
    final var iterator = this.original.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Tag next() {
        return iterator.next();
      }

      @Override
      public void forEachRemaining(final Consumer<? super Tag> action) {
        iterator.forEachRemaining(action);
      }
    };
  }

  @Override
  public void forEach(@NotNull final Consumer<? super Tag> action) {
    this.original.forEach(action);
  }

  @NotNull
  @Override
  public Spliterator<Tag> spliterator() {
    return Spliterators.spliterator(this.original, Spliterator.ORDERED | Spliterator.IMMUTABLE);
  }

  private void edit(@NotNull final Consumer<List<Tag>> consumer, final byte type) {
    final var tags = new ArrayList<>(this.original);
    consumer.accept(tags);
    if (type != -1 && this.listType == Tag.END.id()) {
      this.original = tags;
      this.listType = type;
    } else {
      this.original = tags;
    }
  }
}
