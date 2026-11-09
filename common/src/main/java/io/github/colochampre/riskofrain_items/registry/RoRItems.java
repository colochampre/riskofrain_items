package io.github.colochampre.riskofrain_items.registry;

import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.items.*;
import net.minecraft.world.item.Item;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;


public class RoRItems {
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RoRMod.MOD_ID, Registries.ITEM);

  public static final RegistrySupplier<Item> SMALL_CHEST = ITEMS.register("small_chest_item", () -> new Item(new Item.Properties()));
  public static final RegistrySupplier<Item> BUSTLING_FUNGUS = ITEMS.register("bustling_fungus",
          () -> new BustlingFungusItem(new Item.Properties()));
  public static final RegistrySupplier<Item> CROWBAR = ITEMS.register("crowbar",
          () -> new CrowbarItem(new Item.Properties()));
  public static final RegistrySupplier<Item> GOAT_HOOF = ITEMS.register("goat_hoof",
          () -> new GoatHoofItem(new Item.Properties()));
  public static final RegistrySupplier<Item> GOLDEN_GUN = ITEMS.register("golden_gun",
          () -> new GoldenGunItem(new Item.Properties()));
  public static final RegistrySupplier<Item> INFUSION = ITEMS.register("infusion",
          () -> new InfusionItem(new Item.Properties()));
  public static final RegistrySupplier<Item> LENS_MAKERS_GLASSES = ITEMS.register("lens_makers_glasses",
          () -> new LensMakersGlassesItem(new Item.Properties()));
  public static final RegistrySupplier<Item> REPULSION_ARMOR_PLATE = ITEMS.register("repulsion_armor_plate",
          () -> new RepulsionArmorPlateItem(new Item.Properties()));
  public static final RegistrySupplier<Item> ROSE_BUCKLER = ITEMS.register("rose_buckler",
          () -> new RoseBucklerItem (new Item.Properties()));
  public static final RegistrySupplier<Item> SMART_SHOPPER = ITEMS.register("smart_shopper",
          () -> new SmartShopperItem(new Item.Properties()));
  public static final RegistrySupplier<Item> SOLDIERS_SYRINGE = ITEMS.register("soldiers_syringe",
          () -> new SoldiersSyringeItem(new Item.Properties()));
  public static final RegistrySupplier<Item> TOPAZ_BROOCH = ITEMS.register("topaz_brooch",
          () -> new TopazBroochItem(new Item.Properties()));
  public static final RegistrySupplier<Item> TOUGHER_TIMES = ITEMS.register("tougher_times",
          () -> new TougherTimesItem(new Item.Properties()));

  public static void init() {
    ITEMS.register();
  }
}
