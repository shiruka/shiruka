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
    final var fragmentManager = new ShirukaFragmentManager(fragmentsDir, logger);
    final var descriptions = fragmentManager.getDescriptions();
    fragmentDownloader.getFragments(fragmentInfos ->
      fragmentInfos.stream()
        .filter(info -> {
          try {
            final var fragmentPath = fragmentsDir.toPath().resolve(info.getName() + ".jar");
            // Check if the fragment should download and load.
            if (server.checkFragmentInfo(info)) {
              // If the file does not exist download.
              if (!Files.exists(fragmentPath)) {
                return true;
              }
              final var description = descriptions.get(info.getName());
              // If the fragment.yml in the .jar file exist and
              // the version equals the upcoming fragment's version do not download.
              if (description != null && description.getVersion().equals(info.getVersion())) {
                return false;
              }
              // If the fragment.yml does not exist or the upcoming fragment's version
              // not equal the current fragment's version delete file and download the latest.
              Files.delete(fragmentPath);
              return true;
            } else if (Files.exists(fragmentPath)) {
              // If the fragment file exist
              // If the fragment should not download and load delete the file.
              Files.delete(fragmentPath);
            }
          } catch (final IOException e) {
            JiraExceptionCatcher.serverException(e);
          }
          return false;
        })
        .forEach(fragmentInfo -> {
          try {
            fragmentInfo.download(fragmentsDir);
          } catch (final IOException e) {
            JiraExceptionCatcher.serverException(e);
          }
        }));
    Shiruka.initServer(server, fragmentManager);
    fragmentManager.loadFragments();
    final var console = new ShirukaConsole(server);
    console.start();
  }
}
