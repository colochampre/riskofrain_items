package io.github.colochampre.riskofrain_items.forge.config;

import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

// Client only: call it behind a dist check
public class ForgeConfigIntegration {
  public static void register() {
    ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
        new ConfigScreenHandler.ConfigScreenFactory((client, parent) ->
            AutoConfig.getConfigScreen(RoRConfigs.class, parent).get()));
  }
}
