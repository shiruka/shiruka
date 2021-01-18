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

package io.github.shiruka.shiruka.pack;

import com.google.common.base.Preconditions;
import io.github.shiruka.api.pack.*;
import io.github.shiruka.api.text.TranslatedText;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.network.packets.PacketOutPackInfo;
import io.github.shiruka.shiruka.network.packets.PacketOutPackStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a simple implementation for {@link PackManager}.
 */
public final class SimplePackManager implements PackManager {

  /**
   * the logger.
   */
  private static final Logger LOGGER = LogManager.getLogger("SimplePackManager");

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
   * the packs info packet.
   */
  private final AtomicReference<PacketOutPackInfo> packInfo = new AtomicReference<>(new PacketOutPackInfo(
    new ObjectArrayList<>(), false, new ObjectArrayList<>(), false));

  /**
   * the pack stack packet.
   */
  private final AtomicReference<PacketOutPackStack> packStack = new AtomicReference<>(new PacketOutPackStack(
    new ObjectArrayList<>(), new ObjectArrayList<>(), false, false, "", new ObjectArrayList<>()));

  /**
   * the packs.
   */
  private final Map<String, Pack> packs = new HashMap<>();

  /**
   * the packs by id.
   */
  private final Map<UUID, Pack> packsById = new HashMap<>();

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
    this.checkClosed();
    final var mustAccept = (boolean) ServerConfig.FORCE_RESOURCES.getValue()
      .orElse(false);
    this.packInfo.set(new PacketOutPackInfo(Collections.emptyList(),
      mustAccept,
      new ObjectArrayList<>(this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PacketOutPackInfo.Entry("", "", pack.getId().toString(), pack.getSize(), pack.getVersion().toString(),
            false, false, ""))
        .collect(Collectors.toList())),
      false));
    this.packStack.set(new PacketOutPackStack(
      Collections.emptyList(),
      Collections.emptyList(),
      true,
      mustAccept,
      "*",
      this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PacketOutPackStack.Entry(pack.getId().toString(), pack.getVersion().toString(), ""))
        .collect(Collectors.toList())));
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
      SimplePackManager.LOGGER.error(String.format("Failed to load %s", loader.getLocation()), e);
    }
    return Optional.empty();
  }

  @NotNull
  @Override
  public Optional<Pack> getPack(@NotNull final String s) {
    return Optional.ofNullable(this.packs.get(s));
  }

  @NotNull
  @Override
  public Optional<Pack> getPackByUniqueId(@NotNull final UUID uuid) {
    return Optional.ofNullable(this.packsById.get(uuid));
  }

  @NotNull
  @Override
  public Object getPackInfo() {
    return this.packInfo.get();
  }

  @NotNull
  @Override
  public Object getPackStack() {
    return this.packStack.get();
  }

  @Override
  public void loadPack(@NotNull final Path path) {
    this.checkClosed();
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
    this.checkClosed();
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
      SimplePackManager.LOGGER.error("", e);
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
      SimplePackManager.LOGGER.error("Could not load packs due to missing dependencies {}", joiner);
    }
    for (final var manifest : manifestMap.values()) {
      final var loader = loaderMap.get(manifest.getHeader().getUuid());
      final var module = manifest.getModules().get(0);
      final var factory = this.packFactories.get(module.getType());
      if (factory == null) {
        SimplePackManager.LOGGER.warn("Unsupported pack type {}", module.getType());
        continue;
      }
      final var uuid = manifest.getHeader().getUuid();
      final var pack = factory.create(loader, manifest, module);
      this.packs.put(uuid + "_" + manifest.getHeader().getVersion(), pack);
      this.packsById.put(uuid, pack);
      loader.getPreparedFile();
    }
    final var translate = TranslatedText.get("shiruka.resources.success")
      .translate(manifestMap.size());
    SimplePackManager.LOGGER.debug(translate);
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
  private void checkClosed() {
    Preconditions.checkState(!this.closed, "PackManager registration is closed!");
  }
}
