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

package net.shiruka.shiruka.text;

import net.shiruka.api.text.TranslatedText;

/**
 * a class that contains {@link TranslatedText} constants.
 */
public final class TranslatedTexts {

  /**
   * the add confirm.
   */
  public static final TranslatedText ADD_CONFIRM = TranslatedText.get("shiruka.command.stop_command.add_confirm");

  /**
   * the banned reason.
   */
  public static final TranslatedText ALREADY_LOGGED_IN_REASON =
    TranslatedText.get("shiruka.player.already_logged_in");

  /**
   * the banned reason.
   */
  public static final TranslatedText BANNED_REASON = TranslatedText.get("shiruka.player.banned");

  /**
   * the disconnected with no reason.
   */
  public static final TranslatedText DISCONNECTED_NO_REASON = TranslatedText.get("disconnect.disconnected");

  /**
   * the enabling plugins before worlds.
   */
  public static final TranslatedText ENABLING_PLUGINS_BEFORE_WORLDS =
    TranslatedText.get("shiruka.server.enabling_plugin");

  /**
   * the invalid name reason.
   */
  public static final TranslatedText INVALID_NAME_REASON = TranslatedText.get("disconnectionScreen.invalidName");

  /**
   * the invalid skin reason.
   */
  public static final TranslatedText INVALID_SKIN_REASON = TranslatedText.get("disconnectionScreen.invalidSkin");

  /**
   * the loading plugins.
   */
  public static final TranslatedText LOADING_PLUGINS = TranslatedText.get("shiruka.server.loading_plugins");

  /**
   * the login error.
   */
  public static final TranslatedText LOGIN_ERROR = TranslatedText.get("shiruka.server.login_error");

  /**
   * the not authenticated reason.
   */
  public static final TranslatedText NOT_AUTHENTICATED_REASON =
    TranslatedText.get("disconnectionScreen.notAuthenticated");

  /**
   * the no reason.
   */
  public static final TranslatedText NO_REASON = TranslatedText.get("disconnectionScreen.noReason");

  /**
   * the resource pack reason.
   */
  public static final TranslatedText RESOURCE_PACK_REASON = TranslatedText.get("disconnectionScreen.resourcePack");

  /**
   * the restart reason.
   */
  public static final TranslatedText RESTART_REASON = TranslatedText.get("shiruka.connection.restart_message");

  /**
   * the server full reason.
   */
  public static final TranslatedText SERVER_FULL_REASON = TranslatedText.get("disconnectionScreen.serverFull");

  /**
   * the server starting.
   */
  public static final TranslatedText SERVER_STARTING = TranslatedText.get("shiruka.server.starting");

  /**
   * the slot login reason.
   */
  public static final TranslatedText SLOW_LOGIN_REASON = TranslatedText.get("shiruka.connection.slow_login");

  /**
   * the whitelist on reason.
   */
  public static final TranslatedText WHITELIST_ON_REASON = TranslatedText.get("shiruka.player.whitelist.on");

  /**
   * ctor.
   */
  private TranslatedTexts() {
  }
}
