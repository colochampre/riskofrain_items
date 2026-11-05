package io.github.colochampre.riskofrain_items.utils.fabric;

import io.github.colochampre.riskofrain_items.utils.ContainerContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ContainerContentsImpl {
  private ContainerContentsImpl() {
  }

  // Fabric API 1.20.1 only looks up item storages in blocks, so the stack's NBT is all there is to read
  public static int countInside(ItemStack container, Item item) {
    return ContainerContents.countInTag(container, item);
  }
}
