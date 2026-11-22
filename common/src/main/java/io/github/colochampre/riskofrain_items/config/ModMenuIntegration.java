package io.github.colochampre.riskofrain_items.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import me.shedaniel.autoconfig.AutoConfig;

// Loaded by Mod Menu on Fabric only
public class ModMenuIntegration implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> AutoConfig.getConfigScreen(RoRConfigs.class, parent).get();
  }
}
