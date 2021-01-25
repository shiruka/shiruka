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

package net.shiruka.shiruka.event;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import net.shiruka.api.events.LoginDataEvent;
import net.shiruka.api.geometry.AnimatedTextureType;
import net.shiruka.api.geometry.AnimationData;
import net.shiruka.api.geometry.ImageData;
import net.shiruka.api.geometry.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link LoginDataEvent.ChainData}.
 */
public final class SimpleChainData implements LoginDataEvent.ChainData {

  /**
   * the key of chain.
   */
  private static final String CHAIN = "chain";

  /**
   * the key of identity public key.
   */
  private static final String IDENTITY_PUBLIC_KEY = "identityPublicKey";

  /**
   * the key of image height.
   */
  private static final String IMAGE_HEIGHT = "ImageHeight";

  /**
   * the key of image width.
   */
  private static final String IMAGE_WIDTH = "ImageWidth";

  /**
   * the Mojang public key.
   */
  private static final PublicKey MOJANG_PUBLIC_KEY;

  /**
   * the Mojang public key as {@link Base64} format.
   */
  private static final String MOJANG_PUBLIC_KEY_BASE64 =
    "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";

  /**
   * the chain data itself.
   */
  @NotNull
  private final String chainData;

  /**
   * the skin data itself.
   */
  @NotNull
  private final String skinData;

  /**
   * the current input mode.
   */
  private int currentInputMode;

  /**
   * the default input mode.
   */
  private int defaultInputMode;

  /**
   * the device id.
   */
  @Nullable
  private String deviceId;

  /**
   * the device model.
   */
  @Nullable
  private String deviceModel;

  /**
   * the device OS.
   */
  private int deviceOS;

  /**
   * the game version.
   */
  @Nullable
  private String gameVersion;

  /**
   * the gui scale.
   */
  private int guiScale;

  /**
   * the client id.
   */
  private long id;

  /**
   * te language code.
   */
  private String languageCode;

  /**
   * the public key.
   */
  @Nullable
  private String publicKey;

  /**
   * the server address.
   */
  @Nullable
  private String serverAddress;

  /**
   * the skin.
   */
  @Nullable
  private Skin skin;

  /**
   * the ui profile.
   */
  private int uiProfile;

  /**
   * the client unique id.
   */
  @Nullable
  private UUID uniqueId;

  /**
   * the username.
   */
  @Nullable
  private String username;

  /**
   * the xbox authed.
   */
  private boolean xboxAuthed;

  /**
   * the xbox id.
   */
  @Nullable
  private String xuid;

  static {
    try {
      MOJANG_PUBLIC_KEY = SimpleChainData.generateKey(SimpleChainData.MOJANG_PUBLIC_KEY_BASE64);
    } catch (final InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * ctor.
   *
   * @param chainData the chain data.
   * @param skinData the skin data.
   */
  private SimpleChainData(@NotNull final String chainData, @NotNull final String skinData) {
    this.chainData = chainData;
    this.skinData = skinData;
  }

  /**
   * creates a new instance of {@code this} then use {@link #initialize()} method to initiate the chain data.
   *
   * @param chainData the chain data to create.
   * @param skinData the skin data to create.
   *
   * @return a new instance of {@link LoginDataEvent.ChainData}.
   */
  @NotNull
  public static LoginDataEvent.ChainData create(@NotNull final String chainData,
                                                @NotNull final String skinData) {
    final var data = new SimpleChainData(chainData, skinData);
    data.initialize();
    return data;
  }

  /**
   * decodes the token.
   *
   * @param token the token to decode.
   *
   * @return decoded token.
   */
  @NotNull
  private static JsonObject decodeToken(@NotNull final String token) {
    final var base = token.split("\\.");
    Preconditions.checkArgument(base.length >= 2, "Invalid token length");
    try {
      final var json = new String(Base64.getDecoder().decode(base[1]), StandardCharsets.UTF_8);
      return Json.parse(json).asObject();
    } catch (final Exception e) {
      throw new IllegalArgumentException("Invalid token JSON", e);
    }
  }

  /**
   * generates and returns a new generated public key from the given base64 value.
   *
   * @param base64 the base64 value to generator.
   *
   * @return a new public key instance.
   *
   * @throws NoSuchAlgorithmException if no {@code Provider} supports a
   *   {@code KeyFactorySpi} implementation for the
   *   specified algorithm.
   * @throws InvalidKeySpecException if the given key specification
   *   is inappropriate for this key factory to produce a public key.
   * @throws NullPointerException if {@code algorithm} is {@code null}.
   */
  @NotNull
  private static PublicKey generateKey(@NotNull final String base64) throws NoSuchAlgorithmException,
    InvalidKeySpecException {
    return KeyFactory.getInstance("EC")
      .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
  }

  /**
   * gets the given value as {@link NotNull}.
   *
   * @param value the value to get.
   * @param <V> type of the value.
   *
   * @return the value itself.
   *
   * @throws NullPointerException if the given value is {@code null}.
   */
  @NotNull
  private static <V> V get(@Nullable final V value) {
    return Objects.requireNonNull(value, "Please run #initialize() method before get SimpleChainData values!");
  }

  /**
   * gives an {@link AnimationData} instance from the given json.
   *
   * @param json the json to convert.
   *
   * @return a new instance of {@link AnimationData}.
   */
  @NotNull
  private static AnimationData getAnimation(@NotNull final JsonObject json) {
    final var frames = json.get("Frames").asFloat();
    final var type = AnimatedTextureType.values()[json.get("Type").asInt()];
    final var data = Base64.getDecoder().decode(json.get("Image").asString());
    final var width = json.get(SimpleChainData.IMAGE_WIDTH).asInt();
    final var height = json.get(SimpleChainData.IMAGE_HEIGHT).asInt();
    return new AnimationData(frames, ImageData.of(width, height, data), type);
  }

  /**
   * gives an {@link ImageData} instance from the given json and name.
   *
   * @param json the json to convert.
   * @param name the name to convert.
   *
   * @return a new instance of {@link ImageData}.
   */
  @NotNull
  private static ImageData getImage(@NotNull final JsonObject json, @NotNull final String name) {
    final var keys = json.names();
    if (!keys.contains(name + "Data")) {
      return ImageData.empty();
    }
    final var skinImage = Base64.getDecoder().decode(json.get(name + "Data").asString());
    if (!keys.contains(name + SimpleChainData.IMAGE_HEIGHT) ||
      !keys.contains(name + SimpleChainData.IMAGE_WIDTH)) {
      return ImageData.of(skinImage);
    }
    final var width = json.get(name + SimpleChainData.IMAGE_WIDTH).asInt();
    final var height = json.get(name + SimpleChainData.IMAGE_HEIGHT).asInt();
    return ImageData.of(width, height, skinImage);
  }

  /**
   * creates a new {@link Skin} from the given skin token.
   *
   * @param json the json to create.
   *
   * @return a new instance of {@link Skin}.
   */
  @NotNull
  private static Skin getSkin(@NotNull final JsonObject json) {
    final var skinBuilder = Skin.builder();
    final var keys = json.names();
    if (keys.contains("SkinId")) {
      skinBuilder.skinId(json.get("SkinId").asString());
    }
    if (keys.contains("CapeId")) {
      skinBuilder.capeId(json.get("CapeId").asString());
    }
    skinBuilder.skinData(SimpleChainData.getImage(json, "Skin"));
    skinBuilder.capeData(SimpleChainData.getImage(json, "Cape"));
    if (keys.contains("PremiumSkin")) {
      skinBuilder.premium(json.get("PremiumSkin").asBoolean());
    }
    if (keys.contains("PersonaSkin")) {
      skinBuilder.persona(json.get("PersonaSkin").asBoolean());
    }
    if (keys.contains("CapeOnClassicSkin")) {
      skinBuilder.capeOnClassic(json.get("CapeOnClassicSkin").asBoolean());
    }
    if (keys.contains("SkinResourcePatch")) {
      skinBuilder.skinResourcePatch(new String(Base64.getDecoder().decode(json.get("SkinResourcePatch").asString()), StandardCharsets.UTF_8));
    }
    if (keys.contains("SkinGeometryData")) {
      skinBuilder.geometryData(new String(Base64.getDecoder().decode(json.get("SkinGeometryData").asString()), StandardCharsets.UTF_8));
    }
    if (keys.contains("SkinAnimationData")) {
      skinBuilder.animationData(new String(Base64.getDecoder().decode(json.get("SkinAnimationData").asString()), StandardCharsets.UTF_8));
    }
    if (!keys.contains("AnimatedImageData")) {
      return skinBuilder.build();
    }
    final var animations = new ArrayList<AnimationData>();
    final var jsonArray = json.get("AnimatedImageData").asArray();
    jsonArray.forEach(jsonValue ->
      animations.add(SimpleChainData.getAnimation(jsonValue.asObject())));
    skinBuilder.animations(animations);
    return skinBuilder.build();
  }

  /**
   * verifies the given key and object.
   *
   * @param key the key to verify.
   * @param object the object to verify.
   *
   * @return {@code true} if the key and object verified.
   *
   * @throws JOSEException if something went wrong when verifying the given key and object.
   */
  private static boolean verify(@NotNull final PublicKey key, @NotNull final JWSObject object) throws JOSEException {
    final var verifier = new DefaultJWSVerifierFactory().createJWSVerifier(object.getHeader(), key);
    return object.verify(verifier);
  }

  /**
   * verifies the given chains.
   *
   * @param chains the chains to verify.
   *
   * @return {@code true} if the given chains correct.
   */
  private static boolean verifyChain(@NotNull final List<String> chains) throws ParseException, JOSEException,
    NoSuchAlgorithmException, InvalidKeySpecException {
    PublicKey lastKey = null;
    var mojangKeyVerified = false;
    for (final var chain : chains) {
      final var jws = JWSObject.parse(chain);
      if (!mojangKeyVerified) {
        mojangKeyVerified = SimpleChainData.verify(SimpleChainData.MOJANG_PUBLIC_KEY, jws);
      }
      if (lastKey != null && !SimpleChainData.verify(lastKey, jws)) {
        throw new JOSEException("Unable to verify key in chain.");
      }
      final var payload = jws.getPayload().toJSONObject();
      final var base64key = payload.get(SimpleChainData.IDENTITY_PUBLIC_KEY);
      Preconditions.checkState(base64key instanceof String, "No key found");
      lastKey = SimpleChainData.generateKey((String) base64key);
    }
    return mojangKeyVerified;
  }

  @Override
  public int getCurrentInputMode() {
    return this.currentInputMode;
  }

  @Override
  public int getDefaultInputMode() {
    return this.defaultInputMode;
  }

  @NotNull
  @Override
  public String getDeviceId() {
    return SimpleChainData.get(this.deviceId);
  }

  @NotNull
  @Override
  public String getDeviceModel() {
    return SimpleChainData.get(this.deviceModel);
  }

  @Override
  public int getDeviceOS() {
    return this.deviceOS;
  }

  @NotNull
  @Override
  public String getGameVersion() {
    return SimpleChainData.get(this.gameVersion);
  }

  @Override
  public int getGuiScale() {
    return this.guiScale;
  }

  @Override
  public long getId() {
    return this.id;
  }

  @NotNull
  @Override
  public String getLanguageCode() {
    return SimpleChainData.get(this.languageCode);
  }

  @NotNull
  @Override
  public String getPublicKey() {
    return SimpleChainData.get(this.publicKey);
  }

  @NotNull
  @Override
  public String getServerAddress() {
    return SimpleChainData.get(this.serverAddress);
  }

  @NotNull
  @Override
  public Skin getSkin() {
    return SimpleChainData.get(this.skin);
  }

  @Override
  public int getUiProfile() {
    return this.uiProfile;
  }

  @NotNull
  @Override
  public UUID getUniqueId() {
    return SimpleChainData.get(this.uniqueId);
  }

  @NotNull
  @Override
  public String getUsername() {
    return SimpleChainData.get(this.username);
  }

  @NotNull
  @Override
  public String getXUniqueId() {
    return SimpleChainData.get(this.xuid);
  }

  @Override
  public boolean getXboxAuthed() {
    return this.xboxAuthed;
  }

  /**
   * decodes the chain data.
   */
  private void decodeChainData() {
    final JsonObject parsed;
    try {
      parsed = Json.parse(this.chainData).asObject();
    } catch (final Exception e) {
      throw new IllegalArgumentException("Invalid JSON", e);
    }
    if (parsed.isEmpty() || !parsed.names().contains(SimpleChainData.CHAIN) ||
      parsed.get(SimpleChainData.CHAIN).asArray().isEmpty()) {
      return;
    }
    final var chains = parsed.get(SimpleChainData.CHAIN).asArray().values().stream()
      .map(JsonValue::asString)
      .collect(Collectors.toList());
    try {
      this.xboxAuthed = SimpleChainData.verifyChain(chains);
    } catch (final Exception e) {
      this.xboxAuthed = false;
    }
    chains.stream()
      .map(SimpleChainData::decodeToken)
      .forEach(chainMap -> {
        final var keys = chainMap.names();
        if (!keys.contains("extraData") &&
          keys.contains(SimpleChainData.IDENTITY_PUBLIC_KEY)) {
          this.publicKey = chainMap.get(SimpleChainData.IDENTITY_PUBLIC_KEY).asString();
          return;
        }
        final var extra = chainMap.get("extraData").asObject();
        final var extrasKeys = extra.names();
        if (extrasKeys.contains("displayName")) {
          this.username = extra.get("displayName").asString();
        }
        if (extrasKeys.contains("identity")) {
          this.uniqueId = UUID.fromString(extra.get("identity").asString());
        }
        if (extrasKeys.contains("XUID")) {
          this.xuid = extra.get("XUID").asString();
        }
        if (keys.contains(SimpleChainData.IDENTITY_PUBLIC_KEY)) {
          this.publicKey = chainMap.get(SimpleChainData.IDENTITY_PUBLIC_KEY).asString();
        }
      });
    if (!this.xboxAuthed) {
      this.xuid = null;
    }
  }

  /**
   * decodes and sets the skin data to {@link this#skin}.
   */
  private void decodeSkinData() {
    final var skinToken = SimpleChainData.decodeToken(this.skinData);
    if (skinToken.names().contains("ClientRandomId")) {
      this.id = skinToken.get("ClientRandomId").asLong();
    }
    if (skinToken.names().contains("ServerAddress")) {
      this.serverAddress = skinToken.get("ServerAddress").asString();
    }
    if (skinToken.names().contains("DeviceModel")) {
      this.deviceModel = skinToken.get("DeviceModel").asString();
    }
    if (skinToken.names().contains("DeviceOS")) {
      this.deviceOS = skinToken.get("DeviceOS").asInt();
    }
    if (skinToken.names().contains("DeviceId")) {
      this.deviceId = skinToken.get("DeviceId").asString();
    }
    if (skinToken.names().contains("GameVersion")) {
      this.gameVersion = skinToken.get("GameVersion").asString();
    }
    if (skinToken.names().contains("GuiScale")) {
      this.guiScale = skinToken.get("GuiScale").asInt();
    }
    if (skinToken.names().contains("LanguageCode")) {
      this.languageCode = skinToken.get("LanguageCode").asString();
    }
    if (skinToken.names().contains("CurrentInputMode")) {
      this.currentInputMode = skinToken.get("CurrentInputMode").asInt();
    }
    if (skinToken.names().contains("DefaultInputMode")) {
      this.defaultInputMode = skinToken.get("DefaultInputMode").asInt();
    }
    if (skinToken.names().contains("UIProfile")) {
      this.uiProfile = skinToken.get("UIProfile").asInt();
    }
    this.skin = SimpleChainData.getSkin(skinToken);
  }

  /**
   * initiates the chain data.
   */
  private void initialize() {
    this.decodeChainData();
    this.decodeSkinData();
  }
}
