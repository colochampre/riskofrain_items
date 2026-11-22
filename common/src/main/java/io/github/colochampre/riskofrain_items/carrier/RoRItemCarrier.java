package io.github.colochampre.riskofrain_items.carrier;

import org.jetbrains.annotations.Nullable;

/**
 * Duck interface that the Mob mixin adds to every {@link net.minecraft.world.entity.Mob}. Other code reaches the
 * carried inventory through {@link CarriedItems}, not through this interface.
 */
public interface RoRItemCarrier {

  /** The carried inventory, or null while the mob has never picked anything up. */
  @Nullable
  CarriedInventory riskofrain_items$getCarriedInventory();

  /** The carried inventory, created on the first call. */
  CarriedInventory riskofrain_items$getOrCreateCarriedInventory();
}
