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

package io.github.shiruka.shiruka.network.packets;

import io.github.shiruka.api.Shiruka;
import io.github.shiruka.api.text.ChatColor;
import io.github.shiruka.shiruka.config.ServerConfig;
import io.github.shiruka.shiruka.event.SimpleChainData;
import io.github.shiruka.shiruka.event.SimpleLoginData;
import io.github.shiruka.shiruka.misc.VarInts;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.network.packet.PacketIn;
import io.github.shiruka.shiruka.network.packet.PacketOut;
import io.github.shiruka.shiruka.network.util.Constants;
import io.github.shiruka.shiruka.network.util.Packets;
import io.github.shiruka.shiruka.scheduler.AsyncTask;
import io.netty.buffer.ByteBuf;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that sends by clients to request a login process.
 */
public final class PacketInLogin extends PacketIn {

  /**
   * the name pattern to check client's usernames.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z\\s\\d_]{3,16}+$");

  /**
   * ctor.
   */
  public PacketInLogin() {
    super(PacketInLogin.class);
  }

  /**
   * @todo #1:60m Add Server_To_Client_Handshake Client_To_Server_Handshake packets to request encryption key.
   */
  @Override
  public void read(@NotNull final ByteBuf buf, @NotNull final PlayerConnection connection) {
    final var protocolVersion = buf.readInt();
    final var jwt = buf.readSlice(VarInts.readUnsignedVarInt(buf));
    final var encodedChainData = Packets.readLEAsciiString(jwt).toString();
    final var encodedSkinData = Packets.readLEAsciiString(jwt).toString();
    if (protocolVersion < Constants.MINECRAFT_PROTOCOL_VERSION) {
      connection.sendPacket(new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_CLIENT_OLD));
      return;
    }
    if (protocolVersion > Constants.MINECRAFT_PROTOCOL_VERSION) {
      connection.sendPacket(new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_FAILED_SERVER_OLD));
      return;
    }
    connection.getServer().getSchedulerService().execute(() -> {
      final var chainData = SimpleChainData.create(encodedChainData, encodedSkinData);
      Shiruka.getScheduler().schedule(() -> {
        if (!chainData.xboxAuthed() && ServerConfig.ONLINE_MODE.getValue().orElse(false)) {
          connection.disconnect("disconnectionScreen.notAuthenticated");
          return;
        }
        final var username = chainData.username();
        final var matcher = PacketInLogin.NAME_PATTERN.matcher(username);
        if (!matcher.matches() ||
          username.equalsIgnoreCase("rcon") ||
          username.equalsIgnoreCase("console")) {
          connection.disconnect("disconnectionScreen.invalidName");
          return;
        }
        if (!chainData.skin().isValid()) {
          connection.disconnect("disconnectionScreen.invalidSkin");
          return;
        }
        final var loginData = new SimpleLoginData(chainData, connection, ChatColor.clean(username));
        connection.setLatestLoginData(loginData);
        final var preLogin = Shiruka.getEventManager().playerPreLogin(loginData, "Some reason.");
        preLogin.callEvent();
        if (preLogin.cancelled()) {
          connection.disconnect(preLogin.kickMessage());
          return;
        }
        connection.setState(PlayerConnection.State.STATUS);
        final var asyncLogin = Shiruka.getEventManager().playerAsyncLogin(loginData);
        loginData.setAsyncLogin(asyncLogin);
        final var process = (AsyncTask) Shiruka.getScheduler().scheduleAsync(asyncLogin::callEvent);
        process.onComplete(() -> {
          if (loginData.shouldLogin()) {
            loginData.initializePlayer();
          }
        });
        loginData.setAsyncProcess(process);
        connection.sendPacket(new PacketOutPlayStatus(PacketOutPlayStatus.Status.LOGIN_SUCCESS));
        final var packInfo = Shiruka.getPackManager().getPackInfo();
        if (packInfo instanceof PacketOut) {
          connection.sendPacket((PacketOut) packInfo);
        }
      });
    });
  }
}
