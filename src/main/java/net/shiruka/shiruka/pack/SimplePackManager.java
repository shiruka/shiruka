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

package net.shiruka.shiruka.pack;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.shiruka.api.Shiruka;
import net.shiruka.api.pack.*;
import net.shiruka.api.text.TranslatedText;
import net.shiruka.shiruka.ShirukaMain;
import net.shiruka.shiruka.config.ServerConfig;
import net.shiruka.shiruka.network.packets.PackInfoPacket;
import net.shiruka.shiruka.network.packets.PackStackPacket;
import net.shiruka.shiruka.pack.loader.RplDirectory;
import net.shiruka.shiruka.pack.loader.RplZip;
import net.shiruka.shiruka.pack.pack.ResourcePack;
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
   * the packs path.
   */
  private static final Path PACKS_PATH = ShirukaMain.HOME_PATH.resolve("packs");

  /**
   * the loaders.
   */
  private final Map<Class<? extends PackLoader>, PackLoader.Factory> loaderFactories = new Object2ObjectOpenHashMap<>();

  /**
   * the packs.
   */
  private final EnumMap<PackManifest.PackType, Pack.Factory> packFactories = new EnumMap<>(PackManifest.PackType.class);

  /**
   * the packs info packet.
   */
  private final AtomicReference<PackInfoPacket> packInfo = new AtomicReference<>(new PackInfoPacket());

  /**
   * the pack stack packet.
   */
  private final AtomicReference<PackStackPacket> packStack = new AtomicReference<>(new PackStackPacket());

  /**
   * the packs.
   */
  private final Map<String, Pack> packs = new Object2ObjectOpenHashMap<>();

  /**
   * the packs by id.
   */
  private final Map<UUID, Pack> packsById = new Object2ObjectOpenHashMap<>();

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
    this.packInfo.set(new PackInfoPacket(Collections.emptyList(),
      mustAccept,
      new ObjectArrayList<>(this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PackInfoPacket.Entry(
            "",
            "",
            pack.getId().toString(), pack.getSize(),
            pack.getVersion().toString(),
            false,
            false,
            ""))
        .collect(Collectors.toList())),
      false));
    this.packStack.set(new PackStackPacket(
      Collections.emptyList(),
      Collections.emptyList(),
      true,
      mustAccept,
      "*",
      this.packs.values().stream()
        .filter(pack -> pack.getType() != ResourcePackType.BEHAVIOR)
        .map(pack ->
          new PackStackPacket.Entry(pack.getId().toString(), pack.getVersion().toString(), ""))
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
  public Optional<Pack> getPack(@NotNull final String id) {
    return Optional.ofNullable(this.packs.get(id));
  }

  @NotNull
  @Override
  public Optional<Pack> getPackByUniqueId(@NotNull final UUID uniqueId) {
    return Optional.ofNullable(this.packsById.get(uniqueId));
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

  @NotNull
  @Override
  public Map<String, Pack> getPacks() {
    return Collections.unmodifiableMap(this.packs);
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
    this.putPack(manifest, loader, factory, module);
  }

  @Override
  public void loadPacks(@NotNull final Path directory) {
    this.checkClosed();
    Preconditions.checkArgument(Files.isDirectory(directory), "%s is not a directory", directory);
    final var loaders = new ObjectArrayList<PackLoader>();
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
    final var manifestMap = new Object2ObjectOpenHashMap<UUID, PackManifest>();
    final var loaderMap = new Object2ObjectOpenHashMap<UUID, PackLoader>();
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
    final var missingDependencies = new ObjectArrayList<PackManifest>();
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
      this.putPack(manifest, loader, factory, module);
    }
    SimplePackManager.LOGGER.debug(TranslatedText.get("shiruka.pack.pack_manager.load_packs.success",
      manifestMap.size()));
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
   * reloads packs.
   */
  public void reloadPacks() {
    Shiruka.getLogger().debug("§7Reloading packs.");
    this.registerLoader(RplZip.class, RplZip.FACTORY);
    this.registerLoader(RplDirectory.class, RplDirectory.FACTORY);
    this.registerPack(PackManifest.PackType.RESOURCES, ResourcePack.FACTORY);
    if (Files.notExists(SimplePackManager.PACKS_PATH)) {
      try {
        Files.createDirectory(SimplePackManager.PACKS_PATH);
      } catch (final IOException e) {
        throw new IllegalStateException("Unable to create packs directory");
      }
    }
    this.loadPacks(SimplePackManager.PACKS_PATH);
    this.closeRegistration();
  }

  /**
   * checks if the resource pack manager closed.
   *
   * @throws IllegalStateException if {@link #closed} is true.
   */
  private void checkClosed() {
    Preconditions.checkState(!this.closed, "PackManager registration is closed!");
  }

  /**
   * puts the given manifest into the {@link #packs} and {@link #packsById}.
   *
   * @param manifest the manifest to put.
   * @param loader the loader to put.
   * @param factory the factory to put.
   * @param module the module to put.
   */
  private void putPack(@NotNull final PackManifest manifest, @NotNull final PackLoader loader, @NotNull final Pack.Factory factory,
                       @NotNull final PackManifest.Module module) {
    final var uuid = manifest.getHeader().getUuid();
    final var pack = factory.create(loader, manifest, module);
    this.packs.put(uuid + "_" + manifest.getHeader().getVersion(), pack);
    this.packsById.put(uuid, pack);
    loader.getPreparedFile();
  }
}
