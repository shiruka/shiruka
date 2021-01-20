package co.aikar.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides Utility methods that assist with generating JSON Objects
 */
@SuppressWarnings({"rawtypes", "SuppressionAnnotation"})
public final class JSONUtil {

  private JSONUtil() {
  }

  /**
   * This appends multiple key/value Obj pairs into a JSON Object
   *
   * @param parent Map to be appended to
   * @param data Data to append
   *
   * @return Map
   */
  @NotNull
  public static Map<String, Object> appendObjectData(@NotNull final Map parent, @NotNull final JSONPair... data) {
    for (final var JSONPair : data) {
      parent.put(JSONPair.key, JSONPair.val);
    }
    return parent;
  }

  /**
   * Creates a new JSON object from multiple JSONPair key/value pairs
   *
   * @param data JSONPairs
   *
   * @return Map
   */
  @NotNull
  public static Map<String, Object> createObject(@NotNull final JSONPair... data) {
    return JSONUtil.appendObjectData(new LinkedHashMap(), data);
  }

  /**
   * Creates a key/value "JSONPair" object
   *
   * @param key Key to use
   * @param obj Value to use
   *
   * @return JSONPair
   */
  @NotNull
  public static JSONPair pair(@NotNull final String key, @Nullable final Object obj) {
    return new JSONPair(key, obj);
  }

  @NotNull
  public static JSONPair pair(final long key, @Nullable final Object obj) {
    return new JSONPair(String.valueOf(key), obj);
  }

  /**
   * This builds a JSON array from a set of data
   *
   * @param data Data to build JSON array from
   *
   * @return List
   */
  @NotNull
  public static List<Object> toArray(@NotNull final Object... data) {
    return Lists.newArrayList(data);
  }

  /**
   * These help build a single JSON array using a mapper function
   *
   * @param collection Collection to apply to
   * @param mapper Mapper to apply
   * @param <E> Element Type
   *
   * @return List
   */
  @NotNull
  public static <E> List<Object> toArrayMapper(@NotNull final E[] collection, @NotNull final Function<E, Object> mapper) {
    return JSONUtil.toArrayMapper(Lists.newArrayList(collection), mapper);
  }

  @NotNull
  public static <E> List<Object> toArrayMapper(@NotNull final Iterable<E> collection, @NotNull final Function<E, Object> mapper) {
    final var array = Lists.newArrayList();
    for (final var e : collection) {
      final var object = mapper.apply(e);
      if (object != null) {
        array.add(object);
      }
    }
    return array;
  }

  /**
   * These help build a single JSON Object from a collection, using a mapper function
   *
   * @param collection Collection to apply to
   * @param mapper Mapper to apply
   * @param <E> Element Type
   *
   * @return Map
   */
  @NotNull
  public static <E> Map toObjectMapper(@NotNull final E[] collection, @NotNull final Function<E, JSONPair> mapper) {
    return JSONUtil.toObjectMapper(Lists.newArrayList(collection), mapper);
  }

  @NotNull
  public static <E> Map toObjectMapper(@NotNull final Iterable<E> collection, @NotNull final Function<E, JSONPair> mapper) {
    final Map object = Maps.newLinkedHashMap();
    for (final var e : collection) {
      final var JSONPair = mapper.apply(e);
      if (JSONPair != null) {
        object.put(JSONPair.key, JSONPair.val);
      }
    }
    return object;
  }

  /**
   * Simply stores a key and a value, used internally by many methods below.
   */
  @SuppressWarnings("PublicInnerClass")
  public static class JSONPair {

    final String key;

    final Object val;

    JSONPair(@NotNull final String key, @NotNull final Object val) {
      this.key = key;
      this.val = val;
    }
  }
}
