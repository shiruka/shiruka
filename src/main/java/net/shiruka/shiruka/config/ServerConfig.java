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

package net.shiruka.shiruka.config;

import static io.github.portlek.configs.util.Paths.bool;
import static io.github.portlek.configs.util.Paths.integer;
import static io.github.portlek.configs.util.Paths.locale;
import static io.github.portlek.configs.util.Paths.string;
import static io.github.portlek.configs.util.Paths.stringList;
import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import io.github.portlek.configs.annotation.Comment;
import io.github.portlek.configs.paths.def.BooleanDefaultPath;
import io.github.portlek.configs.paths.def.IntegerDefaultPath;
import io.github.portlek.configs.paths.def.LocaleDefaultPath;
import io.github.portlek.configs.paths.def.StringDefaultPath;
import io.github.portlek.configs.paths.def.StringListDefaultPath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Locale;

/**
 * this class contains the constant values whenever the server loads the properties file in order to shortcut access to
 * each of the values.
 */
public final class ServerConfig implements ConfigHolder {

  /**
   * server's ip address.
   */
  @Comment("server's ip address.")
  public static final StringDefaultPath ADDRESS_IP = string("ip", "127.0.0.1");

  /**
   * the amount of bytes before compressing packets.
   * <p>
   * use -1 to disable.
   */
  @Comment("the amount of bytes before compressing packets.\n" + "use -1 to disable.")
  public static final IntegerDefaultPath COMPRESSION_THRESHOLD = integer("compression-threshold", 256);

  /**
   * name of the over world.
   */
  @Comment("name of the over world.")
  public static final StringDefaultPath DEFAULT_WORLD_NAME = string("default-world-name", "world");

  /**
   * the game mode.
   */
  @Comment("Shiru ka server's game mode.")
  public static final StringDefaultPath DESCRIPTION_GAME_MODE = string("description.game-mode", "survival");

  /**
   * the max players that can be online at once.
   */
  @Comment("the max players that can be online at once.")
  public static final IntegerDefaultPath DESCRIPTION_MAX_PLAYERS = integer("description.max-players", 20);

  /**
   * the description message shown in the server list.
   */
  @Comment("the description message shown in the server list.")
  public static final StringDefaultPath DESCRIPTION_MOTD = string("description.motd", "Shiru ka server");

  /**
   * forces to accept incoming resource packs.
   */
  @Comment("forces to accept resource packs to players.")
  public static final BooleanDefaultPath FORCE_RESOURCES = bool("force-resources", true);

  /**
   * the loaded languages.
   */
  @Comment("loaded languages of the Shiru ka. (will update automatically)")
  public static final StringListDefaultPath LOADED_LANGUAGES = stringList("loaded-languages", new ObjectArrayList<>());

  /**
   * the maximum login handling per tick.
   */
  @Comment("determines the maximum login process, that will handle, per tick.")
  public static final IntegerDefaultPath MAX_LOGIN_PER_TICK = integer("max-login-per-tick", 1);

  /**
   * whether to use Mojang auth to check players.
   */
  @Comment("whether to use Mojang auth to check players.")
  public static final BooleanDefaultPath ONLINE_MODE = bool("online-mode", true);

  /**
   * ops pass player limit to join the server.
   */
  @Comment("Ops will able to join the server which is full.")
  public static final BooleanDefaultPath OPS_PASS_PLAYER_LIMIT = bool("ops-pass-player-limit", true);

  /**
   * server's ipv4 port.
   */
  @Comment("server's port.")
  public static final IntegerDefaultPath PORT = integer("port", 19132);

  /**
   * saves the user cache on stop only.
   */
  @Comment("saves the user cache on stop only.")
  public static final BooleanDefaultPath SAVE_USER_CACHE_ON_STOP_ONLY = bool("save-user-cache-on-stop-only", false);

  /**
   * the server language.
   */
  @Comment("language of the Shiru ka.")
  public static final LocaleDefaultPath SERVER_LANGUAGE = locale("server-language", Locale.ROOT);

  /**
   * the timings server name.
   */
  @Comment("the timings server name.")
  public static final StringDefaultPath TIMINGS_SERVER_NAME = string("timings.server-name", "Unknown Server");

  /**
   * "true" to use linux natives when available.
   */
  @Comment("\"true\" to use linux natives when available.")
  public static final BooleanDefaultPath USE_NATIVE = bool("use-native", true);

  /**
   * shows a warning message when server overloads.
   */
  @Comment("shows warning when server overloads.")
  public static final BooleanDefaultPath WARN_ON_OVERLOAD = bool("warn-on-overload", true);

  /**
   * the white list to prevent join random players.
   */
  @Comment("if its \"true\" players that are not in the white list can't join the server.")
  public static final BooleanDefaultPath WHITE_LIST = bool("white-list", false);

  /**
   * the loader.
   */
  private static ConfigLoader loader;

  /**
   * ctor.
   */
  private ServerConfig() {
  }

  /**
   * saves the file.
   */
  public static void save() {
    try {
      ServerConfig.loader.save();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
