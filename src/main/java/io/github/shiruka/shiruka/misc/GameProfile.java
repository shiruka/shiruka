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

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents game profiles of players.
 */
public final class GameProfile {

  /**
   * the name.
   */
  @NotNull
  private final String name;

  /**
   * the unique id.
   */
  @NotNull
  private final UUID uniqueId;

  /**
   * the xbox id.
   */
  @NotNull
  private final String xboxId;

  /**
   * ctor.
   *
   * @param name the name.
   * @param uniqueId the unique id.
   * @param xboxId the xbox id.
   */
  public GameProfile(@NotNull final String name, @NotNull final UUID uniqueId, @NotNull final String xboxId) {
    this.name = name;
    this.uniqueId = uniqueId;
    this.xboxId = xboxId;
  }

  /**
   * obtains the name.
   *
   * @return name.
   */
  @NotNull
  public String getName() {
    return this.name;
  }

  /**
   * the obtains the unique id.
   *
   * @return unique id.
   */
  @NotNull
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  /**
   * obtains the xbox id.
   *
   * @return xbox id.
   */
  @NotNull
  public String getXboxId() {
    return this.xboxId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.uniqueId, this.xboxId);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final var that = (GameProfile) o;
    return this.name.equals(that.name) &&
      this.uniqueId.equals(that.uniqueId) &&
      this.xboxId.equals(that.xboxId);
  }

  @Override
  public String toString() {
    return "GameProfile{" +
      "name='" + this.name + '\'' +
      ", uniqueId=" + this.uniqueId +
      ", xboxId='" + this.xboxId + '\'' +
      '}';
  }
}
