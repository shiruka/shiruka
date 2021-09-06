package io.github.shiruka.shiruka;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Function;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

/**
 * a class that that represents Shiru ka's console commands.
 */
@CommandLine.Command(
  name = "shiruka",
  mixinStandardHelpOptions = true,
  exitCodeOnVersionHelp = -1,
  exitCodeOnUsageHelp = -1,
  versionProvider = Console.VersionProvider.class,
  showDefaultValues = true
)
@Log4j2
final class Console implements Runnable {

  /**
   * the exception handler for console commands.
   */
  private static final Function<Throwable, Integer> EXCEPTION_HANDLER = throwable -> {
    Console.log.fatal("An exception occurred:", throwable);
    return -1;
  };

  /**
   * the debug mode.
   */
  @Nullable
  @CommandLine.Option(names = {"-c", "--config"}, description = "Config path.", defaultValue = "shiruka.yml")
  private Path configPath;

  /**
   * the debug mode.
   */
  @Nullable
  @CommandLine.Option(names = {"-d", "--debug"}, description = "Debug mode.", defaultValue = "false")
  private Boolean debug;

  /**
   * the server language.
   */
  @Nullable
  @CommandLine.Option(names = {"-l", "--lang"}, description = "Shiru ka language.", defaultValue = "en_US")
  private Locale lang;

  /**
   * initiate the console commands.
   *
   * @param args the args to initiate.
   */
  static void init(@NotNull final String[] args) {
    final var exitCode = new CommandLine(Console.class)
      .setExecutionExceptionHandler((ex, commandLine, parseResult) -> Console.EXCEPTION_HANDLER.apply(ex))
      .setParameterExceptionHandler((ex, args1) -> Console.EXCEPTION_HANDLER.apply(ex))
      .registerConverter(InetSocketAddress.class, new InetSocketAddressConverter())
      .registerConverter(Locale.class, new LocaleConverter())
      .registerConverter(Path.class, Path::of)
      .execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    if (this.debug != null && this.debug) {
      final var context = (LoggerContext) LogManager.getContext(false);
      context.getConfiguration()
        .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        .setLevel(Level.DEBUG);
      context.updateLoggers();
    }
    Config.loadConfig(this.configPath == null
      ? Constants.herePath().resolve("shiruka.yml")
      : Constants.herePath().resolve(this.configPath));
    Config.language(this.lang == null
      ? Locale.ENGLISH
      : this.lang);
    Languages.init(
      Config.shirukaLanguageBundle(),
      Config.vanillaLanguageBundle());
  }

  /**
   * a class that converts user's inputs into inet socket address.
   */
  private static final class InetSocketAddressConverter implements CommandLine.ITypeConverter<InetSocketAddress> {

    @Override
    public InetSocketAddress convert(final String value) {
      final var position = value.trim().lastIndexOf(':');
      if (position < 0) {
        throw new CommandLine.TypeConversionException("Invalid format: must be 'host:port' but was '%s'"
          .formatted(value));
      }
      return new InetSocketAddress(
        value.substring(0, position),
        Integer.parseInt(value.substring(position + 1)));
    }
  }

  /**
   * a class that converts user's inputs into locale.
   */
  private static final class LocaleConverter implements CommandLine.ITypeConverter<Locale> {

    @Override
    public Locale convert(final String value) {
      final var split = value.trim().replace("-", "_").toLowerCase(Locale.ROOT).split("_");
      if (split.length != 2) {
        throw new CommandLine.TypeConversionException("Invalid format: must be '(language)_(country)' but was '%s'"
          .formatted(value));
      }
      return new Locale(split[0], split[1]);
    }
  }

  /**
   * a class that provides the Shiru ka's version.
   */
  static final class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
      return new String[]{
        Constants.version()
      };
    }
  }
}
