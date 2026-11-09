package io.github.colochampre.riskofrain_items.items;

import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

// The damage reduction and the sound are in ModEvents
public class RoseBucklerItem extends Item {
  public RoseBucklerItem(Properties props) {
    super(props);
  }

  @Override
  public void appendHoverText(ItemStack itemstack, Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.riskofrain_items.rose_buckler.tooltip_1"));
    tooltip.add(Component.translatable("item.riskofrain_items.rose_buckler.tooltip_2"));
    tooltip.add(Component.translatable("item.riskofrain_items.rose_buckler.tooltip_3"));
    TotalTooltip.append(tooltip, level, this, (player, count) -> new Object[]{reductionPercent(count)});
    super.appendHoverText(itemstack, level, tooltip, flag);
  }

  // Damage reduction in percent with at most one decimal, like 23.1 or 60
  private static String reductionPercent(int count) {
    String text = String.format(Locale.ROOT, "%.1f", (1.0D - ItemFormulas.roseBucklerDamageMultiplier(count)) * 100.0D);
    return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
  }

  public static SoundEvent getProcSound() {
    return RoRSounds.ROSE_BUCKLER_PROC.get();
  }
}
