package io.github.colochampre.riskofrain_items.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_items.client.CarriedItemsRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Every entity goes through this method whatever its renderer is (vanilla, GeckoLib...), so the carried item icons
// are drawn here, right after the renderer, with the pose still translated to the entity. Client mixin
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

  @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
          target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
  private void riskofrain_items$renderCarriedItems(Entity entity, double x, double y, double z, float yaw, float partialTicks,
                                                   PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
    boolean nameShown = ((EntityRendererInvoker) ((EntityRenderDispatcher) (Object) this).getRenderer(entity))
            .riskofrain_items$shouldShowName(entity);
    CarriedItemsRenderer.render(entity, nameShown, 1.0F, poseStack, buffer);
  }
}
