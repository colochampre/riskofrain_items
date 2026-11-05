package io.github.colochampre.riskofrain_items.items;

import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LensMakersGlassesItem extends Item {

  public LensMakersGlassesItem(Properties props) {
    super(props);
  }

  @Override
  public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.riskofrain_items.lens_makers_glasses.tooltip_1"));
    tooltip.add(Component.translatable("item.riskofrain_items.lens_makers_glasses.tooltip_2"));
    tooltip.add(Component.translatable("item.riskofrain_items.lens_makers_glasses.tooltip_3"));
    TotalTooltip.append(tooltip, level, this, (player, count) -> new Object[]{TotalTooltip.percent(ItemFormulas.lensMakersGlassesCritPercent(count))});
    super.appendHoverText(itemstack, level, tooltip, flag);
  }

  public static SoundEvent getProcSound() {
    return RoRSounds.LENS_CRIT_PROC.get();
  }
}
