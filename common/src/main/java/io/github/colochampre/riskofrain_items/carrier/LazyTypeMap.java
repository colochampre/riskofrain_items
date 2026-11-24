package io.github.colochampre.riskofrain_items.carrier;

import io.github.colochampre.riskofrain_items.RoRMod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;

/**
 * Internal helper, not part of the API: a map keyed by entity types that other mods hand over as suppliers during
 * mod init, before their types exist on Forge. The suppliers are resolved on the first lookup, which only happens
 * once mobs tick or render, after the registries are populated.
 * <p>
 * A supplier is resolved outside the registration lock, so one that registers again or looks the map up (directly or
 * through class initialization) neither breaks the iteration nor blocks other registrations. A supplier that throws or
 * returns null is logged and dropped: by the time of the first lookup the registries are final, so a retry could not
 * succeed. A duplicate type keeps the first value.
 */
public final class LazyTypeMap<V> {

  private final String what;
  private final Map<EntityType<?>, V> resolved = new ConcurrentHashMap<>();
  private final List<Pending<V>> pending = new ArrayList<>();
  /** Serializes resolutions, so a lookup never sees a half resolved batch from another thread. */
  private final Object resolveLock = new Object();
  private volatile boolean hasPending;
  private volatile boolean hasAny;

  private record Pending<V>(Supplier<? extends EntityType<?>> type, V value) {
  }

  /** @param what names the registered values in the log, for example "carrier rule" */
  public LazyTypeMap(String what) {
    this.what = what;
  }

  public void register(Supplier<? extends EntityType<?>> type, V value) {
    if (type == null || value == null) {
      throw new IllegalArgumentException("the type and the " + what + " must not be null");
    }
    synchronized (pending) {
      pending.add(new Pending<>(type, value));
      hasPending = true;
      hasAny = true;
    }
  }

  /** The value registered for the type, or {@code null} when there is none. */
  public V get(EntityType<?> type) {
    if (hasPending) {
      resolvePending();
    }
    return resolved.get(type);
  }

  /** Whether any value is registered or still waiting to be resolved; never triggers a resolution. */
  public boolean hasAny() {
    return hasAny;
  }

  private void resolvePending() {
    if (Thread.holdsLock(resolveLock)) {
      // A supplier looked the map up while being resolved: leave the batch to the outer call.
      return;
    }
    synchronized (resolveLock) {
      List<Pending<V>> batch;
      synchronized (pending) {
        batch = new ArrayList<>(pending);
        pending.clear();
      }
      for (Pending<V> entry : batch) {
        EntityType<?> type;
        try {
          type = entry.type().get();
        } catch (RuntimeException | LinkageError e) {
          // LinkageError: a supplier touching a class of a missing optional mod must not take the rest of the batch.
          RoRMod.LOGGER.error("Could not resolve the entity type of a {}; it is ignored", what, e);
          continue;
        }
        if (type == null) {
          RoRMod.LOGGER.error("A {} supplied a null entity type; it is ignored", what);
        } else if (resolved.putIfAbsent(type, entry.value()) != null) {
          RoRMod.LOGGER.error("A {} is already registered for {}; the duplicate is ignored", what, type);
        }
      }
      synchronized (pending) {
        // A supplier may have registered more entries meanwhile: they stay pending for the next lookup.
        hasPending = !pending.isEmpty();
        hasAny = hasPending || !resolved.isEmpty();
      }
    }
  }
}
