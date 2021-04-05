/*
 * MIT License
 *
 * Copyright (c) 2021 Shiru ka
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

package net.shiruka.shiruka.nbt.list;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.shiruka.shiruka.nbt.ListTag;
import net.shiruka.shiruka.nbt.Tag;
import net.shiruka.shiruka.nbt.TagTypes;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation for {@link ListTag}.
 */
public final class ListTagBasic implements ListTag {

  /**
   * the hashcode.
   */
  private final int hashCode;

  /**
   * the list id.
   */
  @NotNull
  private TagTypes listType;

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
  public ListTagBasic(@NotNull final List<Tag> original, @NotNull final TagTypes listType) {
    this.original = Collections.unmodifiableList(original);
    this.listType = listType;
    this.hashCode = original.hashCode();
  }

  @Override
  public void add(@NotNull final Tag tag) {
    this.edit(tags -> {
      final var endType = TagTypes.END;
      Preconditions.checkArgument(tag.getType() != endType,
        "Cannot add a %s to a %s", endType, Tag.LIST.getType());
      if (this.getListType() != endType) {
        Preconditions.checkArgument(tag.getType() == this.listType,
          "Trying to add tag of type %s to list of %s", tag.getType(), this.listType);
      }
      tags.add(tag);
    }, tag.getType());
  }

  @NotNull
  @Override
  public List<Tag> all() {
    return Collections.unmodifiableList(this.original);
  }

  @NotNull
  @Override
  public TagTypes getListType() {
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

  @NotNull
  @Override
  public Optional<Tag> get(final int key) {
    return Optional.ofNullable(this.original.get(key));
  }

  @Override
  public void remove(final int key) {
    this.edit(tags -> tags.remove(key), TagTypes.NONE);
  }

  @Override
  public void set(final int key, @NotNull final Tag tag) {
    this.edit(tags -> tags.set(key, tag), tag.getType());
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

  /**
   * edits with the given consumer.
   *
   * @param consumer the consumer to edit.
   * @param type the type to edit.
   */
  private void edit(@NotNull final Consumer<List<Tag>> consumer, @NotNull final TagTypes type) {
    final var tags = new ObjectArrayList<>(this.original);
    consumer.accept(tags);
    if (type != TagTypes.NONE && this.listType == TagTypes.END) {
      this.original = tags;
      this.listType = type;
    } else {
      this.original = tags;
    }
  }
}
