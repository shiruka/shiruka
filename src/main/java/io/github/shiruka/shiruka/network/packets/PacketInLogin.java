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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.chat.ChatColor;
import io.github.shiruka.api.events.LoginResultEvent;
import io.github.shiruka.shiruka.concurrent.PoolSpec;
import io.github.shiruka.shiruka.concurrent.ServerThreadPool;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.entity.ShirukaPlayer;
import io.github.shiruka.shiruka.event.SimpleChainData;
import io.github.shiruka.shiruka.event.SimpleLoginData;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.PacketPriority;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Packets;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that sends by clients to request a login process.
 */
public final class PacketInLogin extends PacketIn {

  /**
   * the name pattern to check client's usernames.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[aA-zZ\\s\\d_]{3,16}+$");

  /**
   * ctor.
   */
  public PacketInLogin() {
    super(PacketInLogin.class);
  }

  @Override
  public void read(@NotNull final ByteBuf buf, @NotNull final ShirukaPlayer player) {
    final var protocolVersion = buf.readInt();
    final var jwt = buf.readSlice(VarInts.readUnsignedVarInt(buf));
    final var encodedChainData = Packets.readLEAsciiString(jwt).toString();
    final var encodedSkinData = Packets.readLEAsciiString(jwt).toString();
    if (protocolVersion < Constants.MINECRAFT_PROTOCOL_VERSION) {
      final var packet = new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_CLIENT_OLD);
      player.getPlayerConnection().sendPacket(packet, PacketPriority.IMMEDIATE);
    } else if (protocolVersion > Constants.MINECRAFT_PROTOCOL_VERSION) {
      final var packet = new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_SERVER_OLD);
      player.getPlayerConnection().sendPacket(packet, PacketPriority.IMMEDIATE);
      return;
    }
    final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
    if (!chainData.xboxAuthed() && ServerConfig.ONLINE_MODE.getValue().orElse(false)) {
      player.disconnect("disconnectionScreen.notAuthenticated");
      return;
    }
    final var username = chainData.username();
    final var matcher = PacketInLogin.NAME_PATTERN.matcher(username);
    if (!matcher.matches() ||
      username.equalsIgnoreCase("rcon") ||
      username.equalsIgnoreCase("console")) {
      player.disconnect("disconnectionScreen.invalidName");
      return;
    }
    if (!chainData.skin().isValid()) {
      player.disconnect("disconnectionScreen.invalidSkin");
      return;
    }
    final var loginData = new SimpleLoginData(chainData, player, ChatColor.clean(username));
    final var eventFactory = Shiruka.getEventFactory();
    final var preLogin = eventFactory.playerPreLogin(loginData, "Some reason.");
    eventFactory.call(preLogin);
    if (preLogin.cancelled()) {
      player.disconnect(preLogin.kickMessage());
      return;
    }
    player.getPlayerConnection().setState(PlayerConnection.State.STATUS);
    CompletableFuture.supplyAsync(() -> {
      final var event = eventFactory.playerAsyncLogin(loginData);
      eventFactory.call(event);
      return event;
    }, ServerThreadPool.forSpec(PoolSpec.PLAYERS))
      .thenAccept(event -> {
        if (player.getPlayerConnection().getConnection().isClosed()) {
          return;
        }
        if (event.loginResult() == LoginResultEvent.LoginResult.KICK) {
          player.disconnect(event.kickMessage());
          return;
        }
        if (loginData.shouldLogin()) {
          loginData.initializePlayer();
          event.objects().forEach(action ->
            action.accept(player));
        }
      });
    player.getPlayerConnection().sendPacket(new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_SUCCESS));
    // TODO player.getPlayerConnection().sendPacket(Shiruka.getServer().getResourcePackManager().getPacksInfos());
  }
}
