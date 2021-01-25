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

package net.shiruka.shiruka.network.packets;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import net.shiruka.shiruka.network.ShirukaPacket;
import net.shiruka.shiruka.network.VarInts;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents pack stack packets.
 */
public final class PackStackPacket extends ShirukaPacket {

  /**
   * the behavior packs.
   */
  @NotNull
  private final List<Entry> behaviorPacks;

  /**
   * the experiments.
   */
  @NotNull
  private final List<ExperimentData> experiments;

  /**
   * the experiment previously toggled.
   */
  private final boolean experimentsPreviouslyToggled;

  /**
   * the forced to accept.
   */
  private final boolean forcedToAccept;

  /**
   * the game version.
   */
  @NotNull
  private final String gameVersion;

  /**
   * the resource packs.
   */
  @NotNull
  private final List<Entry> resourcePacks;

  /**
   * ctor.
   */
  public PackStackPacket() {
    this(new ObjectArrayList<>(), new ObjectArrayList<>(), false, false,
      "", new ObjectArrayList<>());
  }

  /**
   * ctor.
   *
   * @param behaviorPacks the behavior packs.
   * @param experiments the experiments.
   * @param experimentsPreviouslyToggled the experiment previously toggled.
   * @param forcedToAccept the forced to accept.
   * @param gameVersion the game version.
   * @param resourcePacks the resource packs.
   */
  public PackStackPacket(@NotNull final List<Entry> behaviorPacks, @NotNull final List<ExperimentData> experiments,
                         final boolean experimentsPreviouslyToggled, final boolean forcedToAccept,
                         @NotNull final String gameVersion, @NotNull final List<Entry> resourcePacks) {
    super(ShirukaPacket.ID_PACK_STACK);
    this.behaviorPacks = Collections.unmodifiableList(behaviorPacks);
    this.experiments = Collections.unmodifiableList(experiments);
    this.experimentsPreviouslyToggled = experimentsPreviouslyToggled;
    this.forcedToAccept = forcedToAccept;
    this.gameVersion = gameVersion;
    this.resourcePacks = Collections.unmodifiableList(resourcePacks);
  }

  @Override
  public void encode() {
    this.writeBoolean(this.forcedToAccept);
    this.writeArray(this.getBehaviorPacks(), this::writeEntry);
    this.writeArray(this.getResourcePacks(), this::writeEntry);
    VarInts.writeString(this.buffer(), this.getGameVersion());
    this.writeExperiments(this.getExperiments());
    this.writeBoolean(this.experimentsPreviouslyToggled);
  }

  /**
   * obtains the behaviour packs.
   *
   * @return behaviour packs.
   */
  @NotNull
  public List<Entry> getBehaviorPacks() {
    return this.behaviorPacks;
  }

  /**
   * obtains the experiments.
   *
   * @return experiments.
   */
  @NotNull
  public List<ExperimentData> getExperiments() {
    return this.experiments;
  }

  /**
   * obtains the game version.
   *
   * @return game version.
   */
  @NotNull
  public String getGameVersion() {
    return this.gameVersion;
  }

  /**
   * obtains the resource pack.
   *
   * @return resource packet.
   */
  @NotNull
  public List<Entry> getResourcePacks() {
    return this.resourcePacks;
  }

  /**
   * obtains the is experiments previously toggled.
   *
   * @return experiments previously toggled.
   */
  public boolean isExperimentsPreviouslyToggled() {
    return this.experimentsPreviouslyToggled;
  }

  /**
   * obtains the is forced to accept.
   *
   * @return is forced to accept.
   */
  public boolean isForcedToAccept() {
    return this.forcedToAccept;
  }

  /**
   * a class that represents entries of {@code this} packet.
   */
  public static final class Entry {

    /**
     * the pack id.
     */
    @NotNull
    private final String packId;

    /**
     * the pack version.
     */
    @NotNull
    private final String packVersion;

    /**
     * the sub pack name.
     */
    @NotNull
    private final String subPackName;

    /**
     * ctor.
     *
     * @param packId the pack id.
     * @param packVersion the pack version.
     * @param subPackName the sub pack name.
     */
    public Entry(@NotNull final String packId, @NotNull final String packVersion, @NotNull final String subPackName) {
      this.packId = packId;
      this.packVersion = packVersion;
      this.subPackName = subPackName;
    }

    /**
     * obtains the pack id.
     *
     * @return pack id.
     */
    @NotNull
    public String getPackId() {
      return this.packId;
    }

    /**
     * obtains the pack version.
     *
     * @return pack version.
     */
    @NotNull
    public String getPackVersion() {
      return this.packVersion;
    }

    /**
     * obtains the sub pack name.
     *
     * @return sub pack name.
     */
    @NotNull
    public String getSubPackName() {
      return this.subPackName;
    }
  }

  /**
   * a class that represents experiment data.
   */
  public static final class ExperimentData {

    /**
     * the enabled.
     */
    private final boolean enabled;

    /**
     * the name.
     */
    @NotNull
    private final String name;

    /**
     * ctor.
     *
     * @param name the name.
     * @param enabled the enabled.
     */
    public ExperimentData(@NotNull final String name, final boolean enabled) {
      this.name = name;
      this.enabled = enabled;
    }

    /**
     * obtains the name.
     *
     * @return name.
     */
    @NotNull
    public String getName() {
      return this.name;
    }

    /**
     * obtains the enabled.
     *
     * @return enabled.
     */
    public boolean isEnabled() {
      return this.enabled;
    }
  }
}
