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

package io.github.shiruka.shiruka.config;

import static io.github.shiruka.api.config.Paths.*;
import io.github.shiruka.api.config.Config;
import io.github.shiruka.api.config.ConfigPath;
import io.github.shiruka.api.config.config.PathableConfig;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * this class contains the constant values whenever the server loads the properties file in order to shortcut access to
 * each of the values.
 */
public final class ServerConfig extends PathableConfig {

  /**
   * server's ip address.
   */
  public static final ConfigPath<String> ADDRESS_IP = commented(stringPath(
    "ip", "127.0.0.1"),
    "server's ip address.");

  /**
   * server's port.
   */
  public static final ConfigPath<Integer> ADDRESS_PORT = commented(integerPath(
    "port", 19132),
    "server's port.");

  /**
   * the amount of bytes before compressing packets.
   * <p>
   * use -1 to disable.
   */
  public static final ConfigPath<Integer> COMPRESSION_THRESHOLD = commented(integerPath(
    "compression-threshold", 256),
    "the amount of bytes before compressing packets.\n" + "use -1 to disable.");

  /**
   * name of the over world.
   */
  public static final ConfigPath<String> DEFAULT_WORLD_NAME = commented(stringPath(
    "default-world-name", "world"),
    "name of the over world.");

  /**
   * the description message shown in the server list.
   */
  public static final ConfigPath<String> DESCRIPTION = commented(stringPath(
    "description", "Shiru ka server"),
    "the description message shown in the server list.");

  /**
   * forces to accept incoming resource packs.
   */
  public static final ConfigPath<Boolean> FORCE_RESOURCES = commented(booleanPath(
    "force-resources", true),
    "forces to accept resource packs to players.");

  /**
   * the loaded languages.
   */
  public static final ConfigPath<List<String>> LOADED_LANGUAGES = commented(listStringPath(
    "loaded-languages", "en_US"),
    "loaded languages of the Shiru ka.");

  /**
   * the max players that can be online at once.
   */
  public static final ConfigPath<Integer> MAX_PLAYERS = commented(integerPath(
    "max-players", 20),
    "the max players that can be online at once.");

  /**
   * whether to check for netty memory leaks during runtime
   */
  public static final ConfigPath<Boolean> NETTY_LEAK_DETECTOR = commented(booleanPath(
    "netty-leak-detector", false),
    "whether to check for netty memory leaks during runtime");

  /**
   * whether to use Mojang auth to check players.
   */
  public static final ConfigPath<Boolean> ONLINE_MODE = commented(booleanPath(
    "online-mode", true),
    "whether to use Mojang auth to check players.");

  /**
   * the server language.
   */
  public static final ConfigPath<Locale> SERVER_LANGUAGE = commented(localePath(
    "server-language", Locale.ROOT),
    "language of the Shiru ka.");

  /**
   * "true" to use linux natives when available.
   */
  public static final ConfigPath<Boolean> USE_NATIVE = commented(booleanPath(
    "use-native", true),
    "\"true\" to use linux natives when available.");

  /**
   * ctor.
   *
   * @param origin the origin.
   */
  private ServerConfig(@NotNull final Config origin) {
    super(origin);
  }

  /**
   * obtains the server config instance.
   *
   * @return config instance.
   */
  @NotNull
  public static Optional<Config> get() {
    return ServerConfig.ADDRESS_IP.getConfig();
  }

  /**
   * initiates the server config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    Config.fromFile(file)
      .map(ServerConfig::new)
      .ifPresent(Config::save);
  }
}
