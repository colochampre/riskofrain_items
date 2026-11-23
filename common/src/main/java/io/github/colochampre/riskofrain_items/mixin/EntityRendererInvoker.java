package io.github.colochampre.riskofrain_items.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// shouldShowName is protected and overridden by LivingEntityRenderer; the invoker keeps the virtual call. Client mixin
@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker {

  @Invoker("shouldShowName")
  boolean riskofrain_items$shouldShowName(Entity entity);
}
