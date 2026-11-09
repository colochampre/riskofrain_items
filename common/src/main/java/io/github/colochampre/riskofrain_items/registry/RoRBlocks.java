package io.github.colochampre.riskofrain_items.registry;

import io.github.colochampre.riskofrain_items.RoRMod;
import io.github.colochampre.riskofrain_items.blocks.custom.SmallChestBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;

import java.util.function.Supplier;

public class RoRBlocks {
  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(RoRMod.MOD_ID, Registries.BLOCK);

  public static final RegistrySupplier<Block> SMALL_CHEST = registerBlock("small_chest",
          () -> new SmallChestBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                  .strength(6f).requiresCorrectToolForDrops().noOcclusion()));

  private static <T extends Block> RegistrySupplier<T> registerBlock(String name, Supplier<T> block) {
    RegistrySupplier<T> toReturn = BLOCKS.register(name, block);
    registerBlockItem(name, toReturn);
    return toReturn;
  }

  private static <T extends Block> RegistrySupplier<Item> registerBlockItem(String name, RegistrySupplier<T> block) {
    return RoRItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void init() {
    BLOCKS.register();
  }
}
