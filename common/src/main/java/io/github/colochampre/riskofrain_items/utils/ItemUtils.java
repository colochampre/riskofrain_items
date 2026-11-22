package io.github.colochampre.riskofrain_items.utils;

import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.carrier.CarriedItems;
import io.github.colochampre.riskofrain_items.carrier.RoRItemCarrier;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemUtils {

  // Non-player entities that may use the items they carry. Ships empty: other mods add their entities to it
  public static final TagKey<EntityType<?>> ITEM_USERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(RoRMod.MOD_ID, "item_users"));

  private static final List<Item> commonItems = new ArrayList<>();
  private static final List<Item> uncommonItems = new ArrayList<>();
  //private static final List<Item> legendaryItems = new ArrayList<>();

  static {
    commonItems.add(RoRItems.BUSTLING_FUNGUS.get());
    commonItems.add(RoRItems.CROWBAR.get());
    commonItems.add(RoRItems.GOAT_HOOF.get());
    commonItems.add(RoRItems.LENS_MAKERS_GLASSES.get());
    commonItems.add(RoRItems.REPULSION_ARMOR_PLATE.get());
    commonItems.add(RoRItems.SOLDIERS_SYRINGE.get());
    commonItems.add(RoRItems.TOPAZ_BROOCH.get());
    commonItems.add(RoRItems.TOUGHER_TIMES.get());

    uncommonItems.add(RoRItems.GOLDEN_GUN.get());
    uncommonItems.add(RoRItems.INFUSION.get());
    uncommonItems.add(RoRItems.ROSE_BUCKLER.get());
    uncommonItems.add(RoRItems.SMART_SHOPPER.get());
  }

  public static List<Item> getCommonItems() {
    return commonItems;
  }

  public static List<Item> getUncommonItems() {
    return uncommonItems;
  }

  /*
  public static List<Item> getLegendaryItems() {
      return legendaryItems;
  }
  */
  // Small chest chances: common 79%, uncommon 20%, legendary 1%
  public static Item getSmallChestItem() {
    Random random = new Random();
    int randomInt = random.nextInt(100);
        /*
        if (randomInt < 1) {
            randomInt = random.nextInt(legendaryItems.size());
            return legendaryItems.get(randomInt);
        } else*/
    if (randomInt < 20) {
      randomInt = random.nextInt(uncommonItems.size());
      return uncommonItems.get(randomInt);
    }
    randomInt = random.nextInt(commonItems.size());
    return commonItems.get(randomInt);
  }

  // Large chest chances: common 0%, uncommon 80%, legendary 20%
  public static Item getLargeChestItem() {
    Random random = new Random();
    int randomInt;
        /*
        randomInt = random.nextInt(100);
        if (randomInt < 20) {
            randomInt = random.nextInt(legendaryItems.size());
            return legendaryItems.get(randomInt);
        }
        */
    randomInt = random.nextInt(uncommonItems.size());
    return uncommonItems.get(randomInt);
  }

  public static boolean hasItemStack(Player player, Item item) {
    return countItemStack(player, item) > 0;
  }

  // Gold carried directly in the inventory, counted in nuggets, for Golden Gun
  public static int countGold(Player player) {
    int gold = 0;
    Inventory inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); ++i) {
      ItemStack stack = inv.getItem(i);
      if (stack.is(Items.GOLD_INGOT)) {
        gold += stack.getCount() * 9;
      } else if (stack.is(Items.RAW_GOLD)) {
        gold += stack.getCount() * 6;
      } else if (stack.is(Items.GOLD_NUGGET)) {
        gold += stack.getCount();
      }
    }
    return gold;
  }

  // A non-player entity that carries items in a vanilla inventory and is listed in the item_users tag, or a mob that
  // picked up items through the carried inventory (see CarriedItems), whatever its current eligibility is
  public static boolean isItemHolder(LivingEntity entity) {
    // The cheap instanceof goes first: this runs every tick for every living entity
    if (entity instanceof Player) {
      return false;
    }
    return entity instanceof Mob mob && CarriedItems.hasItems(mob)
            || entity instanceof InventoryCarrier && entity.getType().is(ITEM_USERS);
  }

  // A player counts the whole inventory (main slots, armor slots and offhand) and the containers sitting in it, such
  // as shulker boxes or backpacks, but not containers inside those. A holder counts only its own inventory slots
  public static int countItemStack(LivingEntity entity, Item item) {
    if (entity instanceof Player player) {
      return countPlayerItemStack(player, item);
    }
    int total = 0;
    if (entity instanceof Mob mob) {
      total += CarriedItems.count(mob, item);
    }
    if (entity instanceof InventoryCarrier carrier && entity.getType().is(ITEM_USERS)) {
      total += countHolderItemStack(carrier, item);
    }
    return total;
  }

  private static int countHolderItemStack(InventoryCarrier carrier, Item item) {
    int total = 0;
    Container inventory = carrier.getInventory();
    for (int i = 0; i < inventory.getContainerSize(); ++i) {
      ItemStack stack = inventory.getItem(i);
      if (stack.is(item)) {
        total += stack.getCount();
      }
    }
    return total;
  }

  private static int countPlayerItemStack(Player player, Item item) {
    int total = 0;
    Inventory inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); ++i) {
      ItemStack stack = inv.getItem(i);
      if (stack.is(item)) {
        total += stack.getCount();
      } else if (!stack.isEmpty()) {
        total += ContainerContents.countInside(stack, item);
      }
    }
    return total;
  }
}
