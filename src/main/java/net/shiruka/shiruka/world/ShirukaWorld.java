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

package net.shiruka.shiruka.world;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.shiruka.api.metadata.MetadataValue;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.registry.Resourced;
import net.shiruka.api.world.World;
import net.shiruka.shiruka.ShirukaServer;
import org.jetbrains.annotations.NotNull;

/**
 * an implementation of {@link World} for LevelDB.
 */
@RequiredArgsConstructor
@Log4j2
public final class ShirukaWorld implements World {

  /**
   * the dimension key.
   */
  @NotNull
  @Getter
  private final Resourced dimensionKey;

  /**
   * the name.
   */
  @NotNull
  @Getter
  private final String name;

  /**
   * the server.
   */
  @NotNull
  private final ShirukaServer server;

  /**
   * the unique id.
   */
  @NotNull
  @Getter
  private final UUID uniqueId;

  @NotNull
  @Override
  public List<MetadataValue> getMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAllMetadata(@NotNull final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeMetadata(@NotNull final String key, @NotNull final Plugin plugin) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMetadata(@NotNull final String key, @NotNull final MetadataValue value) {
    throw new UnsupportedOperationException();
  }
}
