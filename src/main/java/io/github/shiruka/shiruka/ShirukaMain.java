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

package io.github.shiruka.shiruka;

import com.beust.jcommander.JCommander;
import io.github.shiruka.api.Shiruka;
import io.github.shiruka.fragment.FragmentDownloader;
import io.github.shiruka.log.Loggers;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the database of fragments.
   */
  private static final String FRAGMENTS_DATABASE = "https://raw.githubusercontent.com/shiruka/fragments/master/fragments.json";

  /**
   * Shiru ka's program arguments.
   */
  private static final ShirukaCommander COMMANDER = new ShirukaCommander();

  /**
   * the program arguments.
   */
  @NotNull
  private final String[] args;

  /**
   * ctor.
   *
   * @param args the program arguments.
   */
  private ShirukaMain(@NotNull final String[] args) {
    this.args = args.clone();
  }

  /**
   * runs the Java program.
   *
   * @param args the args to run.
   */
  public static void main(final String[] args) {
    try {
      new ShirukaMain(args).exec();
    } catch (final Exception e) {
      JiraExceptionCatcher.serverException(e);
      System.exit(1);
    }
  }

  /**
   * execs the Java program.
   */
  private void exec() throws Exception {
    final var commander = JCommander.newBuilder()
      .programName("Shiru ka")
      .args(this.args)
      .addObject(ShirukaMain.COMMANDER)
      .build();
    if (ShirukaMain.COMMANDER.help) {
      commander.usage();
      return;
    }
    final var logger = Loggers.init("Shiru ka", ShirukaMain.COMMANDER.debug);
    final var fragmentsDir = new File("fragments");
    final var server = new ShirukaServer();
    final var fragmentDownloader = new FragmentDownloader(logger, ShirukaMain.FRAGMENTS_DATABASE);
    fragmentDownloader.getFragments(fragmentInfos ->
      fragmentInfos.stream()
        .filter(info -> {
          if (server.checkFragmentInfo(info)) {

            return true;
          }
          return false;
        })
        .forEach(fragmentInfo -> {
          try {
            fragmentInfo.download(fragmentsDir);
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }));
    final var fragmentManager = new ShirukaFragmentManager(fragmentsDir, logger);
    Shiruka.initServer(server, fragmentManager);
    fragmentManager.loadFragments();
    final var reader = LineReaderBuilder.builder()
      .appName("Shiru ka")
      .terminal(TerminalBuilder.builder()
        .dumb(true)
        .jansi(true)
        .build())
      .build();
    while (true) {
      final var line = reader.readLine("$ ");
      if (line.isEmpty()) {
        continue;
      }
      server.runCommand(line);
      if (server.isInShutdownState()) {
        return;
      }
    }
  }
}
