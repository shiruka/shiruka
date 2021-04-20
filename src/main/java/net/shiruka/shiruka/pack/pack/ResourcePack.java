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

package net.shiruka.shiruka.pack.pack;

import com.nukkitx.protocol.bedrock.data.ResourcePackType;
import java.nio.file.Files;
import java.security.MessageDigest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.pack.Pack;
import net.shiruka.api.pack.PackLoader;
import net.shiruka.api.pack.PackManifest;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents resource packs.
 */
@RequiredArgsConstructor
public final class ResourcePack implements Pack {

  /**
   * the factory.
   */
  public static final Factory FACTORY = new ResourcePackFactory();

  /**
   * the loader.
   */
  @NotNull
  @Getter
  private final PackLoader loader;

  /**
   * the manifest.
   */
  @NotNull
  @Getter
  private final PackManifest manifest;

  /**
   * the hash.
   */
  private byte[] hash;

  @Override
  public byte[] getHash() {
    if (this.hash == null) {
      try {
        this.hash = MessageDigest.getInstance("SHA-256")
          .digest(Files.readAllBytes(this.loader.getPreparedFile().join()));
      } catch (final Exception e) {
        throw new IllegalStateException("Unable to get hash of pack!", e);
      }
    }
    return this.hash.clone();
  }

  @NotNull
  @Override
  public ResourcePackType getType() {
    return ResourcePackType.RESOURCE;
  }

  /**
   * a class that represents resource pack factories.
   */
  public static final class ResourcePackFactory implements Pack.Factory {

    @Override
    @NotNull
    public Pack create(@NotNull final PackLoader loader, @NotNull final PackManifest manifest,
                       @NotNull final PackManifest.Module module) {
      return new ResourcePack(loader, manifest);
    }
  }
}
