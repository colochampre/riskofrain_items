package io.github.colochampre.riskofrain_items.carrier;

import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public entry point for the Risk of Rain items carried by vanilla and modded mobs (everything that passes
 * {@link CarrierEligibility}). The read methods work on any mob and return empty results for mobs that carry nothing.
 * Mobs with their own inventory (InventoryCarrier) are not handled here.
 */
public final class CarriedItems {

  public static final TagKey<Item> PASSIVE_ITEMS = TagKey.create(Registries.ITEM,
          new ResourceLocation("riskofrain_items", "passive_items"));
  // Not "Inventory": that key belongs to the vanilla InventoryCarrier mobs
  public static final String TAG_CARRIED_ITEMS = "riskofrain_items:CarriedItems";
  // The ground scan runs once per this many ticks, staggered by entity id
  public static final int PICKUP_INTERVAL = 10;

  private CarriedItems() {
  }

  @Nullable
  private static CarriedInventory inventory(Mob mob) {
    return ((RoRItemCarrier) mob).riskofrain_items$getCarriedInventory();
  }

  /** Copies of the carried stacks, in slot order. The list is read-only and changing it changes nothing. */
  public static List<ItemStack> get(Mob mob) {
    CarriedInventory inventory = inventory(mob);
    if (inventory == null) {
      return Collections.emptyList();
    }
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        stacks.add(stack.copy());
      }
    }
    return Collections.unmodifiableList(stacks);
  }

  public static boolean hasItems(Mob mob) {
    CarriedInventory inventory = inventory(mob);
    return inventory != null && !inventory.isEmpty();
  }

  /** Total count of one item across all the carried stacks. */
  public static int count(Mob mob, Item item) {
    CarriedInventory inventory = inventory(mob);
    if (inventory == null) {
      return 0;
    }
    int total = 0;
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (stack.is(item)) {
        total += stack.getCount();
      }
    }
    return total;
  }

  /** One distinct item and its total count across the carried stacks. */
  public record Entry(Item item, int count) {
  }

  /** Total count per distinct item, in the order each item first appears. */
  static List<Entry> summarize(CarriedInventory inventory) {
    Map<Item, Integer> totals = new LinkedHashMap<>();
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
      }
    }
    List<Entry> summary = new ArrayList<>(totals.size());
    totals.forEach((item, count) -> summary.add(new Entry(item, count)));
    return summary;
  }

  // The one place that runs after every change of any carried inventory (pickup, load, drop): it syncs the summary
  // to the players tracking the mob when it differs from the last one sent. A mob that never carried anything sends
  // nothing, and one that goes from some items to none sends the empty summary once
  static void onInventoryChanged(Mob mob) {
    if (mob.level().isClientSide) {
      return;
    }
    CarriedInventory inventory = inventory(mob);
    if (inventory == null) {
      return;
    }
    List<Entry> summary = summarize(inventory);
    if (summary.equals(inventory.syncedSummary)) {
      return;
    }
    inventory.syncedSummary = summary;
    CarrierSync.sendToTrackers(mob, summary);
  }

  public static CarriedInventory createInventory(Mob mob) {
    CarriedInventory inventory = new CarriedInventory();
    inventory.addListener(changed -> onInventoryChanged(mob));
    return inventory;
  }

  // Called at the end of the mob's AI step on the server. The reach is Mob.getPickupReach, protected, so the mixin
  // passes it in: the scan uses the same area as the vanilla loot pickup in Mob.aiStep
  public static void tick(Mob mob, Vec3i reach) {
    if ((mob.tickCount + mob.getId()) % PICKUP_INTERVAL != 0 || !mob.isAlive() || mob.isDeadOrDying()) {
      return;
    }
    if (!mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || !CarrierEligibility.canCarry(mob)) {
      return;
    }
    pickUpNearby(mob, reach);
  }

  private static void pickUpNearby(Mob mob, Vec3i reach) {
    List<ItemEntity> items = mob.level().getEntitiesOfClass(ItemEntity.class,
            mob.getBoundingBox().inflate(reach.getX(), reach.getY(), reach.getZ()));
    for (ItemEntity itemEntity : items) {
      ItemStack stack = itemEntity.getItem();
      if (itemEntity.isRemoved() || stack.isEmpty() || itemEntity.hasPickUpDelay() || !stack.is(PASSIVE_ITEMS)) {
        continue;
      }
      CarriedInventory inventory = inventory(mob);
      // Mobs that never pick anything up never get a container
      boolean canTake = inventory == null ? RoRConfigs.get().CARRIED_SLOTS > 0 : inventory.canAddItem(stack);
      if (!canTake) {
        continue;
      }
      if (inventory == null) {
        inventory = ((RoRItemCarrier) mob).riskofrain_items$getOrCreateCarriedInventory();
      }
      // Same sequence as InventoryCarrier.pickUpItem
      mob.onItemPickup(itemEntity);
      int before = stack.getCount();
      ItemStack rest = inventory.addItem(stack);
      mob.take(itemEntity, before - rest.getCount());
      if (rest.isEmpty()) {
        itemEntity.discard();
      } else {
        stack.setCount(rest.getCount());
      }
    }
  }

  public static void save(Mob mob, CompoundTag tag) {
    CarriedInventory inventory = inventory(mob);
    if (inventory == null || inventory.isEmpty()) {
      return;
    }
    ListTag list = new ListTag();
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        list.add(stack.save(new CompoundTag()));
      }
    }
    tag.put(TAG_CARRIED_ITEMS, list);
  }

  public static void load(Mob mob, CompoundTag tag) {
    RoRItemCarrier carrier = (RoRItemCarrier) mob;
    CarriedInventory inventory = carrier.riskofrain_items$getCarriedInventory();
    if (!tag.contains(TAG_CARRIED_ITEMS, 9)) {
      // Loading can run again on a live mob (/data merge): the saved state is the truth
      if (inventory != null) {
        inventory.clearContent();
      }
      return;
    }
    if (inventory == null) {
      inventory = carrier.riskofrain_items$getOrCreateCarriedInventory();
    } else {
      inventory.clearContent();
    }
    ListTag list = tag.getList(TAG_CARRIED_ITEMS, 10);
    // Slots are filled as saved, ignoring the slots setting, so lowering it never deletes carried items
    int slot = 0;
    for (int i = 0; i < list.size() && slot < inventory.getContainerSize(); i++) {
      ItemStack stack = ItemStack.of(list.getCompound(i));
      if (!stack.isEmpty()) {
        inventory.setItem(slot++, stack);
      }
    }
  }

  public static void dropAll(Mob mob) {
    CarriedInventory inventory = inventory(mob);
    if (mob.level().isClientSide || inventory == null) {
      return;
    }
    for (ItemStack stack : inventory.removeAllItems()) {
      mob.spawnAtLocation(stack);
    }
  }
}
