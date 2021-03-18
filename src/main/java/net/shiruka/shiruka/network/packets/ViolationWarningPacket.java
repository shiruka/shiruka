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

package net.shiruka.shiruka.network.packets;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.shiruka.shiruka.network.PacketHandler;
import net.shiruka.shiruka.network.ShirukaPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents violation warning packets.
 */
public final class ViolationWarningPacket extends ShirukaPacket {

  /**
   * the severities.
   */
  private static final PacketViolationSeverity[] SEVERITIES = PacketViolationSeverity.values();

  /**
   * the types.
   */
  private static final PacketViolationType[] TYPES = PacketViolationType.values();

  /**
   * the cause id.
   */
  private int causeId;

  /**
   * the context.
   */
  @Nullable
  private String context;

  /**
   * the severity.
   */
  @Nullable
  private PacketViolationSeverity severity;

  /**
   * the type.
   */
  @Nullable
  private PacketViolationType type;

  /**
   * ctor.
   *
   * @param original the original.
   */
  public ViolationWarningPacket(final @NotNull ByteBuf original) {
    super(ShirukaPacket.ID_VIOLATION_WARNING, original);
  }

  @Override
  public void decode() {
    // @todo #1:15 Implement violation warning packet.
  }

  @Override
  public void handle(@NotNull final PacketHandler handler) {
    handler.violationWarning(this);
  }

  /**
   * obtains the cause id.
   *
   * @return cause id.
   */
  public int getCauseId() {
    return this.causeId;
  }

  /**
   * obtains the context.
   *
   * @return context.
   */
  @NotNull
  public String getContext() {
    return Objects.requireNonNull(this.context);
  }

  /**
   * obtains the severity.
   *
   * @return severity.
   */
  @NotNull
  public PacketViolationSeverity getSeverity() {
    return Objects.requireNonNull(this.severity);
  }

  /**
   * obtains the type.
   *
   * @return type.
   */
  @NotNull
  public PacketViolationType getType() {
    return Objects.requireNonNull(this.type);
  }

  /**
   * an enum class that represents packet violation severities.
   */
  public enum PacketViolationSeverity {
    /**
     * the unknown.
     */
    UNKNOWN,
    /**
     * the warning.
     */
    WARNING,
    /**
     * the final warning.
     */
    FINAL_WARNING,
    /**
     * the terminating connection.
     */
    TERMINATING_CONNECTION
  }

  /**
   * an enum class that represents packet violation types.
   */
  public enum PacketViolationType {
    /**
     * the unknown.
     */
    UNKNOWN,
    /**
     * the malformed packet.
     */
    MALFORMED_PACKET
  }
}
