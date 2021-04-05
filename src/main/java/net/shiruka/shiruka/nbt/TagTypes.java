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

package net.shiruka.shiruka.nbt;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.shiruka.nbt.primitive.EndTag;
import org.jetbrains.annotations.NotNull;

/**
 * an enum class that contains type of nbt tags.
 */
@RequiredArgsConstructor
public enum TagTypes {
  /**
   * the none tag.
   */
  NONE(() -> null, -1),
  /**
   * the end tag.
   */
  END(EndTag::new, 0),
  /**
   * the byte tag.
   */
  BYTE(Tag::createByte, 1),
  /**
   * the short tag.
   */
  SHORT(Tag::createShort, 2),
  /**
   * the int tag.
   */
  INT(Tag::createInt, 3),
  /**
   * the long tag.
   */
  LONG(Tag::createLong, 4),
  /**
   * the float tag.
   */
  FLOAT(Tag::createFloat, 5),
  /**
   * the double tag.
   */
  DOUBLE(Tag::createDouble, 6),
  /**
   * the byte array tag.
   */
  BYTE_ARRAY(Tag::createByteArray, 7),
  /**
   * the string tag.
   */
  STRING(Tag::createString, 8),
  /**
   * the list tag.
   */
  LIST(Tag::createList, 9),
  /**
   * the compound tag.
   */
  COMPOUND(Tag::createCompound, 10),
  /**
   * the int array tag.
   */
  INT_ARRAY(Tag::createIntArray, 11),
  /**
   * the long array tag.
   */
  LONG_ARRAY(Tag::createLongArray, 12),
  /**
   * the all tag.
   */
  ALL(() -> null, 99);

  /**
   * the empty tag supplier.
   */
  @NotNull
  private final Supplier<? extends Tag> emptyTagSupplier;

  /**
   * the id.
   */
  @Getter
  private final byte id;

  /**
   * ctor.
   *
   * @param emptyTagSupplier the empty tag supplier.
   * @param id the id.
   */
  TagTypes(@NotNull final Supplier<? extends Tag> emptyTagSupplier, final int id) {
    this(emptyTagSupplier, (byte) id);
  }

  /**
   * obtains an empty tag.
   *
   * @return empty tag.
   */
  @NotNull
  public Tag getEmptyTag() {
    return this.emptyTagSupplier.get();
  }
}
