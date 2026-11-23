package io.github.colochampre.riskofrain_items.carrier;

import java.util.function.Predicate;
import java.util.function.Supplier;
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
 * Call {@link #register} once per type from your mod initializer. The type is a {@link Supplier}, resolved the first
 * time a mob is checked (after registries are populated), so it is safe to call during mod init on Forge, before your
 * entity types exist, and before or after riskofrain_items has initialized. A type must be registered only once; a
 * supplier that throws or returns null at that point, or a duplicate type, is logged and ignored.
 * <p>
 * The rule runs on the server thread, for the mob that scans for items, once per pickup scan (every few ticks per
 * mob), so it must be cheap and must not modify the world. Example, with a mod that wants its tamed drones to carry
 * items even though they are not hostile:
 * <pre>{@code
 * CarrierRules.register(MyEntities.DRONE, mob -> mob instanceof Drone drone && drone.isActive());
 * }</pre>
 * where {@code MyEntities.DRONE} is a {@code RegistrySupplier<EntityType<Drone>>} (or any {@code Supplier}, such as
 * {@code () -> MyEntities.DRONE.get()}); do not call {@code get()} yourself at init.
 * Mods that keep working without riskofrain_items must call this only behind a loaded-mod check, from a class that
 * is not loaded otherwise.
 * <p>
 * A mob that leaves the world without dying (a drone breaking down, a turret picked up as an item) should call
 * {@link CarriedItems#dropAll(Mob)} first, so the carried items are not lost.
 */
public final class CarrierRules {

  private static final LazyTypeMap<Predicate<Mob>> RULES = new LazyTypeMap<>("carrier rule");

  private CarrierRules() {
  }

  /**
   * Hands an entity type to the carrier system. The type is resolved lazily, so a registry supplier can be passed
   * straight from mod init.
   *
   * @param type supplies the entity type of the mobs that can carry items; called once, when first needed
   * @param rule decides per mob whether it can pick up and carry items right now; it must be cheap and have no side
   *             effects. Returning {@code false} makes the mob stop picking up, but it keeps what it already carries
   * @throws IllegalArgumentException if an argument is null
   */
  public static void register(Supplier<? extends EntityType<?>> type, Predicate<Mob> rule) {
    RULES.register(type, rule);
  }

  /** The rule registered for the type, or {@code null} when the type follows the default rules. */
  static Predicate<Mob> get(EntityType<?> type) {
    return RULES.get(type);
  }

  /** Whether any mod registered a rule; the pickup scan must run for those types even with every group off. */
  static boolean hasAny() {
    return RULES.hasAny();
  }
}
