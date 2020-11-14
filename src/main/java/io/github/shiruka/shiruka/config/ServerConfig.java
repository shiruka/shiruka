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

package io.github.shiruka.shiruka.config;

import static io.github.shiruka.api.conf.Paths.*;
import io.github.shiruka.api.conf.CommentablePath;
import io.github.shiruka.api.conf.config.PathableConfig;
import io.github.shiruka.api.conf.config.YamlConfig;
import java.io.File;
import org.jetbrains.annotations.NotNull;

/**
 * this class contains the constant values whenever the
 * server loads the properties file in order to shortcut
 * access to each of the values.
 */
public final class ServerConfig extends PathableConfig {

  /**
   * server's ip address.
   */
  public static final CommentablePath<String> ADDRESS =
    commented(stringPath("address", "0.0.0.0"));

  /**
   * server's port.
   */
  public static final CommentablePath<Integer> PORT =
    commented(integerPath("port", 19132));

  /**
   * "true" to use linux natives when available.
   */
  public static final CommentablePath<Boolean> USE_NATIVE =
    commented(booleanPath("use-native", true));

  /**
   * the amount of bytes before compressing packets.
   * <p>
   * use -1 to disable.
   */
  public static final CommentablePath<Integer> COMPRESSION_THRESHOLD =
    commented(integerPath("compression-threshold", 256));

  /**
   * whether to use Mojang auth to check players.
   */
  public static final CommentablePath<Boolean> ONLINE_MODE =
    commented(booleanPath("online-mode", true));

  /**
   * the max players that can be online at once.
   */
  public static final CommentablePath<Integer> MAX_PLAYERS =
    commented(integerPath("max-players", 20));

  /**
   * the MOTD message shown in the server list.
   */
  public static final CommentablePath<String> MOTD =
    commented(stringPath("motd", "Shiru ka server"));

  /**
   * whether to check for netty memory leaks during runtime
   */
  public static final CommentablePath<Boolean> NETTY_LEAK_DETECTOR =
    commented(booleanPath("netty-leak-detector", false));

  /**
   * ctor.
   *
   * @param file the file.
   */
  private ServerConfig(@NotNull final File file) {
    super(new YamlConfig(file));
  }

  /**
   * initiates the server config to the given file.
   *
   * @param file the file to create.
   */
  public static void init(@NotNull final File file) {
    new ServerConfig(file);
  }
}
