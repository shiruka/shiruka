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

package io.github.shiruka.shiruka.network;

import org.jetbrains.annotations.NotNull;

/**
 * an enum class to determine disconnect reason for {@link Connection}s.
 */
public enum DisconnectReason {
  /**
   * the closed by remote peer.
   */
  CLOSED_BY_REMOTE_PEER("CLOSED_BY_REMOTE_PEER"),
  /**
   * the shutting down.
   */
  SHUTTING_DOWN("SHUTTING_DOWN"),
  /**
   * the disconnected.
   */
  DISCONNECTED("DISCONNECTED"),
  /**
   * the timed out.
   */
  TIMED_OUT("TIMED_OUT"),
  /**
   * the connection request failed.
   */
  CONNECTION_REQUEST_FAILED("CONNECTION_REQUEST_FAILED"),
  /**
   * the already connected.
   */
  ALREADY_CONNECTED("ALREADY_CONNECTED"),
  /**
   * the no free incoming connections.
   */
  NO_FREE_INCOMING_CONNECTIONS("NO_FREE_INCOMING_CONNECTIONS"),
  /**
   * the incompatible protocol version.
   */
  INCOMPATIBLE_PROTOCOL_VERSION("INCOMPATIBLE_PROTOCOL_VERSION"),
  /**
   * the ip recently connected.
   */
  IP_RECENTLY_CONNECTED("IP_RECENTLY_CONNECTED"),
  /**
   * the bad packet.
   */
  BAD_PACKET("BAD_PACKET");

  /**
   * the message.
   */
  @NotNull
  private final String message;

  /**
   * ctor.
   *
   * @param message the message.
   */
  DisconnectReason(@NotNull final String message) {
    this.message = message;
  }

  /**
   * obtains the message.
   *
   * @return message.
   */
  @NotNull
  public String getMessage() {
    return this.message;
  }
}
