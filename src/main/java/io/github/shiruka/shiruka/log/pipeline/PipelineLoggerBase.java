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
package io.github.shiruka.shiruka.log.pipeline;

import io.github.shiruka.shiruka.log.LogMessage;
import io.github.shiruka.shiruka.log.PipelineLogger;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class which is the superclass of every logger in the pipeline.
 */
public abstract class PipelineLoggerBase implements PipelineLogger {

  /**
   * the next logger in the pipeline.
   */
  @NotNull
  private final PipelineLogger next;

  /**
   * ctor.
   *
   * @param next the next logger in the pipeline.
   */
  protected PipelineLoggerBase(@NotNull final PipelineLogger next) {
    this.next = next;
  }

  /**
   * empty logger.
   */
  PipelineLoggerBase() {
    this(PipelineLogger.EMPTY);
  }

  @Override
  public void debug(@NotNull final LogMessage msg) {
    this.next.debug(this.handle(msg));
  }

  @Override
  public void error(@NotNull final LogMessage msg) {
    this.next.error(this.handle(msg));
  }

  @Override
  public void log(@NotNull final LogMessage msg) {
    this.next.log(this.handle(msg));
  }

  @Override
  @NotNull
  public final PipelineLogger next() {
    return this.next;
  }

  @Override
  @NotNull
  public OutputStream out() {
    return this.next.out();
  }

  @Override
  public void success(@NotNull final LogMessage msg) {
    this.next.success(this.handle(msg));
  }

  @Override
  public void warn(@NotNull final LogMessage msg) {
    this.next.warn(this.handle(msg));
  }
}
