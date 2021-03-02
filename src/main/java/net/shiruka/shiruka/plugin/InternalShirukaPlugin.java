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

package net.shiruka.shiruka.plugin;

import io.github.portlek.configs.tree.FileConfiguration;
import java.io.File;
import java.io.InputStream;
import net.shiruka.api.plugin.InvalidDescriptionException;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.plugin.PluginDescriptionFile;
import net.shiruka.api.plugin.PluginLoader;
import net.shiruka.shiruka.ShirukaServer;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents internal plugin for Shiru ka.
 */
public final class InternalShirukaPlugin implements Plugin {

  /**
   * the plugin description file.
   */
  @NotNull
  private final PluginDescriptionFile pluginDescriptionFile;

  /**
   * the enabled.
   */
  private boolean enabled = true;

  /**
   * ctor.
   */
  public InternalShirukaPlugin() {
    try {
      this.pluginDescriptionFile = PluginDescriptionFile.init("Shiru ka", ShirukaServer.VERSION, "shiruka");
    } catch (final InvalidDescriptionException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public FileConfiguration getConfig() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public File getDataFolder() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public PluginDescriptionFile getDescription() {
    return this.pluginDescriptionFile;
  }

  @NotNull
  @Override
  public Logger getLogger() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public PluginLoader getPluginLoader() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public InputStream getResource(@NotNull final String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * sets the enabled.
   *
   * @param enabled the enabled to set.
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean isSusceptible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSusceptible(final boolean canSuspect) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void onDisable() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void onEnable() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void onLoad() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveResource(@NotNull final String resourcePath, final boolean replace) {
    throw new UnsupportedOperationException();
  }
}
