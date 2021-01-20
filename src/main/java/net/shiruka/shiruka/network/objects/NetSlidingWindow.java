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

package net.shiruka.shiruka.network.objects;

import net.shiruka.shiruka.network.util.Constants;

/**
 * processor for ACK and NACK packets.
 */
public final class NetSlidingWindow {

  /**
   * the mtu size.
   */
  private final int mtu;

  /**
   * the backoff this block.
   */
  private boolean backoffThisBlock;

  /**
   * the congestion window.
   */
  private double congestionWindow;

  /**
   * the deviation RTT.
   */
  private double deviationRTT = -1;

  /**
   * the estimated RTT.
   */
  private double estimatedRTT = -1;

  /**
   * the latest rtt.
   */
  private double lastRTT = -1;

  /**
   * the next congestion control block.
   */
  private long nextCongestionControlBlock;

  /**
   * the oldest un sent ACK packet.
   */
  private long oldestUnsentAck;

  /**
   * the ss thresh.
   */
  private double ssThresh;

  /**
   * ctor.
   *
   * @param mtu the mtu size.
   */
  public NetSlidingWindow(final int mtu) {
    this.mtu = mtu;
    this.congestionWindow = mtu;
  }

  /**
   * obtains the rto for retransmission.
   *
   * @return the rto for retransmission.
   */
  public long getRtoForRetransmission() {
    if (this.estimatedRTT == -1) {
      return Constants.CC_MAXIMUM_THRESHOLD;
    }
    final var threshold = (long) (2.0D * this.estimatedRTT + 4.0D * this.deviationRTT + Constants.CC_ADDITIONAL_VARIANCE);
    return Math.min(threshold, Constants.CC_MAXIMUM_THRESHOLD);
  }

  /**
   * calculates the transmission bandwidth.
   *
   * @param unACKedBytes the bytes to calculate
   *
   * @return the transmission bandwidth.
   */
  public int getTransmissionBandwidth(final int unACKedBytes) {
    if (unACKedBytes <= this.congestionWindow) {
      return (int) (this.congestionWindow - unACKedBytes);
    }
    return 0;
  }

  /**
   * handles the packet with the given parameters.
   *
   * @param rtt the rtt to handle.
   * @param sequenceIndex the sequence index to handle
   * @param curSequenceIndex the current sequence index to handle.
   */
  public void onACK(final long rtt, final int sequenceIndex, final int curSequenceIndex) {
    this.lastRTT = rtt;
    if (this.estimatedRTT == -1) {
      this.estimatedRTT = rtt;
      this.deviationRTT = rtt;
    } else {
      final var difference = rtt - this.estimatedRTT;
      this.estimatedRTT += 0.5D * difference;
      this.deviationRTT += 0.5 * (Math.abs(difference) - this.deviationRTT);
    }
    final var isNewCongestionControlPeriod = sequenceIndex > this.nextCongestionControlBlock;
    if (isNewCongestionControlPeriod) {
      this.backoffThisBlock = false;
      this.nextCongestionControlBlock = curSequenceIndex;
    }
    if (this.isInSlowStart()) {
      this.congestionWindow += this.mtu;
      if (this.congestionWindow > this.ssThresh && this.ssThresh != 0) {
        this.congestionWindow = this.ssThresh + this.mtu * this.mtu / this.congestionWindow;
      }
    } else if (isNewCongestionControlPeriod) {
      this.congestionWindow += this.mtu * this.mtu / this.congestionWindow;
    }
  }

  /**
   * handles the NACK packets
   */
  public void onNACK() {
    if (!this.backoffThisBlock) {
      this.ssThresh = this.congestionWindow / 2D;
    }
  }

  /**
   * sets latest incoming ACK packet.
   *
   * @param curTime the time to set
   */
  public void onPacketReceived(final long curTime) {
    if (this.oldestUnsentAck == 0) {
      this.oldestUnsentAck = curTime;
    }
  }

  /**
   * runs when resends.
   *
   * @param curSequenceIndex the current sequence index to resend.
   */
  public void onResend(final long curSequenceIndex) {
    if (this.backoffThisBlock || !(this.congestionWindow > this.mtu * 2)) {
      return;
    }
    this.ssThresh = this.congestionWindow / 2;
    if (this.ssThresh < this.mtu) {
      this.ssThresh = this.mtu;
    }
    this.congestionWindow = this.mtu;
    this.nextCongestionControlBlock = curSequenceIndex;
    this.backoffThisBlock = true;
  }

  /**
   * runs when ACK packet sends.
   */
  public void onSendACK() {
    this.oldestUnsentAck = 0;
  }

  /**
   * checks if it should send the ACK packets.
   *
   * @param curTime the packet to check
   *
   * @return return true if it should send the ACK packets.
   */
  public boolean shouldSendACKs(final long curTime) {
    final long rto = this.getSenderRtoForAck();
    return rto == -1 || curTime >= this.oldestUnsentAck + Constants.CC_SYN;
  }

  /**
   * obtains sender rto for ACK packets.
   *
   * @return tro for ACK packets.
   */
  private long getSenderRtoForAck() {
    if (this.lastRTT == -1) {
      return -1;
    }
    return (long) (this.lastRTT + Constants.CC_SYN);
  }

  /**
   * checks if packet is in the slow start.
   *
   * @return return true if the packet is in the slow start.
   */
  private boolean isInSlowStart() {
    return this.congestionWindow <= this.ssThresh || this.ssThresh == 0;
  }
}
