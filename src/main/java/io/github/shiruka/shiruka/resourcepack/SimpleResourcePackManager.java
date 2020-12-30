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

package io.github.shiruka.shiruka.resourcepack;

import io.github.shiruka.api.resourcepack.ResourcePackLoader;
import io.github.shiruka.api.resourcepack.ResourcePackManager;
import io.github.shiruka.api.resourcepack.ResourcePackManifest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link ResourcePackManager}.
 */
public final class SimpleResourcePackManager implements ResourcePackManager {

  @Override
  public void close() throws IOException {
  }

  @Override
  public void closeRegistration() {
  }

  @NotNull
  @Override
  public Optional<ResourcePackLoader> getLoader(@NotNull final Path path) throws IOException {
    return Optional.empty();
  }

  @NotNull
  @Override
  public ResourcePackManifest getManifest(@NotNull final ResourcePackLoader loader) {
    return null;
  }

  @Override
  public void loadResourcePack(@NotNull final Path path) {
  }

  @Override
  public void loadResourcePacks(@NotNull final Path path) {
  }

  @Override
  public void registerLoader(@NotNull final Class<? extends ResourcePackLoader> cls,
                             @NotNull final Predicate<Path> predicate,
                             @NotNull final Function<Path, ResourcePackLoader> function) {
  }
}
