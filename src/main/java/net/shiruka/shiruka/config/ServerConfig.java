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
import static io.github.portlek.configs.util.Paths.comment;
import static io.github.portlek.configs.util.Paths.integer;
import static io.github.portlek.configs.util.Paths.locale;
import static io.github.portlek.configs.util.Paths.string;
import static io.github.portlek.configs.util.Paths.stringList;
import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.paths.comment.BooleanCommentPath;
import io.github.portlek.configs.paths.comment.IntegerCommentPath;
import io.github.portlek.configs.paths.comment.LocaleCommentPath;
import io.github.portlek.configs.paths.comment.StringCommentPath;
import io.github.portlek.configs.paths.comment.StringListCommentPath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Locale;

/**
 * this class contains the constant values whenever the server loads the properties file in order to shortcut access to
 * each of the values.
 */
public final class ServerConfig implements ConfigHolder {

  /**
   * server's ip address.
   */
  public static final StringCommentPath ADDRESS_IP = comment(string(
    "ip", "127.0.0.1"),
    "server's ip address.");

  /**
   * the amount of bytes before compressing packets.
   * <p>
   * use -1 to disable.
   */
  public static final IntegerCommentPath COMPRESSION_THRESHOLD = comment(integer(
    "compression-threshold", 256),
    "the amount of bytes before compressing packets.\n" + "use -1 to disable.");

  /**
   * name of the over world.
   */
  public static final StringCommentPath DEFAULT_WORLD_NAME = comment(string(
    "default-world-name", "world"),
    "name of the over world.");

  /**
   * the game mode.
   */
  public static final StringCommentPath DESCRIPTION_GAME_MODE = comment(string(
    "description.game-mode", "survival"),
    "Shiru ka server's game mode.");

  /**
   * the max players that can be online at once.
   */
  public static final IntegerCommentPath DESCRIPTION_MAX_PLAYERS = comment(integer(
    "description.max-players", 20),
    "the max players that can be online at once.");

  /**
   * the description message shown in the server list.
   */
  public static final StringCommentPath DESCRIPTION_MOTD = comment(string(
    "description.motd", "Shiru ka server"),
    "the description message shown in the server list.");

  /**
   * forces to accept incoming resource packs.
   */
  public static final BooleanCommentPath FORCE_RESOURCES = comment(bool(
    "force-resources", true),
    "forces to accept resource packs to players.");

  /**
   * the loaded languages.
   */
  public static final StringListCommentPath LOADED_LANGUAGES = comment(stringList(
    "loaded-languages", new ObjectArrayList<>()),
    "loaded languages of the Shiru ka. (will update automatically)");

  /**
   * the maximum login handling per tick.
   */
  public static final IntegerCommentPath MAX_LOGIN_PER_TICK = comment(integer(
    "max-login-per-tick", 1),
    "determines the maximum login process, that will handle, per tick.");

  /**
   * whether to use Mojang auth to check players.
   */
  public static final BooleanCommentPath ONLINE_MODE = comment(bool(
    "online-mode", true),
    "whether to use Mojang auth to check players.");

  /**
   * ops pass player limit to join the server.
   */
  public static final BooleanCommentPath OPS_PASS_PLAYER_LIMIT = comment(bool(
    "ops-pass-player-limit", true),
    "Ops will able to join the server which is full.");

  /**
   * server's ipv4 port.
   */
  public static final IntegerCommentPath PORT = comment(integer(
    "port", 19132),
    "server's port.");

  /**
   * saves the user cache on stop only.
   */
  public static final BooleanCommentPath SAVE_USER_CACHE_ON_STOP_ONLY = comment(bool(
    "save-user-cache-on-stop-only", false),
    "saves the user cache on stop only.");

  /**
   * the server language.
   */
  public static final LocaleCommentPath SERVER_LANGUAGE = comment(locale(
    "server-language", Locale.ROOT),
    "language of the Shiru ka.");

  /**
   * the timings server name.
   */
  public static final StringCommentPath TIMINGS_SERVER_NAME = comment(string(
    "timings.server-name", "Unknown Server"),
    "the timings server name.");

  /**
   * "true" to use linux natives when available.
   */
  public static final BooleanCommentPath USE_NATIVE = comment(bool(
    "use-native", true),
    "\"true\" to use linux natives when available.");

  /**
   * shows a warning message when server overloads.
   */
  public static final BooleanCommentPath WARN_ON_OVERLOAD = comment(bool(
    "warn-on-overload", true),
    "shows warning when server overloads.");

  /**
   * the white list to prevent join random players.
   */
  public static final BooleanCommentPath WHITE_LIST = comment(bool(
    "white-list", false),
    "if its \"true\" players that are not in the white list can't join the server.");

  /**
   * ctor.
   */
  private ServerConfig() {
  }
}
