package io.github.colochampre.riskofrain_items.client;

import io.github.colochampre.riskofrain_items.events.PassiveItemEffects;
import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Keeps the field of view from stretching with every Goat Hoof: the view widens as if the hooves added at most
 * {@link ItemFormulas#GOAT_HOOF_FOV_BONUS_CAP}, while the player still moves at the full speed. Other speed sources,
 * such as sprinting or a Speed potion, keep their usual effect on the view.
 */
public final class GoatHoofFov {
  private GoatHoofFov() {
  }

  public static float adjust(Player player, float fovModifier) {
    // The spyglass sets a fixed zoom that speed does not touch
    if (player.isScoping()) {
      return fovModifier;
    }
    float walkingSpeed = player.getAbilities().getWalkingSpeed();
    AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
    if (walkingSpeed == 0.0F || speed == null) {
      return fovModifier;
    }
    AttributeModifier hoof = speed.getModifier(PassiveItemEffects.GOAT_HOOF_SPEED_ID);
    if (hoof == null || hoof.getAmount() <= ItemFormulas.GOAT_HOOF_FOV_BONUS_CAP) {
      return fovModifier;
    }
    // The hoof multiplies the total speed, so swapping its factor for the capped one leaves every other source as is.
    // Vanilla scales the view by (speed / walkingSpeed + 1) / 2; the ratio keeps its flying and bow factors
    double value = speed.getValue();
    double capped = value * (1.0D + ItemFormulas.GOAT_HOOF_FOV_BONUS_CAP) / (1.0D + hoof.getAmount());
    return (float) (fovModifier * (capped / walkingSpeed + 1.0D) / (value / walkingSpeed + 1.0D));
  }
}
