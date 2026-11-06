package io.github.colochampre.riskofrain_items.fabric;

import io.github.colochampre.riskofrain_items.RoRMod;
import net.fabricmc.api.ModInitializer;

public final class RoRModFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    // Run our common setup.
    RoRMod.init();
  }
}
