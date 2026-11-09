package io.github.colochampre.riskofrain_items.utils;

import io.github.colochampre.riskofrain_items.registry.RoRItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemUtils {

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

  // Counts the whole inventory (main slots, armor slots and offhand) and the containers sitting in it, such as
  // shulker boxes or backpacks, but not containers inside those
  public static int countItemStack(Player player, Item item) {
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
