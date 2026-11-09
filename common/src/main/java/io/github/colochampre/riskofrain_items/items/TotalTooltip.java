package io.github.colochampre.riskofrain_items.items;

import io.github.colochampre.riskofrain_items.client.ClientPlayerAccess;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The "Total" line at the end of an item tooltip: what the items the local player carries add up to right now.
 * Its translation key is the item's key plus {@code .tooltip_total}, and the count is always its last argument.
 */
public final class TotalTooltip {
  private static final DecimalFormat NUMBER = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

  private TotalTooltip() {
  }

  @FunctionalInterface
  public interface Values {
    Object[] of(Player player, int count);
  }

  // Tooltips are built on the client only; the line is left out when the player carries none of the item
  public static void append(List<Component> tooltip, @Nullable Level level, Item item, Values values) {
    if (level == null || !level.isClientSide()) {
      return;
    }
    Player player = ClientPlayerAccess.localPlayer();
    int count = player == null ? 0 : ItemUtils.countItemStack(player, item);
    if (count == 0) {
      return;
    }
    Object[] shown = values.of(player, count);
    Object[] args = Arrays.copyOf(shown, shown.length + 1);
    args[shown.length] = count;
    tooltip.add(Component.translatable(item.getDescriptionId() + ".tooltip_total", args));
  }

  // A percent value as shown in a tooltip, stopping at 100 because nothing goes past certain
  public static String percent(double value) {
    return number(Math.min(value, 100.0D));
  }

  public static String number(double value) {
    return NUMBER.format(value);
  }
}
