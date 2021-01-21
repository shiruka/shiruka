/*
 * This file is licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 Daniel Ennis <http://aikar.co>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package co.aikar.timings;

import static co.aikar.timings.TimingsManager.HISTORY;
import static co.aikar.util.JSONUtil.*;
import com.google.common.collect.Sets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.Material;
import net.shiruka.api.entity.EntityType;
import net.shiruka.api.text.ChatColor;
import net.shiruka.shiruka.ShirukaServer;
import net.shiruka.shiruka.config.ServerConfig;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.MemorySection;

@SuppressWarnings({"rawtypes", "SuppressionAnnotation"})
public class TimingsExport extends Thread {

  private static long lastReport = 0;

  private final TimingHistory[] history;

  private final TimingsReportListener listeners;

  private final Map out;

  private TimingsExport(final TimingsReportListener listeners, final Map out, final TimingHistory[] history) {
    super("Timings paste thread");
    this.listeners = listeners;
    this.out = out;
    this.history = history;
  }

  /**
   * Checks if any pending reports are being requested, and builds one if needed.
   */
  public static void reportTimings() {
    if (Timings.requestingReport.isEmpty()) {
      return;
    }
    final var listeners = new TimingsReportListener(Timings.requestingReport);
    listeners.addConsoleIfNeeded();
    Timings.requestingReport.clear();
    final var now = System.currentTimeMillis();
    final var lastReportDiff = now - TimingsExport.lastReport;
    if (lastReportDiff < 60000) {
      listeners.sendMessage(ChatColor.RED + "Please wait at least 1 minute in between Timings reports. (" + (int) ((60000 - lastReportDiff) / 1000) + " seconds)");
      listeners.done();
      return;
    }
    final var lastStartDiff = now - TimingsManager.timingStart;
    if (lastStartDiff < 180000) {
      listeners.sendMessage(ChatColor.RED + "Please wait at least 3 minutes before generating a Timings report. Unlike Timings v1, v2 benefits from longer timings and is not as useful with short timings. (" + (int) ((180000 - lastStartDiff) / 1000) + " seconds)");
      listeners.done();
      return;
    }
    listeners.sendMessage(ChatColor.GREEN + "Preparing Timings Report...");
    TimingsExport.lastReport = now;
    final Map parent = createObject(
      // Get some basic system details about the server
      pair("version", ShirukaServer.VERSION),
      pair("maxplayers", Shiruka.getServer().getMaxPlayerCount()),
      pair("start", TimingsManager.timingStart / 1000),
      pair("end", System.currentTimeMillis() / 1000),
      pair("online-mode", ServerConfig.ONLINE_MODE.getValue().orElse(false)),
      pair("sampletime", (System.currentTimeMillis() - TimingsManager.timingStart) / 1000),
      pair("datapacks", Shiruka.getPackManager().getPacks().keySet())
    );
    if (!TimingsManager.privacy) {
      final var description = Shiruka.getServer().getServerDescription().join();
      appendObjectData(parent,
        pair("server", ServerConfig.TIMINGS_SERVER_NAME.getValue().orElse("Unknown Server")),
        pair("motd", description.getDescription())
//        pair("icon", "")
      );
    }
    final var runtime = Runtime.getRuntime();
    final var runtimeBean = ManagementFactory.getRuntimeMXBean();
    final var osInfo = ManagementFactory.getOperatingSystemMXBean();
    parent.put("system", createObject(
      pair("timingcost", TimingsExport.getCost()),
      pair("loadavg", osInfo.getSystemLoadAverage()),
      pair("name", System.getProperty("os.name")),
      pair("version", System.getProperty("os.version")),
      pair("jvmversion", System.getProperty("java.version")),
      pair("arch", System.getProperty("os.arch")),
      pair("maxmem", runtime.maxMemory()),
      pair("memory", createObject(
        pair("heap", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().toString()),
        pair("nonheap", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().toString()),
        pair("finalizing", ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount())
      )),
      pair("cpu", runtime.availableProcessors()),
      pair("runtime", runtimeBean.getUptime()),
      pair("flags", String.join(" ", runtimeBean.getInputArguments())),
      pair("gc", toObjectMapper(ManagementFactory.getGarbageCollectorMXBeans(), input -> pair(input.getName(), toArray(input.getCollectionCount(), input.getCollectionTime()))))
      )
    );
    parent.put("worlds", new HashMap<>());
//    @todo #1:60m Implement world for timings 2.
//    parent.put("worlds", toObjectMapper(Shiruka.getWorlds(), world -> {
//      return pair(world.getName(), createObject(
//        pair("gamerules", toObjectMapper(world.getGameRules(), rule -> {
//          return pair(rule, world.getGameRuleValue(rule));
//        })),
//        pair("ticking-distance", world.getChunkProvider().playerChunkMap.getEffectiveViewDistance()),
//        pair("notick-viewdistance", world.getChunkProvider().playerChunkMap.getEffectiveNoTickViewDistance())
//      ));
//    }));
    final var tileEntityTypeSet = Sets.<Material>newHashSet();
    final var entityTypeSet = Sets.<EntityType>newHashSet();
    final var size = HISTORY.size();
    final var history = new TimingHistory[size + 1];
    var i = 0;
    for (final var timingHistory : HISTORY) {
      tileEntityTypeSet.addAll(timingHistory.tileEntityTypeSet);
      entityTypeSet.addAll(timingHistory.entityTypeSet);
      history[i++] = timingHistory;
    }
    history[i] = new TimingHistory(); // Current snapshot
    tileEntityTypeSet.addAll(history[i].tileEntityTypeSet);
    entityTypeSet.addAll(history[i].entityTypeSet);
    final var handlers = createObject();
    final Map groupData;
    synchronized (TimingIdentifier.GROUP_MAP) {
      for (final var group : TimingIdentifier.GROUP_MAP.values()) {
        synchronized (group.handlers) {
          for (final var id : group.handlers) {
            if (!id.isTimed() && !id.isSpecial()) {
              continue;
            }
            var name = id.identifier.name;
            if (name.startsWith("##")) {
              name = name.substring(3);
            }
            handlers.put(String.valueOf(id.id), toArray(group.id, name));
          }
        }
      }
      groupData = toObjectMapper(
        TimingIdentifier.GROUP_MAP.values(), group -> pair(group.id, group.name));
    }
    parent.put("idmap", createObject(
      pair("groups", groupData),
      pair("handlers", handlers),
      pair("worlds", toObjectMapper(TimingHistory.worldMap.entrySet(), input -> pair(input.getValue(), input.getKey()))),
      pair("tileentity",
        toObjectMapper(tileEntityTypeSet, input -> pair(input.ordinal(), input.name()))),
      pair("entity",
        toObjectMapper(entityTypeSet, input -> pair(input.ordinal(), input.name())))
    ));
    // Information about loaded plugins
    parent.put("plugins", toObjectMapper(Shiruka.getPluginManager().getPlugins(),
      plugin -> pair(plugin.getName(), createObject(
        pair("version", plugin.getDescription().getVersion()),
        pair("description", plugin.getDescription().getDescription().trim()),
        pair("website", plugin.getDescription().getWebsite()),
        pair("authors", String.join(", ", plugin.getDescription().getAuthors()))
      ))));
    // Information on the users Config
    parent.put("config", createObject(
      pair("spigot", TimingsExport.mapAsJSON(ServerConfig.get().orElseThrow().getConfiguration(), null))
    ));
    new TimingsExport(listeners, parent, history).start();
  }

  static long getCost() {
    // Benchmark the users System.nanotime() for cost basis
    final int passes = 100;
    final TimingHandler SAMPLER1 = Timings.ofSafe("Timings Sampler 1");
    final TimingHandler SAMPLER2 = Timings.ofSafe("Timings Sampler 2");
    final TimingHandler SAMPLER3 = Timings.ofSafe("Timings Sampler 3");
    final TimingHandler SAMPLER4 = Timings.ofSafe("Timings Sampler 4");
    final TimingHandler SAMPLER5 = Timings.ofSafe("Timings Sampler 5");
    final TimingHandler SAMPLER6 = Timings.ofSafe("Timings Sampler 6");
    final long start = System.nanoTime();
    for (int i = 0; i < passes; i++) {
      SAMPLER1.startTiming();
      SAMPLER2.startTiming();
      SAMPLER3.startTiming();
      SAMPLER3.stopTiming();
      SAMPLER4.startTiming();
      SAMPLER5.startTiming();
      SAMPLER6.startTiming();
      SAMPLER6.stopTiming();
      SAMPLER5.stopTiming();
      SAMPLER4.stopTiming();
      SAMPLER2.stopTiming();
      SAMPLER1.stopTiming();
    }
    final long timingsCost = (System.nanoTime() - start) / passes / 6;
    SAMPLER1.reset(true);
    SAMPLER2.reset(true);
    SAMPLER3.reset(true);
    SAMPLER4.reset(true);
    SAMPLER5.reset(true);
    SAMPLER6.reset(true);
    return timingsCost;
  }

  private static JSONObject mapAsJSON(final ConfigurationSection config, final String parentKey) {
    final var object = new JSONObject();
    for (final var key : config.getKeys(false)) {
      final var fullKey = parentKey != null ? parentKey + "." + key : key;
      if (fullKey.equals("database") || fullKey.equals("settings.bungeecord-addresses") ||
        TimingsManager.hiddenConfigs.contains(fullKey) || key.startsWith("seed-") || key.equals("worldeditregentempworld")) {
        continue;
      }
      final var val = config.get(key);
      object.put(key, TimingsExport.valAsJSON(val, fullKey));
    }
    return object;
  }

  private static Object valAsJSON(final Object val, final String parentKey) {
    if (!(val instanceof MemorySection)) {
      if (val instanceof List) {
        final Iterable<Object> v = (Iterable<Object>) val;
        return toArrayMapper(v, input -> TimingsExport.valAsJSON(input, parentKey));
      } else {
        return String.valueOf(val);
      }
    } else {
      return TimingsExport.mapAsJSON((ConfigurationSection) val, parentKey);
    }
  }

  @Override
  public void run() {
    this.out.put("data", toArrayMapper(this.history, TimingHistory::export));
    String response = null;
    String timingsURL = null;
    try {
      final HttpURLConnection con = (HttpURLConnection) new URL("http://timings.aikar.co/post").openConnection();
      con.setDoOutput(true);
      String hostName = "BrokenHost";
      try {
        hostName = InetAddress.getLocalHost().getHostName();
      } catch (final Exception ignored) {
      }
      con.setRequestProperty("User-Agent", "Shiruka/" + ServerConfig.TIMINGS_SERVER_NAME.getValue().orElse("Unknown Server") + "/" + hostName);
      con.setRequestMethod("POST");
      con.setInstanceFollowRedirects(false);
      final OutputStream request = new GZIPOutputStream(con.getOutputStream()) {{
        this.def.setLevel(7);
      }};
      request.write(JSONValue.toJSONString(this.out).getBytes(StandardCharsets.UTF_8));
      request.close();
      response = this.getResponse(con);
      if (con.getResponseCode() != 302) {
        this.listeners.sendMessage(
          ChatColor.RED + "Upload Error: " + con.getResponseCode() + ": " + con.getResponseMessage());
        this.listeners.sendMessage(ChatColor.RED + "Check your logs for more information");
        if (response != null) {
          Shiruka.getLogger().fatal(response);
        }
        return;
      }
      timingsURL = con.getHeaderField("Location");
      this.listeners.sendMessage(ChatColor.GREEN + "View Timings Report: " + timingsURL);
      if (response != null && !response.isEmpty()) {
        Shiruka.getLogger().info("Timing Response: " + response);
      }
    } catch (final IOException ex) {
      this.listeners.sendMessage(ChatColor.RED + "Error uploading timings, check your logs for more information");
      if (response != null) {
        Shiruka.getLogger().fatal(response);
      }
      Shiruka.getLogger().fatal("Could not paste timings", ex);
    } finally {
      this.listeners.done(timingsURL);
    }
  }

  @Nullable
  private String getResponse(final HttpURLConnection con) throws IOException {
    try (final var is = con.getInputStream()) {
      final var bos = new ByteArrayOutputStream();
      final var b = new byte[1024];
      int bytesRead;
      while ((bytesRead = is.read(b)) != -1) {
        bos.write(b, 0, bytesRead);
      }
      return bos.toString();
    } catch (final IOException ex) {
      this.listeners.sendMessage(ChatColor.RED + "Error uploading timings, check your logs for more information");
      Shiruka.getLogger().warn(con.getResponseMessage(), ex);
      return null;
    }
  }
}
