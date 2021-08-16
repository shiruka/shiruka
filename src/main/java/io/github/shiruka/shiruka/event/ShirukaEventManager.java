package io.github.shiruka.shiruka.event;

import io.github.shiruka.api.event.EventManager;
import io.github.shiruka.api.event.method.MethodAdapter;
import io.github.shiruka.api.event.method.SimpleMethodAdapter;
import lombok.experimental.Delegate;

/**
 * a class that represents Shiru ka's event manager.
 */
public final class ShirukaEventManager implements EventManager {

  /**
   * the adapter.
   */
  @Delegate
  private final MethodAdapter adapter = new SimpleMethodAdapter();
}
