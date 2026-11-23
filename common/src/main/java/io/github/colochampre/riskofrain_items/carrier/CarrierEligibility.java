package io.github.colochampre.riskofrain_items.carrier;

import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.InventoryCarrier;

/**
 * Single place that decides which mobs can pick up Risk of Rain items.
 * <ul>
 *   <li>{@code #riskofrain_items:pickup_excluded} removes a mob type from every group.</li>
 *   <li>{@code #riskofrain_items:pickup_allowed} opts in non-hostile mobs from other mods (the vanilla groups
 *   ignore it).</li>
 *   <li>Mobs from other mods in a boss convention tag are excluded. Both conventions exist in 1.20.1
 *   ({@code c:bosses} from the Fabric API and {@code forge:bosses} from Forge); a tag that a loader does not provide
 *   is just empty, so checking both ids needs no platform code.</li>
 *   <li>Neutral creatures ({@link NeutralMob} that are not {@link Enemy}: wild wolves, polar bears, iron golems,
 *   other mods' neutral mobs) have their own switch, in every namespace. That switch governs them everywhere, so a
 *   modded neutral mob is eligible with it even if the other-mods switch is off; the other-mods switch only governs
 *   modded {@code Enemy} mobs and mobs in {@code pickup_allowed}. Boss convention tags still exclude non-vanilla
 *   mobs. A tamed wolf uses the tamed-wolves switch only.</li>
 * </ul>
 */
public final class CarrierEligibility {

  private static final String VANILLA_NAMESPACE = "minecraft";

  public static final TagKey<EntityType<?>> PICKUP_EXCLUDED = tag(RoRMod.MOD_ID, "pickup_excluded");
  public static final TagKey<EntityType<?>> PICKUP_ALLOWED = tag(RoRMod.MOD_ID, "pickup_allowed");
  private static final TagKey<EntityType<?>> FABRIC_BOSSES = tag("c", "bosses");
  private static final TagKey<EntityType<?>> FORGE_BOSSES = tag("forge", "bosses");

  private CarrierEligibility() {
  }

  private static TagKey<EntityType<?>> tag(String namespace, String path) {
    return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(namespace, path));
  }

  /** Whether any group is switched on; with all of them off no mob needs to be checked at all. */
  public static boolean anyGroupEnabled() {
    RoRConfigs config = RoRConfigs.get();
    return config.VANILLA_HOSTILE_MOBS_PICK_UP || config.TAMED_WOLVES_PICK_UP || config.MODDED_MOBS_PICK_UP
            || config.NEUTRAL_MOBS_PICK_UP;
  }

  public static boolean canCarry(Mob mob) {
    // Mobs with their own inventory (the riskofrain_mobs mobs, piglins, villagers...) keep their own logic
    if (mob instanceof InventoryCarrier) {
      return false;
    }
    EntityType<?> type = mob.getType();
    if (type.is(PICKUP_EXCLUDED)) {
      return false;
    }
    RoRConfigs config = RoRConfigs.get();
    // Exact type: a modded subclass of Wolf has another type and is not covered by the wolves setting.
    // A tamed wolf is never eligible through the neutral group
    if (type == EntityType.WOLF && ((TamableAnimal) mob).isTame()) {
      return config.TAMED_WOLVES_PICK_UP;
    }
    boolean vanilla = VANILLA_NAMESPACE.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace());
    if (vanilla) {
      if (mob instanceof Enemy) {
        return config.VANILLA_HOSTILE_MOBS_PICK_UP;
      }
      return mob instanceof NeutralMob && config.NEUTRAL_MOBS_PICK_UP;
    }
    if (type.is(FABRIC_BOSSES) || type.is(FORGE_BOSSES)) {
      return false;
    }
    if (mob instanceof Enemy || type.is(PICKUP_ALLOWED)) {
      return config.MODDED_MOBS_PICK_UP;
    }
    return mob instanceof NeutralMob && config.NEUTRAL_MOBS_PICK_UP;
  }
}
