package io.github.colochampre.riskofrain_items.carrier;

import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.InventoryCarrier;

/**
 * Single place that decides which mobs can pick up Risk of Rain items. The exclusion and opt-in tags and the boss
 * convention tags are added here in T3.
 */
public final class CarrierEligibility {

  private CarrierEligibility() {
  }

  public static boolean canCarry(Mob mob) {
    // Mobs with their own inventory (the riskofrain_mobs mobs, piglins, villagers...) keep their own logic
    if (mob instanceof InventoryCarrier) {
      return false;
    }
    RoRConfigs config = RoRConfigs.get();
    EntityType<?> type = mob.getType();
    if (mob instanceof Enemy) {
      boolean vanilla = "minecraft".equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace());
      return vanilla ? config.VANILLA_HOSTILE_MOBS_PICK_UP : config.MODDED_MOBS_PICK_UP;
    }
    // Exact type: a modded subclass of Wolf has another type and is not covered by the wolves setting
    return type == EntityType.WOLF && config.TAMED_WOLVES_PICK_UP && ((TamableAnimal) mob).isTame();
  }
}
