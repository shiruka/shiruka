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

package io.github.shiruka.shiruka.world;

import io.github.shiruka.api.world.World;
import io.github.shiruka.api.world.WorldLoader;
import io.github.shiruka.shiruka.config.ServerConfig;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * an abstract implementation for {@link WorldLoader}
 */
public abstract class ShirukaWorldLoader implements WorldLoader {

  /**
   * the file visitor which removes all files under the enclosing directory
   * (including the enclosing directory and all subdirectories).
   */
  protected static final SimpleFileVisitor<Path> DELETE_FILES = new SimpleFileVisitor<>() {
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  };

  /**
   * the worlds map.
   */
  @NotNull
  protected final Map<String, World> worldsByName = new ConcurrentHashMap<>();

  /**
   * the worlds map.
   */
  @NotNull
  protected final Map<UUID, World> worldsByUniqueId = new ConcurrentHashMap<>();

  /**
   * ctor.
   */
  protected ShirukaWorldLoader() {
  }

  @NotNull
  @Override
  public final World getDefaultWorld() {
    return this.worldsByName.get(ServerConfig.DEFAULT_WORLD_NAME.getValue().orElseThrow());
  }

  @NotNull
  @Override
  public final Map<String, World> getWorldsByName() {
    return Collections.unmodifiableMap(this.worldsByName);
  }

  @NotNull
  @Override
  public final Map<UUID, World> getWorldsByUniqueId() {
    return Collections.unmodifiableMap(this.worldsByUniqueId);
  }
}
