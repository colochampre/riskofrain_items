package io.github.colochampre.riskofrain_items.events;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Effects of items that work just by being in the inventory. They run once per player tick with the total count of
 * the inventory, so one stack of 64 and 8 stacks of 8 give the same result.
 */
public class PassiveItemEffects {
  public static final UUID GOAT_HOOF_SPEED_ID = UUID.fromString("6f0c3b8e-2d4a-4e7b-9f15-8a2c71d4e903");
  public static final UUID SOLDIERS_SYRINGE_ATTACK_SPEED_ID = UUID.fromString("1875e0cf-29b8-4945-afeb-64eb8b960126");
  private static final int FUNGUS_STILL_TICKS = 40;    // 2 seconds standing still before healing starts
  private static final int FUNGUS_HEAL_INTERVAL = 20;  // then heal once per second

  /* Per-player state, so each player in multiplayer keeps their own timer (Bungus multiplayer fix by Yuese) */
  private static final Map<UUID, StillState> FUNGUS_STATES = new HashMap<>();

  public static void init() {
    TickEvent.PLAYER_POST.register(PassiveItemEffects::onPlayerTick);
    PlayerEvent.PLAYER_QUIT.register(player -> FUNGUS_STATES.remove(player.getUUID()));
  }

  private static void onPlayerTick(Player player) {
    if (player.level().isClientSide()) {
      return;
    }
    bustlingFungus(player);
    goatHoof(player);
    soldiersSyringe(player);
  }

  // After standing still for 2 seconds, heal once per second
  private static void bustlingFungus(Player player) {
    int total = ItemUtils.countItemStack(player, RoRItems.BUSTLING_FUNGUS.get());
    if (total == 0) {
      // Without fungus the timer starts over
      FUNGUS_STATES.remove(player.getUUID());
      return;
    }
    StillState state = FUNGUS_STATES.computeIfAbsent(player.getUUID(), uuid -> new StillState());
    boolean samePosition = player.getX() == state.x && player.getY() == state.y && player.getZ() == state.z;
    state.stillTicks = samePosition ? state.stillTicks + 1 : 0;
    state.x = player.getX();
    state.y = player.getY();
    state.z = player.getZ();

    if (state.stillTicks < FUNGUS_STILL_TICKS || player.getHealth() >= player.getMaxHealth()) {
      return;
    }
    Level level = player.level();
    if (state.stillTicks == FUNGUS_STILL_TICKS) {
      level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.BUNGUS_PROC_1.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
      level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.BUNGUS_PROC_2.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }
    if ((state.stillTicks - FUNGUS_STILL_TICKS) % FUNGUS_HEAL_INTERVAL == 0) {
      player.heal((float) (player.getMaxHealth() * ItemFormulas.bustlingFungusHealPercent(total) / 100.0D));
    }
  }

  // While sprinting: +14% movement speed per hoof
  private static void goatHoof(Player player) {
    AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
    if (speed == null) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.GOAT_HOOF.get());
    double bonus = player.isSprinting() ? ItemFormulas.goatHoofSpeedBonus(total) : 0.0D;
    AttributeModifier current = speed.getModifier(GOAT_HOOF_SPEED_ID);
    if (current != null && current.getAmount() == bonus) {
      return;
    }
    if (current != null) {
      speed.removeModifier(GOAT_HOOF_SPEED_ID);
    }
    if (bonus > 0) {
      // Transient: never saved, so it cannot stay behind after the hooves are gone
      speed.addTransientModifier(new AttributeModifier(GOAT_HOOF_SPEED_ID, "Goat Hoof speed", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  // +15% attack speed per syringe
  private static void soldiersSyringe(Player player) {
    AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
    if (attackSpeed == null) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.SOLDIERS_SYRINGE.get());
    double bonus = ItemFormulas.soldiersSyringeAttackSpeedBonus(total);
    AttributeModifier current = attackSpeed.getModifier(SOLDIERS_SYRINGE_ATTACK_SPEED_ID);
    if (current != null && current.getAmount() == bonus) {
      return;
    }
    if (current != null) {
      attackSpeed.removeModifier(SOLDIERS_SYRINGE_ATTACK_SPEED_ID);
    }
    if (bonus > 0) {
      // Transient: never saved, so it cannot stay behind after the syringes are gone
      attackSpeed.addTransientModifier(new AttributeModifier(SOLDIERS_SYRINGE_ATTACK_SPEED_ID, "Soldier's Syringe attack speed", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  private static class StillState {
    int stillTicks;
    // NaN never equals a position, so the first tick always starts the timer at 0
    double x = Double.NaN;
    double y = Double.NaN;
    double z = Double.NaN;
  }
}
