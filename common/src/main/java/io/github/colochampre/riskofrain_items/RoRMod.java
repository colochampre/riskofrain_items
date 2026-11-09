package io.github.colochampre.riskofrain_items;

import io.github.colochampre.riskofrain_items.events.ModEvents;
import io.github.colochampre.riskofrain_items.events.PassiveItemEffects;
import io.github.colochampre.riskofrain_items.loot.ModLootTables;
import io.github.colochampre.riskofrain_items.registry.RoRBlocks;
import io.github.colochampre.riskofrain_items.registry.RoRCreativeTabs;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import io.github.colochampre.riskofrain_items.registry.RoRPaintings;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RoRMod {
  public static final String MOD_ID = "riskofrain_items";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static void init() {
    LOGGER.info("Initialized RoR Items");

    RoRSounds.init();
    // Blocks go first: loading RoRBlocks adds the block items to RoRItems.ITEMS
    RoRBlocks.init();
    RoRItems.init();
    RoRCreativeTabs.init();
    RoRPaintings.init();
    ModEvents.init();
    PassiveItemEffects.init();
    ModLootTables.init();
  }
}
