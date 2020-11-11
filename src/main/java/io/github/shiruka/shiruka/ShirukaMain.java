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
import io.github.shiruka.fragment.FragmentDownloader;
import io.github.shiruka.log.Loggers;
import io.github.shiruka.shiruka.misc.JiraExceptionCatcher;
import java.io.File;
import java.nio.file.Files;
import org.jetbrains.annotations.NotNull;

/**
 * a Java main class to start the Shiru ka's server.
 */
public final class ShirukaMain {

  /**
   * the database of fragments.
   */
  private static final String FRAGMENTS_DATABASE =
    "https://raw.githubusercontent.com/shiruka/fragments/master/fragments.json";

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
    final var commander = JCommander.newBuilder()
      .programName("Shiru ka")
      .args(args)
      .addObject(ShirukaMain.COMMANDER)
      .build();
    if (ShirukaMain.COMMANDER.help) {
      commander.usage();
      return;
    }
    JiraExceptionCatcher.run(() ->
      new ShirukaMain(args).exec());
  }

  /**
   * execs the Java program.
   */
  private void exec() throws Exception {
    final var logger = Loggers.init("Shiru ka", ShirukaMain.COMMANDER.debug);
    logger.log("Shiru ka is starting...");
    final var fragmentsDir = new File("fragments");
    if (!fragmentsDir.exists()) {
      logger.log("Fragment directory not found, It's is creating...");
      fragmentsDir.mkdirs();
    }
    final var server = new ShirukaServer();
    logger.log("Server configuration file is preparing...");
    // TODO initiate server configuration file.
    logger.log("Fragments are preparing...");
    final var fragmentDownloader = new FragmentDownloader(logger, ShirukaMain.FRAGMENTS_DATABASE);
    final var fragmentManager = new ShirukaFragmentManager(fragmentsDir, logger);
    final var descriptions = server.filterFragments(fragmentManager.getDescriptions());
    fragmentDownloader.getFragments().thenAccept(fragmentInfos -> {
      if (fragmentInfos != null) {
        for (final var info : fragmentInfos) {
          final var fragmentPath = fragmentsDir.toPath().resolve(info.getName() + ".jar");
          if (descriptions.containsKey(info.getName())) {
            if (Files.exists(fragmentPath)) {
              final var description = descriptions.get(info.getName());
              if (description.getVersion().equals(info.getVersion())) {
                continue;
              }
              JiraExceptionCatcher.run(() ->
                Files.delete(fragmentPath));
            }
            JiraExceptionCatcher.run(() ->
              info.download(fragmentsDir));
          } else if (Files.exists(fragmentPath)) {
            JiraExceptionCatcher.run(() ->
              Files.delete(fragmentPath));
          }
        }
      }
    });
    final var console = new ShirukaConsole(server);
    console.start();
  }
}
