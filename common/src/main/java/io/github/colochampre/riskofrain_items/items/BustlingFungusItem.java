package io.github.colochampre.riskofrain_items.items;

import io.github.colochampre.riskofrain_items.client.ClientPlayerAccess;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BustlingFungusItem extends Item {
  private static final int STILL_TICKS = 40;    // 2 seconds standing still before healing starts
  private static final int HEAL_INTERVAL = 20;  // then heal once per second
  private static final double FIRST_HEAL_PERCENT = 5.0;      // 5% of max health for the first fungus
  private static final double EXTRA_HEAL_PERCENT = 2.25;     // +2.25% for each additional fungus
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

  /* Per-player state, so each player in multiplayer keeps their own timer (Bungus multiplayer fix by Yuese) */
  private static final Map<UUID, StillState> STATES = new HashMap<>();

  public BustlingFungusItem(Properties props) {
    super(props);
  }

  @Override
  public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_1"));
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_2"));
    tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_3"));
    if (level != null && level.isClientSide()) {
      Player player = ClientPlayerAccess.localPlayer();
      int count = player == null ? 0 : ItemUtils.countItemStack(player, this);
      if (count > 0) {
        // Healing past max health is lost, so the total shown stops at 100%
        String total = PERCENT_FORMAT.format(Math.min(healPercent(count), 100.0));
        tooltip.add(Component.translatable("item.riskofrain_items.bustling_fungus.tooltip_total", total, count));
      }
    }
    super.appendHoverText(itemstack, level, tooltip, flag);
  }

  private static double healPercent(int count) {
    return FIRST_HEAL_PERCENT + EXTRA_HEAL_PERCENT * (count - 1);
  }

  @Override
  public void inventoryTick(ItemStack stack, Level level, @NotNull Entity entity, int itemSlot, boolean isSelected) {
    if (!level.isClientSide() && entity instanceof Player player) {
      healIfStill(player, level);
    }
    super.inventoryTick(stack, level, entity, itemSlot, isSelected);
  }

  private void healIfStill(Player player, Level level) {
    StillState state = STATES.computeIfAbsent(player.getUUID(), uuid -> new StillState());
    long time = level.getGameTime();
    // Every fungus stack in the inventory ticks; only the first one each tick counts
    if (state.lastTick == time) {
      return;
    }
    boolean tickedLastTick = state.lastTick == time - 1;
    state.lastTick = time;

    boolean samePosition = player.getX() == state.x && player.getY() == state.y && player.getZ() == state.z;
    // A skipped tick means the player had no fungus for a while, so the timer starts over
    state.stillTicks = tickedLastTick && samePosition ? state.stillTicks + 1 : 0;
    state.x = player.getX();
    state.y = player.getY();
    state.z = player.getZ();

    if (state.stillTicks < STILL_TICKS || player.getHealth() >= player.getMaxHealth()) {
      return;
    }
    if (state.stillTicks == STILL_TICKS) {
      level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.BUNGUS_PROC_1.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
      level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.BUNGUS_PROC_2.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }
    if ((state.stillTicks - STILL_TICKS) % HEAL_INTERVAL == 0) {
      int count = ItemUtils.countItemStack(player, this);
      player.heal((float) (player.getMaxHealth() * healPercent(count) / 100.0));
    }
  }

  private static class StillState {
    long lastTick = Long.MIN_VALUE;
    int stillTicks;
    double x;
    double y;
    double z;
  }
}
