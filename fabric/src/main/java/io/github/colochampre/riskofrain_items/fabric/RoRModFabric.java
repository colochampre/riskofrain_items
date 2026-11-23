package io.github.colochampre.riskofrain_items.fabric;

import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.carrier.CarrierSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public final class RoRModFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    // Run our common setup.
    RoRMod.init();
    EntityTrackingEvents.START_TRACKING.register(CarrierSync::onStartTracking);
  }
}
