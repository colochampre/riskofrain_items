package io.github.colochampre.riskofrain_items.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.colochampre.riskofrain_items.RoRMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RoRMod.MOD_ID)
public final class RoRModForge {
  public RoRModForge() {
    // Submit our event bus to let Architectury API register our content on the right time.
    EventBuses.registerModEventBus(RoRMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    // Run our common setup.
    RoRMod.init();

  }

}
