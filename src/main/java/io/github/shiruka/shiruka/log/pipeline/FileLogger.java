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
import io.github.shiruka.shiruka.network.util.Misc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents the file logger which writes the
 * messages sent by loggers to the log file.
 */
public final class FileLogger extends PipelineLoggerBase {

  /**
   * the directory to the log files.
   */
  private static final Path DIR = Misc.HOME_PATH.resolve("logs");

  /**
   * the index separator for the.
   */
  private static final String IDX_SEPARATOR = Pattern.quote(".");

  /**
   * The system line separator.
   */
  private static final String LINE_SEP = System.getProperty("line.separator");

  /**
   * max file length, 80 mb.
   */
  private static final int MAX_LEN = 83886080;

  /**
   * the file writer lock.
   */
  private final Object lock = new Object();

  /**
   * current log file.
   */
  @Nullable
  private Path current;

  /**
   * the file writer.
   */
  @Nullable
  private Writer out;

  /**
   * ctor.
   *
   * @param next the next logger in the pipeline.
   */
  private FileLogger(@NotNull final PipelineLogger next) {
    super(next);
  }

  /**
   * initializes the files and directories, attempts to
   * find the last log file.
   *
   * @param next the next logger in the pipeline.
   *
   * @return a new instance for {@code this}.
   *
   * @throws IOException pass down exception.
   * @throws IllegalStateException when the file has not match.
   */
  @NotNull
  public static FileLogger init(@NotNull final PipelineLogger next) throws IOException {
    final var logger = new FileLogger(next);
    if (!Files.exists(FileLogger.DIR)) {
      Files.createDirectory(FileLogger.DIR);
    }
    final var files = FileLogger.DIR.toFile().listFiles();
    if (files != null && files.length > 0) {
      final var idx = new AtomicInteger(-1);
      final var f = new AtomicReference<File>();
      Arrays.stream(files).forEach(file -> {
        final var split = file.getName().split(FileLogger.IDX_SEPARATOR);
        final var i = Integer.parseInt(split[1]);
        if (i > idx.get()) {
          idx.set(i);
          f.set(file);
        }
      });
      if (f.get() == null) {
        throw new IllegalStateException();
      }
      synchronized (logger.lock) {
        logger.makeNewLog(f.get().toPath());
      }
    } else {
      synchronized (logger.lock) {
        logger.makeNewLog(0);
      }
    }
    return logger;
  }

  @NotNull
  @Override
  public LogMessage handle(@NotNull final LogMessage msg) {
    try {
      final var writer = this.check();
      writer.write(msg.format(0));
      writer.write(FileLogger.LINE_SEP);
      writer.flush();
    } catch (final IOException exception) {
      throw new IllegalStateException(exception);
    }
    return msg;
  }

  /**
   * checks if the file needs to be truncated.
   *
   * @return the writer, including if it was updated.
   */
  private Writer check() {
    if (ThreadLocalRandom.current().nextInt(50) == 1) {
      synchronized (this.lock) {
        try {
          if (Files.size(Objects.requireNonNull(this.current, "current")) > FileLogger.MAX_LEN) {
            this.makeNewLog(this.current);
          }
          return this.out;
        } catch (final IOException e) {
          throw new IllegalStateException(e);
        }
      }
    } else {
      synchronized (this.lock) {
        return this.out;
      }
    }
  }

  /**
   * creates a new log file.
   *
   * @param last the last log file.
   *
   * @throws IOException if something dumb went wrong.
   */
  private void makeNewLog(final Path last) throws IOException {
    final var split = last.toFile().getName().split(FileLogger.IDX_SEPARATOR);
    final var curIdx = Integer.parseInt(split[1]) + 1;
    this.makeNewLog(curIdx);
  }

  /**
   * creates a new log file based from the new index.
   *
   * @param idx the new index.
   *
   * @throws IOException if something dumb went wrong.
   */
  private void makeNewLog(final int idx) throws IOException {
    final var path = FileLogger.DIR.resolve("log." + idx + ".log");
    Files.createFile(path);
    this.current = path;
    this.out = new FileWriter(path.toFile());
  }
}
