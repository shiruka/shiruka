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

package net.shiruka.shiruka.console;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.shiruka.api.Shiruka;
import net.shiruka.api.command.CommandDispatcher;
import net.shiruka.api.command.suggestion.Suggestion;
import net.shiruka.shiruka.command.SimpleCommandManager;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/**
 * an implementation for {@link Completer} to auto complete console commands.
 */
final class ConsoleCommandCompleter implements Completer {

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    final var buffer = line.line();
    final var event = Shiruka.getEventManager().asyncTabComplete(Shiruka.getConsoleCommandSender(),
      Collections.emptyList(), buffer);
    final var successful = event.callEvent();
    final var completions = successful
      ? event.getCompletions()
      : Collections.<String>emptyList();
    if (!successful || event.isHandled()) {
      if (!completions.isEmpty()) {
        candidates.addAll(completions.stream()
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Candidate::new)
          .collect(Collectors.toList()));
      }
      return;
    }
    try {
      final var parse = SimpleCommandManager.getDispatcher().parse(event.getText(), event.getSender());
      CommandDispatcher.getCompletionSuggestions(parse).get().getSuggestionList().stream()
        .map(Suggestion::getText)
        .map(Candidate::new)
        .forEach(candidates::add);
    } catch (final ExecutionException e) {
      // @todo #1:5m Add language support for Unhandled exception when tab completing.
      Shiruka.getLogger().warn("Unhandled exception when tab completing", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
