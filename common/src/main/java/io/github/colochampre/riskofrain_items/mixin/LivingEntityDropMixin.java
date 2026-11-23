package io.github.colochampre.riskofrain_items.mixin;

import io.github.colochampre.riskofrain_items.carrier.CarriedItems;
import io.github.colochampre.riskofrain_items.carrier.RoRItemCarrier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// LivingEntity.die calls dropAllDeathLoot for every living entity, whatever killed it. Unlike dropCustomDeathLoot,
// it is not skipped by the doMobLoot rule or by subclass overrides of the custom loot
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {

  @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
  private void riskofrain_items$dropCarriedItems(DamageSource source, CallbackInfo ci) {
    if ((Object) this instanceof RoRItemCarrier) {
      CarriedItems.dropAll((Mob) (Object) this);
    }
  }
}
