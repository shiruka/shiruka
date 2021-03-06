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

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import io.github.portlek.configs.annotation.Comment;
import io.github.portlek.configs.annotation.Route;
import io.github.portlek.configs.configuration.ConfigurationSection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * this class contains the constant values whenever the server loads the properties file in order to shortcut access to
 * each of the values.
 */
public final class ServerConfig implements ConfigHolder {

  /**
   * the server language.
   */
  @Comment("language of the Shiru ka.")
  @Route("server-language")
  public static Locale serverLanguage = Locale.ROOT;

  /**
   * the amount of bytes before compressing packets.
   * <p>
   * use -1 to disable.
   */
  @Comment("the amount of bytes before compressing packets.\n" + "use -1 to disable.")
  @Route("compression-threshold")
  public static int compressionThreshold = 256;

  /**
   * name of the over world.
   */
  @Comment("name of the over world.")
  @Route("default-world-name")
  public static String defaultWorldName = "world";

  /**
   * the description settings.
   */
  public static Description description;

  /**
   * forces to accept incoming resource packs.
   */
  @Comment("forces to accept resource packs to players.")
  @Route("force-resources")
  public static boolean forceResources = false;

  /**
   * server's ip address.
   */
  @Comment("server's ip address.")
  @Route("ip")
  public static String ip = "127.0.0.1";

  /**
   * the loaded languages.
   */
  @Comment("loaded languages of the Shiru ka. (will update automatically)")
  @Route("loaded-languages")
  public static List<String> loadedLanguages = new ObjectArrayList<>();

  /**
   * the maximum login handling per tick.
   */
  @Comment("determines the maximum login process, that will handle, per tick.")
  @Route("max-login-per-tick")
  public static int maxLoginPerTick = 1;

  /**
   * whether to use Mojang auth to check players.
   */
  @Comment("whether to use Mojang auth to check players.")
  @Route("online-mode")
  public static boolean onlineMode = true;

  /**
   * ops pass player limit to join the server.
   */
  @Comment("Ops will able to join the server which is full.")
  @Route("ops-pass-player-limit")
  public static boolean opsPassPlayerLimit = true;

  /**
   * server's ipv4 port.
   */
  @Comment("server's port.")
  @Route("port")
  public static int port = 19132;

  /**
   * saves the user cache on stop only.
   */
  @Comment("saves the user cache on stop only.")
  @Route("save-user-cache-on-stop-only")
  public static boolean saveUserCacheOnStopOnly = false;

  /**
   * the section.
   */
  public static ConfigurationSection section;

  /**
   * "true" to use linux natives when available.
   */
  @Comment("\"true\" to use linux natives when available.")
  @Route("use-native")
  public static boolean useNative = true;

  /**
   * shows a warning message when server overloads.
   */
  @Comment("shows warning when server overloads.")
  @Route("warn-on-overload")
  public static boolean warnOnOverload = true;

  /**
   * the white list to prevent join random players.
   */
  @Comment("if its \"true\" players that are not in the white list can't join the server.")
  @Route("white-list")
  public static boolean whiteList = false;

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

  /**
   * a class that represents settings for description of the server.
   */
  public static final class Description implements ConfigHolder {

    /**
     * the game mode.
     */
    @Comment("Shiru ka server's game mode.")
    @Route("game-mode")
    public static String gameMode = "survival";

    /**
     * the max players that can be online at once.
     */
    @Comment("the max players that can be online at once.")
    @Route("max-players")
    public static int maxPlayers = 20;

    /**
     * the description message shown in the server list.
     */
    @Comment("the description message shown in the server list.")
    @Route("motd")
    public static String motd = "Shiru ka server";
  }
}
