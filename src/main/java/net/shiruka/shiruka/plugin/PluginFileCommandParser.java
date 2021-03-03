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

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import net.shiruka.api.Shiruka;
import net.shiruka.api.command.Commands;
import net.shiruka.api.command.builder.LiteralBuilder;
import net.shiruka.api.plugin.Plugin;
import net.shiruka.api.text.Text;
import net.shiruka.api.text.TranslatedText;
import org.jetbrains.annotations.NotNull;

/**
 * a utility class to helps the parsing commands in the plugin files.
 */
@UtilityClass
public class PluginFileCommandParser {

  /**
   * the permission key.
   */
  private String PERMISSIONS = "shiruka.command.permission";

  /**
   * parses the given commands of the given plugin's file.
   *
   * @param plugin the plugin to parse.
   *
   * @return parsed plugin's commands.
   *
   * @todo #1:15m Add language support when parsing the commands section of plugin's file.
   */
  @NotNull
  public List<LiteralBuilder> parse(@NotNull final Plugin plugin) {
    final var builders = new ObjectArrayList<LiteralBuilder>();
    final var map = plugin.getDescription().getCommands();
    map.forEach((key, value) -> {
      if (key.contains(":")) {
        Shiruka.getLogger().fatal("Could not load command {} for plugin {}: Illegal Characters",
          key, plugin.getName());
        return;
      }
      final var builder = Commands.literal(key);
      final var description = value.get("description");
      final var usage = value.get("usage");
      final var aliases = value.get("aliases");
      final var permission = value.get("permission");
      final var permissionMessage = value.get("permission-message");
      if (description != null) {
        builder.describe(description.toString());
      }
      if (usage != null) {
        builder.usage(usage.toString());
      }
      if (aliases != null) {
        final var aliasList = new ObjectArrayList<String>();
        if (aliases instanceof List<?>) {
          ((List<?>) aliases).stream()
            .filter(o -> {
              if (!o.toString().contains(":")) {
                return true;
              }
              Shiruka.getLogger().fatal("Could not load alias {} for plugin {}: Illegal Characters",
                o.toString(), plugin.getName());
              return false;
            })
            .forEach(o -> aliasList.add(o.toString()));
        } else {
          if (aliases.toString().contains(":")) {
            Shiruka.getLogger().fatal("Could not load alias {} for plugin {}: Illegal Characters",
              aliases.toString(), plugin.getName());
          } else {
            aliasList.add(aliases.toString());
          }
        }
        builder.aliases(aliasList.toArray(String[]::new));
      }
      if (permission != null) {
        builder.permission((sender, permissions) -> {
          final var joined = Joiner.on(", ").join(permissions);
          final Text error;
          if (permissionMessage != null) {
            error = permissionMessage::toString;
          } else {
            error = TranslatedText.get(PluginFileCommandParser.PERMISSIONS, joined);
          }
          sender.sendMessage(error);
        }, permission.toString());
      }
      builders.add(builder);
    });
    return builders;
  }
}
