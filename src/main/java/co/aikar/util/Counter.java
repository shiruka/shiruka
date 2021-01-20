package co.aikar.util;

import com.google.common.collect.ForwardingMap;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Counter<T> extends ForwardingMap<T, Long> {

  private final Map<T, Long> counts = new HashMap<>();

  public long decrement(@Nullable final T key) {
    return this.increment(key, -1);
  }

  public long decrement(@Nullable final T key, final long amount) {
    return this.decrement(key, -amount);
  }

  public long getCount(@Nullable final T key) {
    return this.counts.getOrDefault(key, 0L);
  }

  public long increment(@Nullable final T key, final long amount) {
    var count = this.getCount(key);
    count += amount;
    this.counts.put(key, count);
    return count;
  }

  public long increment(@Nullable final T key) {
    return this.increment(key, 1);
  }

  @NotNull
  @Override
  protected Map<T, Long> delegate() {
    return this.counts;
  }
}
