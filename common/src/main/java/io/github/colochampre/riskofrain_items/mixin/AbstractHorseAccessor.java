package io.github.colochampre.riskofrain_items.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// The saddle, armor and (for chested horses) chest container, which is protected in vanilla
@Mixin(AbstractHorse.class)
public interface AbstractHorseAccessor {

  @Accessor("inventory")
  SimpleContainer riskofrain_items$getInventory();
}
