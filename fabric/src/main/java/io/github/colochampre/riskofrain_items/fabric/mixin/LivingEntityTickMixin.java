package io.github.colochampre.riskofrain_items.fabric.mixin;

import io.github.colochampre.riskofrain_items.events.PassiveItemEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fabric has no tick event for every living entity, so this does the job of Forge's LivingTickEvent. Player and Mob
// call super.tick(), so this covers them; players are ticked through Architectury, and the check inside
// onHolderTick lets only item holders through
@Mixin(LivingEntity.class)
public abstract class LivingEntityTickMixin {

  @Inject(method = "tick", at = @At("TAIL"))
  private void riskofrain_items$holderTick(CallbackInfo ci) {
    PassiveItemEffects.onHolderTick((LivingEntity) (Object) this);
  }
}
