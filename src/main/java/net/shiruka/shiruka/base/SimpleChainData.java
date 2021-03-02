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

package net.shiruka.shiruka.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shiruka.api.Shiruka;
import net.shiruka.api.base.ChainData;
import net.shiruka.api.geometry.AnimatedTextureType;
import net.shiruka.api.geometry.AnimationData;
import net.shiruka.api.geometry.ImageData;
import net.shiruka.api.geometry.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link ChainData}.
 */
@RequiredArgsConstructor
public final class SimpleChainData implements ChainData {

  /**
   * the map type reference.
   */
  private static final TypeReference<Map<String, List<String>>> MAP_TYPE_REFERENCE = new TypeReference<>() {
  };

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
  @Getter
  private final String chainData;

  /**
   * the skin data itself.
   */
  @NotNull
  @Getter
  private final String skinData;

  /**
   * the current input mode.
   */
  @Getter
  private int currentInputMode;

  /**
   * the default input mode.
   */
  @Getter
  private int defaultInputMode;

  /**
   * the device id.
   */
  @Nullable
  @Getter
  private String deviceId;

  /**
   * the device model.
   */
  @Nullable
  @Getter
  private String deviceModel;

  /**
   * the device OS.
   */
  @Getter
  private int deviceOS;

  /**
   * the game version.
   */
  @Nullable
  @Getter
  private String gameVersion;

  /**
   * the gui scale.
   */
  @Getter
  private int guiScale;

  /**
   * the client id.
   */
  @Getter
  private long id;

  /**
   * te language code.
   */
  @Getter
  private String languageCode;

  /**
   * the public key.
   */
  @Nullable
  @Getter
  private String publicKey;

  /**
   * the server address.
   */
  @Nullable
  @Getter
  private String serverAddress;

  /**
   * the skin.
   */
  @Nullable
  @Getter
  private Skin skin;

  /**
   * the ui profile.
   */
  @Getter
  private int uiProfile;

  /**
   * the client unique id.
   */
  @Nullable
  @Getter
  private UUID uniqueId;

  /**
   * the username.
   */
  @Nullable
  @Getter
  private String username;

  /**
   * the xbox authed.
   */
  @Getter
  private boolean xboxAuthed;

  /**
   * the xbox id.
   */
  @Nullable
  @Getter
  private String xboxUniqueId;

  static {
    try {
      MOJANG_PUBLIC_KEY = SimpleChainData.generateKey(SimpleChainData.MOJANG_PUBLIC_KEY_BASE64);
    } catch (final InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * creates a new instance of {@code this} then use {@link #initialize()} method to initiate the chain data.
   *
   * @param chainData the chain data to create.
   * @param skinData the skin data to create.
   *
   * @return a new instance of {@link ChainData}.
   */
  @NotNull
  public static ChainData create(@NotNull final String chainData, @NotNull final String skinData) {
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
  private static JsonNode decodeToken(@NotNull final String token) {
    final var base = token.split("\\.");
    Preconditions.checkArgument(base.length >= 2, "Invalid token length");
    final var json = new String(Base64.getDecoder().decode(base[1]), StandardCharsets.UTF_8);
    try {
      return Shiruka.JSON_MAPPER.readTree(json);
    } catch (final IOException e) {
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
   * @throws NoSuchAlgorithmException if no {@code Provider} supports a {@code KeyFactorySpi} implementation for the
   *   specified algorithm.
   * @throws InvalidKeySpecException if the given key specification is inappropriate for this key factory to produce a
   *   public key.
   * @throws NullPointerException if {@code algorithm} is {@code null}.
   */
  @NotNull
  private static PublicKey generateKey(@NotNull final String base64) throws NoSuchAlgorithmException,
    InvalidKeySpecException {
    return KeyFactory.getInstance("EC")
      .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
  }

  /**
   * gives an {@link AnimationData} instance from the given json.
   *
   * @param json the json to convert.
   *
   * @return a new instance of {@link AnimationData}.
   */
  @NotNull
  private static AnimationData getAnimation(@NotNull final JsonNode json) {
    final var frames = json.get("Frames").floatValue();
    final var type = AnimatedTextureType.values()[json.get("Type").intValue()];
    final var data = Base64.getDecoder().decode(json.get("Image").textValue());
    final var width = json.get("ImageWidth").intValue();
    final var height = json.get("ImageHeight").intValue();
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
  private static ImageData getImage(@NotNull final JsonNode json, @NotNull final String name) {
    if (!json.has(name + "Data")) {
      return ImageData.empty();
    }
    final var skinImage = Base64.getDecoder().decode(json.get(name + "Data").textValue());
    if (!json.has(name + "ImageHeight") || !json.has(name + "ImageWidth")) {
      return ImageData.of(skinImage);
    }
    final var width = json.get(name + "ImageWidth").intValue();
    final var height = json.get(name + "ImageHeight").intValue();
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
  private static Skin getSkin(@NotNull final JsonNode json) {
    final var skinBuilder = Skin.builder();
    if (json.has("SkinId")) {
      skinBuilder.skinId(json.get("SkinId").textValue());
    }
    if (json.has("CapeId")) {
      skinBuilder.capeId(json.get("CapeId").textValue());
    }
    skinBuilder.skinData(SimpleChainData.getImage(json, "Skin"));
    skinBuilder.capeData(SimpleChainData.getImage(json, "Cape"));
    if (json.has("PremiumSkin")) {
      skinBuilder.premium(json.get("PremiumSkin").booleanValue());
    }
    if (json.has("PersonaSkin")) {
      skinBuilder.persona(json.get("PersonaSkin").booleanValue());
    }
    if (json.has("CapeOnClassicSkin")) {
      skinBuilder.capeOnClassic(json.get("CapeOnClassicSkin").booleanValue());
    }
    if (json.has("SkinResourcePatch")) {
      skinBuilder.skinResourcePatch(new String(Base64.getDecoder().decode(json.get("SkinResourcePatch").textValue()), StandardCharsets.UTF_8));
    }
    if (json.has("SkinGeometryData")) {
      skinBuilder.geometryData(new String(Base64.getDecoder().decode(json.get("SkinGeometryData").textValue()), StandardCharsets.UTF_8));
    }
    if (json.has("SkinAnimationData")) {
      skinBuilder.animationData(new String(Base64.getDecoder().decode(json.get("SkinAnimationData").textValue()), StandardCharsets.UTF_8));
    }
    if (json.has("AnimatedImageData")) {
      final var animations = new ObjectArrayList<AnimationData>();
      final var array = json.get("AnimatedImageData");
      for (final var element : array) {
        animations.add(SimpleChainData.getAnimation(element));
      }
      skinBuilder.animations(animations);
    }
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
      final var base64key = payload.get("identityPublicKey");
      Preconditions.checkState(base64key instanceof String, "No key found");
      lastKey = SimpleChainData.generateKey((String) base64key);
    }
    return mojangKeyVerified;
  }

  /**
   * decodes the chain data.
   */
  private void decodeChainData() {
    final Map<String, List<String>> map;
    try {
      map = Shiruka.JSON_MAPPER.readValue(this.chainData, SimpleChainData.MAP_TYPE_REFERENCE);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Invalid JSON", e);
    }
    if (map.isEmpty() || !map.containsKey("chain") || map.get("chain").isEmpty()) {
      return;
    }
    final var chains = map.get("chain");
    try {
      this.xboxAuthed = SimpleChainData.verifyChain(chains);
    } catch (final Exception e) {
      this.xboxAuthed = false;
    }
    chains.stream()
      .map(SimpleChainData::decodeToken)
      .forEach(chainMap -> {
        if (chainMap.has("extraData")) {
          final JsonNode extra = chainMap.get("extraData");
          if (extra.has("displayName")) {
            this.username = extra.get("displayName").textValue();
          }
          if (extra.has("identity")) {
            this.uniqueId = UUID.fromString(extra.get("identity").textValue());
          }
          if (extra.has("XUID")) {
            this.xboxUniqueId = extra.get("XUID").textValue();
          }
        }
        if (chainMap.has("identityPublicKey")) {
          this.publicKey = chainMap.get("identityPublicKey").textValue();
        }
      });
    if (!this.xboxAuthed) {
      this.xboxUniqueId = null;
    }
  }

  /**
   * decodes and sets the skin data to {@link this#skin}.
   */
  private void decodeSkinData() {
    final var skinToken = SimpleChainData.decodeToken(this.skinData);
    if (skinToken.has("ClientRandomId")) {
      this.id = skinToken.get("ClientRandomId").longValue();
    }
    if (skinToken.has("ServerAddress")) {
      this.serverAddress = skinToken.get("ServerAddress").textValue();
    }
    if (skinToken.has("DeviceModel")) {
      this.deviceModel = skinToken.get("DeviceModel").textValue();
    }
    if (skinToken.has("DeviceOS")) {
      this.deviceOS = skinToken.get("DeviceOS").intValue();
    }
    if (skinToken.has("DeviceId")) {
      this.deviceId = skinToken.get("DeviceId").textValue();
    }
    if (skinToken.has("GameVersion")) {
      this.gameVersion = skinToken.get("GameVersion").textValue();
    }
    if (skinToken.has("GuiScale")) {
      this.guiScale = skinToken.get("GuiScale").intValue();
    }
    if (skinToken.has("LanguageCode")) {
      this.languageCode = skinToken.get("LanguageCode").textValue();
    }
    if (skinToken.has("CurrentInputMode")) {
      this.currentInputMode = skinToken.get("CurrentInputMode").intValue();
    }
    if (skinToken.has("DefaultInputMode")) {
      this.defaultInputMode = skinToken.get("DefaultInputMode").intValue();
    }
    if (skinToken.has("UIProfile")) {
      this.uiProfile = skinToken.get("UIProfile").intValue();
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
