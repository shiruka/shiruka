package io.github.shiruka.shiruka;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Locale;
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
   * the debug mode.
   */
  @Nullable
  @CommandLine.Option(names = {"-c", "--config"}, description = "Config path.", defaultValue = "config.yml")
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
    final var exitCode = new picocli.CommandLine(Console.class)
      .registerConverter(InetSocketAddress.class, new Console.InetSocketAddressConverter())
      .registerConverter(Locale.class, new Console.LocaleConverter())
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
    if (this.configPath == null) {
      Config.loadConfig(Constants.getHerePath().resolve("config.yml"));
    } else {
      Config.loadConfig(Constants.getHerePath().resolve(this.configPath));
    }
    if (this.lang != null) {
      Config.setLanguage(this.lang);
    }
    Languages.init(Config.getShirukaLanguageBundle());
    Console.log.info(Languages.getLanguageValue("language-set", Config.lang));
  }

  /**
   * a class that converts user's inputs into inet socket address.
   */
  public static final class InetSocketAddressConverter implements CommandLine.ITypeConverter<InetSocketAddress> {

    @Override
    public InetSocketAddress convert(final String value) {
      final var position = value.lastIndexOf(':');
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
  public static final class LocaleConverter implements CommandLine.ITypeConverter<Locale> {

    @Nullable
    @Override
    public Locale convert(final String value) {
      final var split = value.trim().replace("-", "_").split("_");
      if (split.length != 2) {
        return null;
      }
      return new Locale(
        split[0].toLowerCase(Locale.ROOT),
        split[1].toUpperCase(Locale.ROOT));
    }
  }

  /**
   * a class that provides the Shiru ka's version.
   */
  public static final class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
      return new String[]{
        Constants.getVersion()
      };
    }
  }
}
