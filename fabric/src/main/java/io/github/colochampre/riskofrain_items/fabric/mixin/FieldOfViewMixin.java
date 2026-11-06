package io.github.colochampre.riskofrain_items.fabric.mixin;

import io.github.colochampre.riskofrain_items.client.GoatHoofFov;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fabric has no event for the field of view, so this does the job of Forge's ComputeFovModifierEvent
@Mixin(AbstractClientPlayer.class)
public abstract class FieldOfViewMixin {

  @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
  private void riskofrain_items$capGoatHoofFov(CallbackInfoReturnable<Float> cir) {
    cir.setReturnValue(GoatHoofFov.adjust((AbstractClientPlayer) (Object) this, cir.getReturnValueF()));
  }
}
