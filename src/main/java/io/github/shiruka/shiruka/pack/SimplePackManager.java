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

package io.github.shiruka.shiruka.pack;

import com.google.common.base.Preconditions;
import io.github.shiruka.api.log.Loggers;
import io.github.shiruka.api.pack.*;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.network.packets.PacketOutPackStack;
import io.github.shiruka.shiruka.network.packets.PacketOutPacksInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link PackManager}.
 */
public final class SimplePackManager implements PackManager {

  /**
   * the manifest path.
   */
  private static final Path MANIFEST_PATH = Paths.get("manifest.json");

  /**
   * the loaders.
   */
  private final Map<Class<? extends PackLoader>, PackLoader.Factory> loaderFactories = new HashMap<>();

  /**
   * the packs.
   */
  private final EnumMap<PackManifest.PackType, Pack.Factory> packFactories = new EnumMap<>(PackManifest.PackType.class);

  /**
   * the pack stack packet.
   */
  private final AtomicReference<PacketOutPackStack> packStack = new AtomicReference<>();

  /**
   * the packs.
   */
  private final Map<String, Pack> packs = new HashMap<>();

  /**
   * the packs by id.
   */
  private final Map<UUID, Pack> packsById = new HashMap<>();

  /**
   * the packs info packet.
   */
  private final AtomicReference<PacketOutPacksInfo> packsInfos = new AtomicReference<>();

  /**
   * the closed.
   */
  private volatile boolean closed;

  @Override
  public void close() throws IOException {
    for (final var pack : this.packs.values()) {
      pack.close();
    }
  }

  @Override
  public void closeRegistration() {
    this.checkRegistrationClosed();
    final var mustAccept = (boolean) ServerConfig.FORCE_RESOURCES.getValue()
      .orElse(false);
    this.packsInfos.set(new PacketOutPacksInfo(
      this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PacketOutPacksInfo.Entry("", "", pack.getId().toString(),
            pack.getSize(), pack.getVersion().toString(), false, false, ""))
        .collect(Collectors.toList()),
      mustAccept));
    this.packStack.set(new PacketOutPackStack(
      this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PacketOutPackStack.Entry(pack.getId().toString(),
            pack.getVersion().toString(), ""))
        .collect(Collectors.toList()),
      "*",
      mustAccept));
    this.closed = true;
  }

  @NotNull
  @Override
  public Optional<PackLoader> getLoader(@NotNull final Path path) {
    return this.loaderFactories.values().stream()
      .map(loaderFactory -> loaderFactory.create(path))
      .filter(Objects::nonNull)
      .findFirst();
  }

  @NotNull
  @Override
  public Optional<PackManifest> getManifest(@NotNull final PackLoader loader) {
    if (!loader.hasAsset(SimplePackManager.MANIFEST_PATH)) {
      return Optional.empty();
    }
    try {
      final var asset = loader.getAsset(SimplePackManager.MANIFEST_PATH);
      if (asset.isPresent()) {
        return Optional.of(PackManifest.load(asset.get()));
      }
    } catch (final IllegalStateException | IOException e) {
      Loggers.error("Failed to load " + loader.getLocation(), e);
    }
    return Optional.empty();
  }

  @Override
  public void loadPack(@NotNull final Path path) {
    this.checkRegistrationClosed();
    final var loaderOptional = this.getLoader(path);
    Preconditions.checkState(loaderOptional.isPresent(), "No suitable loader found for pack!");
    final var loader = loaderOptional.get();
    final var manifestOptional = this.getManifest(loader);
    Preconditions.checkState(manifestOptional.isPresent(), "No manifest found!");
    final var manifest = manifestOptional.get();
    final var module = manifest.getModules().get(0);
    final var factory = this.packFactories.get(module.getType());
    Preconditions.checkNotNull(factory, "Unsupported pack type %s", module.getType());
    final var uuid = manifest.getHeader().getUuid();
    final var pack = factory.create(loader, manifest, module);
    this.packs.put(uuid + "_" + manifest.getHeader().getVersion(), pack);
    this.packsById.put(uuid, pack);
    loader.getPreparedFile();
  }

  @Override
  public void loadPacks(@NotNull final Path directory) {
    this.checkRegistrationClosed();
    Preconditions.checkArgument(Files.isDirectory(directory), "%s is not a directory", directory);
    final var loaders = new ArrayList<PackLoader>();
    try (final var stream = Files.newDirectoryStream(directory)) {
      for (final var entry : stream) {
        final var loader = this.getLoader(entry);
        if (loader.isEmpty()) {
          continue;
        }
        loaders.add(loader.get());
      }
    } catch (final IOException e) {
      Loggers.error("", e);
    }
    final var manifestMap = new HashMap<UUID, PackManifest>();
    final var loaderMap = new HashMap<UUID, PackLoader>();
    loaders.forEach(loader -> {
      final var optional = this.getManifest(loader);
      if (optional.isEmpty()) {
        return;
      }
      final var manifest = optional.get();
      final var uuid = manifest.getHeader().getUuid();
      manifestMap.put(uuid, manifest);
      loaderMap.put(uuid, loader);
    });
    loaders.clear();
    final var missingDependencies = new ArrayList<PackManifest>();
    var missingDependency = false;
    do {
      missingDependency = false;
      final var iterator = manifestMap.values().iterator();
      while (iterator.hasNext()) {
        final var manifest = iterator.next();
        for (final var dependency : manifest.getDependencies()) {
          final var uuid = dependency.getUuid();
          if (manifestMap.containsKey(uuid) || this.packsById.containsKey(uuid)) {
            continue;
          }
          // @todo #1:15m Check version.
          iterator.remove();
          missingDependencies.add(manifest);
          missingDependency = true;
          break;
        }
      }
    } while (missingDependency);
    if (!missingDependencies.isEmpty()) {
      final var joiner = new StringJoiner(", ");
      missingDependencies.stream()
        .map(pack -> pack.getHeader().getName() + ":" + pack.getHeader().getVersion())
        .forEach(joiner::add);
      Loggers.error("Could not load packs due to missing dependencies %s", joiner.toString());
    }
    for (final var manifest : manifestMap.values()) {
      final var loader = loaderMap.get(manifest.getHeader().getUuid());
      final var module = manifest.getModules().get(0);
      final var factory = this.packFactories.get(module.getType());
      if (factory == null) {
        Loggers.warn("Unsupported pack type %s", module.getType());
        continue;
      }
      final var uuid = manifest.getHeader().getUuid();
      final var pack = factory.create(loader, manifest, module);
      this.packs.put(uuid + "_" + manifest.getHeader().getVersion(), pack);
      this.packsById.put(uuid, pack);
      loader.getPreparedFile();
    }
    // @todo #1:60m Loggers.log(Shiruka.getLanguage().translate("shiruka.resources.success", String.valueOf(manifestMap.size())));
  }

  @Override
  public void registerLoader(@NotNull final Class<? extends PackLoader> cls,
                             @NotNull final PackLoader.Factory factory) {
    Preconditions.checkArgument(this.loaderFactories.putIfAbsent(cls, factory) == null,
      "The pack loader factory is already registered!");
  }

  @Override
  public void registerPack(@NotNull final PackManifest.PackType type, @NotNull final Pack.Factory factory) {
    Preconditions.checkArgument(this.packFactories.putIfAbsent(type, factory) == null,
      "The pack factory is already registered!");
  }

  /**
   * checks if the resource pack manager closed.
   *
   * @throws IllegalStateException if {@link #closed} is true.
   */
  private void checkRegistrationClosed() {
    Preconditions.checkState(!this.closed, "PackManager registration is closed!");
  }
}
