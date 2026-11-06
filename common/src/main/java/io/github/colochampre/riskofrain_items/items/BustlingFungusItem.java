package io.github.colochampre.riskofrain_items.items;

import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// The healing is applied in PassiveItemEffects
public class BustlingFungusItem extends Item {
  public BustlingFungusItem(Properties props) {
    super(props);
  }

  @Override
  public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_1"));
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_2"));
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_3"));
    // Healing past max health is lost, so the total shown stops at 100%
    TotalTooltip.append(tooltip, level, this, (player, count) -> new Object[]{TotalTooltip.percent(ItemFormulas.bustlingFungusHealPercent(count))});
    super.appendHoverText(itemstack, level, tooltip, flag);
  }
}
