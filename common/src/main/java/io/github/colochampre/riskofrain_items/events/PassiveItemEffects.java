package io.github.colochampre.riskofrain_items.events;

import dev.architectury.event.events.common.TickEvent;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Effects of items that work just by being in the inventory. They run once per player tick with the total count of
 * the inventory, so one stack of 64 and 8 stacks of 8 give the same result.
 */
public class PassiveItemEffects {
  private static final UUID GOAT_HOOF_SPEED_ID = UUID.fromString("6f0c3b8e-2d4a-4e7b-9f15-8a2c71d4e903");

  public static void init() {
    TickEvent.PLAYER_POST.register(PassiveItemEffects::onPlayerTick);
  }

  private static void onPlayerTick(Player player) {
    if (player.level().isClientSide()) {
      return;
    }
    goatHoof(player);
    soldiersSyringe(player);
    roseBuckler(player);
  }

  // While sprinting: +14% movement speed per hoof
  private static void goatHoof(Player player) {
    AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
    if (speed == null) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.GOAT_HOOF.get());
    double bonus = player.isSprinting() ? ItemFormulas.goatHoofSpeedBonus(total) : 0.0D;
    AttributeModifier current = speed.getModifier(GOAT_HOOF_SPEED_ID);
    if (current != null && current.getAmount() == bonus) {
      return;
    }
    if (current != null) {
      speed.removeModifier(GOAT_HOOF_SPEED_ID);
    }
    if (bonus > 0) {
      // Transient: never saved, so it cannot stay behind after the hooves are gone
      speed.addTransientModifier(new AttributeModifier(GOAT_HOOF_SPEED_ID, "Goat Hoof speed", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  // Haste, one level more for every 4 syringes
  private static void soldiersSyringe(Player player) {
    int total = ItemUtils.countItemStack(player, RoRItems.SOLDIERS_SYRINGE.get());
    if (total > 0) {
      player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2, ItemFormulas.soldiersSyringeAmplifier(total), true, false));
    }
  }

  // While sprinting: Resistance, one level per buckler up to Resistance IV
  private static void roseBuckler(Player player) {
    int total = ItemUtils.countItemStack(player, RoRItems.ROSE_BUCKLER.get());
    if (total > 0 && player.isSprinting()) {
      player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, ItemFormulas.roseBucklerAmplifier(total), true, false));
    }
  }
}
