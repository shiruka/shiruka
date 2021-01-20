package co.aikar.timings;

import com.google.common.collect.Lists;
import java.util.List;
import net.shiruka.api.Shiruka;
import net.shiruka.api.command.sender.CommandSender;
import net.shiruka.api.command.sender.ConsoleCommandSender;
import net.shiruka.api.command.sender.MessageCommandSender;
import net.shiruka.api.command.sender.RemoteConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.utils.Validate;

@SuppressWarnings("WeakerAccess")
public class TimingsReportListener implements MessageCommandSender {

  private final Runnable onDone;

  private final List<CommandSender> senders;

  private String timingsURL;

  public TimingsReportListener(@NotNull final CommandSender senders) {
    this(senders, null);
  }

  public TimingsReportListener(@NotNull final CommandSender sender, @Nullable final Runnable onDone) {
    this(Lists.newArrayList(sender), onDone);
  }

  public TimingsReportListener(@NotNull final List<CommandSender> senders) {
    this(senders, null);
  }

  public TimingsReportListener(@NotNull final List<CommandSender> senders, @Nullable final Runnable onDone) {
    Validate.notNull(senders);
    Validate.notEmpty(senders);
    this.senders = Lists.newArrayList(senders);
    this.onDone = onDone;
  }

  public void addConsoleIfNeeded() {
    final boolean hasConsole = this.senders.stream()
      .anyMatch(sender -> sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender);
    if (!hasConsole) {
      this.senders.add(Shiruka.getConsoleCommandSender());
    }
  }

  public void done(@Nullable final String url) {
    this.timingsURL = url;
    if (this.onDone != null) {
      this.onDone.run();
    }
    this.senders.stream()
      .filter(sender -> sender instanceof TimingsReportListener)
      .forEach(sender -> ((TimingsReportListener) sender).done());
  }

  public void done() {
    this.done(null);
  }

  @Nullable
  public String getTimingsURL() {
    return this.timingsURL;
  }

  @Override
  public void sendMessage(@NotNull final String message) {
    this.senders.forEach(sender -> sender.sendMessage(message));
  }
}
