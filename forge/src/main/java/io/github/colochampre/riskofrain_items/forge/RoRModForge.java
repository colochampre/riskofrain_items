package io.github.colochampre.riskofrain_items.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.carrier.CarrierSync;
import io.github.colochampre.riskofrain_items.client.GoatHoofFov;
import io.github.colochampre.riskofrain_items.forge.config.ForgeConfigIntegration;
import io.github.colochampre.riskofrain_items.events.ModEvents;
import io.github.colochampre.riskofrain_items.events.PassiveItemEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(RoRMod.MOD_ID)
public final class RoRModForge {
  public RoRModForge() {
    // Submit our event bus to let Architectury API register our content on the right time.
    EventBuses.registerModEventBus(RoRMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    // Run our common setup.
    RoRMod.init();

    MinecraftForge.EVENT_BUS.addListener(RoRModForge::onLivingDamage);
    MinecraftForge.EVENT_BUS.addListener(RoRModForge::onLivingTick);
    MinecraftForge.EVENT_BUS.addListener(RoRModForge::onStartTracking);
    if (FMLEnvironment.dist == Dist.CLIENT) {
      MinecraftForge.EVENT_BUS.addListener(RoRModForge::onComputeFov);
      ForgeConfigIntegration.register();
    }
  }

  private static void onStartTracking(PlayerEvent.StartTracking event) {
    if (event.getEntity() instanceof ServerPlayer player) {
      CarrierSync.onStartTracking(event.getTarget(), player);
    }
  }

  private static void onComputeFov(ComputeFovModifierEvent event) {
    event.setNewFovModifier(GoatHoofFov.adjust(event.getPlayer(), event.getNewFovModifier()));
  }

  // Players are ticked through Architectury, so only the item holders get through the check inside
  private static void onLivingTick(LivingEvent.LivingTickEvent event) {
    PassiveItemEffects.onHolderTick(event.getEntity());
  }

  private static void onLivingDamage(LivingDamageEvent event) {
    event.setAmount(ModEvents.onLivingDamage(event.getEntity(), event.getSource(), event.getAmount()));
  }
}
