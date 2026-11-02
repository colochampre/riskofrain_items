package io.github.colochampre.riskofrain_items.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_items.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class RoRCreativeTabs {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(RoRMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

  public static final RegistrySupplier<CreativeModeTab> RISKOFRAIN_TAB = CREATIVE_MODE_TABS.register("riskofrain_tab",
          () -> CreativeTabRegistry.create(builder -> builder
                  .icon(() -> new ItemStack(RoRItems.SMALL_CHEST.get()))
                  .title(Component.translatable("creativemodetab.riskofrain_tab"))
                  .displayItems((parameters, output) -> {
                    output.accept(RoRItems.BUSTLING_FUNGUS.get());
                    output.accept(RoRItems.CROWBAR.get());
                    output.accept(RoRItems.GOAT_HOOF.get());
                    output.accept(RoRItems.LENS_MAKERS_GLASSES.get());
                    output.accept(RoRItems.REPULSION_ARMOR_PLATE.get());
                    output.accept(RoRItems.SOLDIERS_SYRINGE.get());
                    output.accept(RoRItems.TOPAZ_BROOCH.get());
                    output.accept(RoRItems.TOUGHER_TIMES.get());
                    output.accept(RoRItems.GOLDEN_GUN.get());
                    output.accept(RoRItems.INFUSION.get());
                    output.accept(RoRItems.ROSE_BUCKLER.get());
                    output.accept(RoRItems.SMART_SHOPPER.get());
                    // output.accept(RoRBlocks.SMALL_CHEST.get());
                  })));

  public static void init() {
    CREATIVE_MODE_TABS.register();
  }
}
