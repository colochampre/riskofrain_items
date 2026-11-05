package io.github.colochampre.riskofrain_items.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Items stored inside another item, such as a shulker box or a backpack, one level deep only: a container inside
 * the container is not opened.
 */
public final class ContainerContents {
  private ContainerContents() {
  }

  // Forge also asks the stack's item handler, which mod backpacks expose. Fabric 1.20.1 has no lookup for items
  // inside items, so it reads the stack's NBT only
  @ExpectPlatform
  public static int countInside(ItemStack container, Item item) {
    throw new AssertionError();
  }

  // The item lists vanilla keeps in a stack: shulker boxes under BlockEntityTag, bundles at the top
  public static int countInTag(ItemStack container, Item item) {
    CompoundTag tag = container.getTag();
    if (tag == null) {
      return 0;
    }
    String id = BuiltInRegistries.ITEM.getKey(item).toString();
    int total = countInList(tag.getList("Items", Tag.TAG_COMPOUND), id);
    if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
      total += countInList(tag.getCompound("BlockEntityTag").getList("Items", Tag.TAG_COMPOUND), id);
    }
    return total;
  }

  private static int countInList(ListTag items, String id) {
    int total = 0;
    for (int i = 0; i < items.size(); ++i) {
      CompoundTag entry = items.getCompound(i);
      if (id.equals(entry.getString("id"))) {
        // Vanilla saves the count as a byte; getInt reads any number type, in case a mod saves an int
        total += entry.getInt("Count");
      }
    }
    return total;
  }
}
