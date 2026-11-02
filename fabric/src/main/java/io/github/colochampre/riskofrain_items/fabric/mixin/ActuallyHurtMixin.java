package io.github.colochampre.riskofrain_items.fabric.mixin;

import io.github.colochampre.riskofrain_items.events.ModEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Fabric has no event that can change damage, so this does the job of Forge's LivingDamageEvent. Player overrides
// actuallyHurt, so both classes are needed
@Mixin({LivingEntity.class, Player.class})
public abstract class ActuallyHurtMixin {

  // Load 6 is the `amount != 0` check in both methods: armor and absorption are already applied, as in LivingDamageEvent
  @ModifyVariable(method = "actuallyHurt", at = @At(value = "LOAD", ordinal = 6), argsOnly = true)
  private float riskofrain_items$modifyDamage(float amount, DamageSource source) {
    return ModEvents.onLivingDamage((LivingEntity) (Object) this, source, amount);
  }
}
