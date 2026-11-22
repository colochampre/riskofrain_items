package io.github.colochampre.riskofrain_items.carrier;

import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * The Risk of Rain items a mob carries. The container always has {@link #MAX_SLOTS} slots (the largest value of the
 * slots setting), but only as many stacks as the setting allows are accepted: the limit is read when an item is
 * added, so lowering the setting never deletes what a mob already carries, it only stops new stacks.
 */
public class CarriedInventory extends SimpleContainer {

  public static final int MAX_SLOTS = 27;

  public CarriedInventory() {
    super(MAX_SLOTS);
  }

  private static int allowedStacks() {
    return RoRConfigs.get().CARRIED_SLOTS;
  }

  private int filledSlots() {
    int filled = 0;
    for (int i = 0; i < getContainerSize(); i++) {
      if (!getItem(i).isEmpty()) {
        filled++;
      }
    }
    return filled;
  }

  private boolean canMergeInto(ItemStack existing, ItemStack stack) {
    return !existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)
            && existing.getCount() < Math.min(existing.getMaxStackSize(), getMaxStackSize());
  }

  @Override
  public boolean canAddItem(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    for (int i = 0; i < getContainerSize(); i++) {
      if (canMergeInto(getItem(i), stack)) {
        return true;
      }
    }
    return filledSlots() < allowedStacks();
  }

  // Same contract as SimpleContainer.addItem (returns what did not fit), but an empty slot is used only while the
  // number of carried stacks is below the setting
  @Override
  public ItemStack addItem(ItemStack stack) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack rest = stack.copy();
    for (int i = 0; i < getContainerSize() && !rest.isEmpty(); i++) {
      ItemStack existing = getItem(i);
      if (canMergeInto(existing, rest)) {
        int moved = Math.min(rest.getCount(), Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getCount());
        existing.grow(moved);
        rest.shrink(moved);
        setChanged();
      }
    }
    int filled = filledSlots();
    for (int i = 0; i < getContainerSize() && !rest.isEmpty() && filled < allowedStacks(); i++) {
      if (getItem(i).isEmpty()) {
        int moved = Math.min(rest.getCount(), getMaxStackSize());
        setItem(i, rest.split(moved));
        filled++;
      }
    }
    return rest;
  }
}
