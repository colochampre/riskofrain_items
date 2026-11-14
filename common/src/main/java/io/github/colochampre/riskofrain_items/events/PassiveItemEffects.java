package io.github.colochampre.riskofrain_items.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
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
 * Effects of items that work just by being in the inventory. They run once per tick with the total count of the
 * inventory, so one stack of 64 and 8 stacks of 8 give the same result. Players tick through Architectury; item
 * holders (see {@link ItemUtils#isItemHolder}) tick through {@link #onHolderTick}, called by each platform.
 */
public class PassiveItemEffects {
  public static final UUID GOAT_HOOF_SPEED_ID = UUID.fromString("6f0c3b8e-2d4a-4e7b-9f15-8a2c71d4e903");
  public static final UUID SOLDIERS_SYRINGE_ATTACK_SPEED_ID = UUID.fromString("1875e0cf-29b8-4945-afeb-64eb8b960126");
  private static final int FUNGUS_STILL_TICKS = 40;    // 2 seconds standing still before healing starts
  private static final int FUNGUS_HEAL_INTERVAL = 20;  // then heal once per second
  private static final int PRUNE_INTERVAL = 200;       // how often states of entities that stopped ticking are dropped
  private static final int STALE_TICKS = 200;          // an entity unseen for this long is gone (unloaded or removed)

  /* Per-entity state, so each player in multiplayer keeps their own timer (Bungus multiplayer fix by Yuese) */
  private static final Map<UUID, MotionState> STATES = new HashMap<>();
  private static long serverTicks;

  public static void init() {
    TickEvent.PLAYER_POST.register(PassiveItemEffects::onPlayerTick);
    TickEvent.SERVER_POST.register(PassiveItemEffects::pruneStates);
    PlayerEvent.PLAYER_QUIT.register(player -> STATES.remove(player.getUUID()));
    EntityEvent.LIVING_DEATH.register((entity, source) -> {
      STATES.remove(entity.getUUID());
      return EventResult.pass();
    });
  }

  private static void onPlayerTick(Player player) {
    if (player.level().isClientSide()) {
      return;
    }
    bustlingFungus(player);
    goatHoof(player, ItemUtils.countItemStack(player, RoRItems.GOAT_HOOF.get()), player.isSprinting());
    soldiersSyringe(player);
  }

  // Architectury 9 has no event for every living entity, so Forge (LivingTickEvent) and Fabric (a mixin on
  // LivingEntity.tick) call this for each one. The instanceof inside isItemHolder keeps it cheap for the rest
  public static void onHolderTick(LivingEntity entity) {
    if (entity.level().isClientSide() || !ItemUtils.isItemHolder(entity)) {
      return;
    }
    int fungus = ItemUtils.countItemStack(entity, RoRItems.BUSTLING_FUNGUS.get());
    int hoof = ItemUtils.countItemStack(entity, RoRItems.GOAT_HOOF.get());
    int buckler = ItemUtils.countItemStack(entity, RoRItems.ROSE_BUCKLER.get());
    if (fungus + hoof + buckler == 0) {
      STATES.remove(entity.getUUID());
      goatHoof(entity, 0, false);
      return;
    }
    MotionState state = track(entity);
    bustlingFungus(entity, state, fungus);
    goatHoof(entity, hoof, state.moving);
  }

  // Holders have no sprint, so Goat Hoof and Rose Buckler read this instead: the horizontal position changed since
  // the previous tick. False until the first tick has been seen
  public static boolean isMoving(LivingEntity holder) {
    MotionState state = STATES.get(holder.getUUID());
    return state != null && state.moving;
  }

  // Entities that unload or get removed without dying never tick again, so their state would stay forever
  private static void pruneStates(MinecraftServer server) {
    if (++serverTicks % PRUNE_INTERVAL == 0) {
      STATES.values().removeIf(state -> serverTicks - state.lastSeen > STALE_TICKS);
    }
  }

  // Records the position of this tick and tells whether it changed since the previous one
  private static MotionState track(LivingEntity entity) {
    MotionState state = STATES.computeIfAbsent(entity.getUUID(), uuid -> new MotionState());
    state.samePosition = entity.getX() == state.x && entity.getY() == state.y && entity.getZ() == state.z;
    state.moving = !Double.isNaN(state.x) && (entity.getX() != state.x || entity.getZ() != state.z);
    state.x = entity.getX();
    state.y = entity.getY();
    state.z = entity.getZ();
    state.lastSeen = serverTicks;
    return state;
  }

  private static void bustlingFungus(Player player) {
    int total = ItemUtils.countItemStack(player, RoRItems.BUSTLING_FUNGUS.get());
    if (total == 0) {
      // Without fungus the timer starts over
      STATES.remove(player.getUUID());
      return;
    }
    bustlingFungus(player, track(player), total);
  }

  // After standing still for 2 seconds, heal once per second
  private static void bustlingFungus(LivingEntity entity, MotionState state, int total) {
    state.stillTicks = state.samePosition ? state.stillTicks + 1 : 0;
    if (state.stillTicks < FUNGUS_STILL_TICKS || entity.getHealth() >= entity.getMaxHealth()) {
      return;
    }
    Level level = entity.level();
    if (state.stillTicks == FUNGUS_STILL_TICKS) {
      level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), RoRSounds.BUNGUS_PROC_1.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
      level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), RoRSounds.BUNGUS_PROC_2.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }
    if ((state.stillTicks - FUNGUS_STILL_TICKS) % FUNGUS_HEAL_INTERVAL == 0) {
      entity.heal((float) (entity.getMaxHealth() * ItemFormulas.bustlingFungusHealPercent(total) / 100.0D));
    }
  }

  // While sprinting (a player) or moving (a holder): +14% movement speed per hoof
  private static void goatHoof(LivingEntity entity, int total, boolean active) {
    AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
    if (speed == null) {
      return;
    }
    double bonus = active ? ItemFormulas.goatHoofSpeedBonus(total) : 0.0D;
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

  private static class MotionState {
    int stillTicks;
    boolean samePosition;
    boolean moving;
    long lastSeen;
    // NaN never equals a position, so the first tick always starts the timer at 0
    double x = Double.NaN;
    double y = Double.NaN;
    double z = Double.NaN;
  }
}
