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

package io.github.shiruka.shiruka.network.packet;

import com.google.common.base.Preconditions;
import io.github.shiruka.shiruka.network.impl.PlayerConnection;
import io.github.shiruka.shiruka.network.packets.*;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that holds packets registered by their identifying packet ID as specified in the Minecraft protocol.
 */
public final class PacketRegistry {

  /**
   * the constructors used to instantiate the packets.
   */
  private static final Map<Class<? extends Packet>, Constructor<? extends Packet>> CONSTRUCTORS = new HashMap<>();

  /**
   * inverse packet registry.
   */
  private static final Int2ReferenceOpenHashMap<Class<? extends Packet>> PACKETS =
    new Int2ReferenceOpenHashMap<>();

  /**
   * packet registry.
   */
  private static final Reference2IntOpenHashMap<Class<? extends Packet>> PACKET_IDS = new Reference2IntOpenHashMap<>();

  static {
    // packets of handshake state.
    PacketRegistry.putIn(PacketInLogin.class, PlayerConnection.State.HANDSHAKE, 1);
    PacketRegistry.putOut(PacketOutPlayStatus.class, PlayerConnection.State.HANDSHAKE, 2);
    // packets of status state.
    PacketRegistry.putOut(PacketOutPackInfo.class, PlayerConnection.State.STATUS, 6);
    PacketRegistry.putOut(PacketOutPackStack.class, PlayerConnection.State.STATUS, 7);
    PacketRegistry.putIn(PacketInResourcePackResponse.class, PlayerConnection.State.STATUS, 8);
    PacketRegistry.putOut(PacketOutResourcePackDataInfo.class, PlayerConnection.State.STATUS, 82);
    PacketRegistry.putOut(PacketOutResourcePackChunkData.class, PlayerConnection.State.STATUS, 83);
    PacketRegistry.putIn(PacketInResourcePackChunkRequest.class, PlayerConnection.State.STATUS, 84);
    PacketRegistry.putIn(PacketInClientCacheStatus.class, PlayerConnection.State.STATUS, 129);
    // packets of any state.
    PacketRegistry.putOut(PacketOutDisconnect.class, PlayerConnection.State.ANY, 5);
    PacketRegistry.PACKETS.trim();
    PacketRegistry.PACKET_IDS.trim();
  }

  /**
   * ctor.
   */
  private PacketRegistry() {
  }

  /**
   * obtains the class of the packet containing the given ID, bound, and the given getState.
   *
   * @param state the packet's network getState.
   * @param bound the packet bound.
   * @param id the packet ID.
   *
   * @return the packet class.
   */
  @Nullable
  public static Class<? extends Packet> byId(@NotNull final PlayerConnection.State state,
                                             @NotNull final PacketBound bound, final int id) {
    final var found = PacketRegistry.PACKETS.get(PacketRegistry.shift(state, bound, id));
    if (found != null) {
      return found;
    }
    return PacketRegistry.PACKETS.get(PacketRegistry.shift(PlayerConnection.State.ANY, bound, id));
  }

  /**
   * obtains the ID of the packet with the given info.
   *
   * @param info the info.
   *
   * @return the packet ID.
   */
  public static int idOf(final int info) {
    return info & 0x7ffffff;
  }

  /**
   * creates a new instance of the given packet class.
   *
   * @param <T> the packet type.
   * @param cls the packet class to instantiate.
   *
   * @return the instantiated packet.
   *
   * @throws IllegalAccessException if this {@code Constructor} object is enforcing Java language access control and
   *   the underlying constructor is inaccessible.
   * @throws InstantiationException if the class that declares the underlying constructor represents an abstract
   *   class.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   */
  @NotNull
  public static <T extends Packet> T make(@NotNull final Class<? extends Packet> cls) throws IllegalAccessException,
    InvocationTargetException, InstantiationException {
    //noinspection unchecked
    return (T) PacketRegistry.CONSTRUCTORS.get(cls).newInstance();
  }

  /**
   * creates a new instance of the given packet class.
   *
   * @param cls the packet class to instantiate.
   *
   * @return the instantiated packet.
   *
   * @throws IllegalAccessException if this {@code Constructor} object is enforcing Java language access control and
   *   the underlying constructor is inaccessible.
   * @throws InstantiationException if the class that declares the underlying constructor represents an abstract
   *   class.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   */
  @NotNull
  public static PacketIn makeIn(@NotNull final Class<? extends Packet> cls) throws IllegalAccessException,
    InvocationTargetException, InstantiationException {
    return PacketRegistry.make(cls);
  }

  /**
   * creates a new instance of the given packet class.
   *
   * @param cls the packet class to instantiate.
   *
   * @return the instantiated packet.
   *
   * @throws IllegalAccessException if this {@code Constructor} object is enforcing Java language access control and
   *   the underlying constructor is inaccessible.
   * @throws InstantiationException if the class that declares the underlying constructor represents an abstract
   *   class.
   * @throws InvocationTargetException if the underlying constructor throws an exception.
   */
  @NotNull
  public static PacketOut makeOut(@NotNull final Class<? extends Packet> cls) throws IllegalAccessException,
    InvocationTargetException, InstantiationException {
    return PacketRegistry.make(cls);
  }

  /**
   * obtains the net getState which the packet is registered to be present in.
   *
   * @param cls the packet class.
   *
   * @return the getState of the packet.
   */
  public static int packetInfo(@NotNull final Class<? extends Packet> cls) {
    final var identifier = PacketRegistry.PACKET_IDS.getInt(cls);
    Preconditions.checkArgument(identifier != -1, "%s is not registered", cls.getSimpleName());
    return identifier;
  }

  /**
   * puts the given packet class into the map with the given ID, and also inserts the constructor into the CTOR cache.
   *
   * @param cls the class.
   * @param id the ID.
   */
  private static void put(@NotNull final Class<? extends Packet> cls, @NotNull final PlayerConnection.State state,
                          @NotNull final PacketBound bound, final int id) {
    final var identifier = PacketRegistry.shift(state, bound, id);
    PacketRegistry.PACKET_IDS.put(cls, identifier);
    if (bound != PacketBound.SERVER) {
      return;
    }
    PacketRegistry.PACKETS.put(identifier, cls);
    try {
      PacketRegistry.CONSTRUCTORS.put(cls, cls.getConstructor());
    } catch (final NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  /**
   * puts the given packet class into the map with the given ID, and also inserts the constructor into the CTOR cache.
   *
   * @param cls the class.
   * @param id the ID.
   */
  private static void putIn(@NotNull final Class<? extends PacketIn> cls, @NotNull final PlayerConnection.State state,
                            final int id) {
    PacketRegistry.put(cls, state, PacketBound.SERVER, id);
  }

  /**
   * puts the given packet class into the map with the given ID, and also inserts the constructor into the CTOR cache.
   *
   * @param cls the class.
   * @param id the ID.
   */
  private static void putOut(@NotNull final Class<? extends PacketOut> cls, @NotNull final PlayerConnection.State state,
                             final int id) {
    PacketRegistry.put(cls, state, PacketBound.CLIENT, id);
  }

  /**
   * combines the data into a single value which is used to locate the a packet inside of the register.
   *
   * @param bound the bound of the packet.
   * @param state the packet getState.
   * @param id the packet ID.
   *
   * @return the compressed packet represented as an integer.
   */
  private static int shift(@NotNull final PlayerConnection.State state, @NotNull final PacketBound bound,
                           final int id) {
    var identifier = id;
    if (state != PlayerConnection.State.ANY) {
      identifier |= state.ordinal() << 27;
    }
    identifier |= bound.ordinal() << 31;
    return identifier;
  }
}
