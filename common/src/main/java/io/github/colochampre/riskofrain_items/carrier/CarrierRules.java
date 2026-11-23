package io.github.colochampre.riskofrain_items.carrier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Registration point for other mods that hand their own entity types to the Risk of Rain item carrier system.
 * <p>
 * A registered type is decided by its rule alone: the rule takes precedence over the {@code InventoryCarrier}
 * exclusion, the four group settings, the boss tags and {@code #riskofrain_items:pickup_allowed}. Only
 * {@code #riskofrain_items:pickup_excluded} still wins, so a data pack can always turn a mob off. Unregistered types
 * keep the default rules of {@link CarrierEligibility}. Everything else (pickup scan, capacity, save data, death drop,
 * conversions, despawn rule, owner take-back, holder effects, icons) applies to registered mobs as it does to any
 * other carrier.
 * <p>
 * Call {@link #register} once per type from your mod initializer, on the main thread. It is safe to call before or
 * after riskofrain_items has initialized, because nothing is read until mobs tick. The map is concurrent, so a
 * registration from a second loader thread is also safe, but a type must be registered only once.
 * <p>
 * The rule runs on the server thread, for the mob that scans for items, once per pickup scan (every few ticks per
 * mob), so it must be cheap and must not modify the world. Example, with a mod that wants its tamed drones to carry
 * items even though they are not hostile:
 * <pre>{@code
 * CarrierRules.register(MyEntities.DRONE.get(), mob -> mob instanceof Drone drone && drone.isActive());
 * }</pre>
 * Mods that keep working without riskofrain_items must call this only behind a loaded-mod check, from a class that
 * is not loaded otherwise.
 * <p>
 * A mob that leaves the world without dying (a drone breaking down, a turret picked up as an item) should call
 * {@link CarriedItems#dropAll(Mob)} first, so the carried items are not lost.
 */
public final class CarrierRules {

  private static final Map<EntityType<?>, Predicate<Mob>> RULES = new ConcurrentHashMap<>();

  private CarrierRules() {
  }

  /**
   * Hands an entity type to the carrier system.
   *
   * @param type the entity type of the mobs that can carry items
   * @param rule decides per mob whether it can pick up and carry items right now; it must be cheap and have no side
   *             effects. Returning {@code false} makes the mob stop picking up, but it keeps what it already carries
   * @throws IllegalStateException if the type already has a rule
   */
  public static void register(EntityType<?> type, Predicate<Mob> rule) {
    if (type == null || rule == null) {
      throw new IllegalArgumentException("type and rule must not be null");
    }
    if (RULES.putIfAbsent(type, rule) != null) {
      throw new IllegalStateException("Carrier rule already registered for " + type);
    }
  }

  /** The rule registered for the type, or {@code null} when the type follows the default rules. */
  static Predicate<Mob> get(EntityType<?> type) {
    return RULES.get(type);
  }

  /** Whether any mod registered a rule; the pickup scan must run for those types even with every group off. */
  static boolean hasAny() {
    return !RULES.isEmpty();
  }
}
