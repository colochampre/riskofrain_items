package io.github.colochampre.riskofrain_items.utils.forge;

import io.github.colochampre.riskofrain_items.utils.ContainerContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public final class ContainerContentsImpl {
  private ContainerContentsImpl() {
  }

  // Mod backpacks expose their slots as an item handler. A stack without one, like a shulker box, keeps its items
  // in NBT. Only one of the two is read, so nothing is counted twice
  public static int countInside(ItemStack container, Item item) {
    IItemHandler handler = container.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
    if (handler == null) {
      return ContainerContents.countInTag(container, item);
    }
    int total = 0;
    for (int slot = 0; slot < handler.getSlots(); ++slot) {
      ItemStack stack = handler.getStackInSlot(slot);
      if (stack.is(item)) {
        total += stack.getCount();
      }
    }
    return total;
  }
}
