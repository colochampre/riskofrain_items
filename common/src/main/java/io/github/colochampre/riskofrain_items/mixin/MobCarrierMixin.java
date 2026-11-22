package io.github.colochampre.riskofrain_items.mixin;

import io.github.colochampre.riskofrain_items.carrier.CarriedInventory;
import io.github.colochampre.riskofrain_items.carrier.CarriedItems;
import io.github.colochampre.riskofrain_items.carrier.RoRItemCarrier;
import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Gives every Mob a carried Risk of Rain inventory (created on the first pickup) and wires pickup, persistence and
// despawn. The death drop lives in LivingEntityDropMixin because the drop method belongs to LivingEntity
@Mixin(Mob.class)
public abstract class MobCarrierMixin extends LivingEntity implements RoRItemCarrier {

  @Unique
  @Nullable
  private CarriedInventory riskofrain_items$carried;

  @Shadow
  protected abstract Vec3i getPickupReach();

  @Shadow
  protected abstract boolean shouldDespawnInPeaceful();

  protected MobCarrierMixin(EntityType<? extends LivingEntity> type, Level level) {
    super(type, level);
  }

  @Override
  @Nullable
  public CarriedInventory riskofrain_items$getCarriedInventory() {
    return this.riskofrain_items$carried;
  }

  @Override
  public CarriedInventory riskofrain_items$getOrCreateCarriedInventory() {
    if (this.riskofrain_items$carried == null) {
      this.riskofrain_items$carried = CarriedItems.createInventory((Mob) (Object) this);
    }
    return this.riskofrain_items$carried;
  }

  // serverAiStep is final in Mob, so no subclass can skip it; it runs on the server only
  @Inject(method = "serverAiStep", at = @At("TAIL"))
  private void riskofrain_items$pickUpCarriedItems(CallbackInfo ci) {
    CarriedItems.tick((Mob) (Object) this, this.getPickupReach());
  }

  @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
  private void riskofrain_items$saveCarriedItems(CompoundTag tag, CallbackInfo ci) {
    CarriedItems.save((Mob) (Object) this, tag);
  }

  @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
  private void riskofrain_items$loadCarriedItems(CompoundTag tag, CallbackInfo ci) {
    CarriedItems.load((Mob) (Object) this, tag);
  }

  // Cancelling checkDespawn covers every removeWhenFarAway override. Peaceful removes the hostile mobs that do not
  // stay there, and that would delete what they carry, so the items drop first
  @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
  private void riskofrain_items$keepCarriers(CallbackInfo ci) {
    Mob mob = (Mob) (Object) this;
    if (!CarriedItems.hasItems(mob)) {
      return;
    }
    if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
      CarriedItems.dropAll(mob);
    } else if (RoRConfigs.get().CARRIERS_NEVER_DESPAWN) {
      this.noActionTime = 0;
      ci.cancel();
    }
  }
}
