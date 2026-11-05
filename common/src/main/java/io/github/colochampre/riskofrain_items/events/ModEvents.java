package io.github.colochampre.riskofrain_items.events;

import com.google.common.collect.Sets;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import io.github.colochampre.riskofrain_items.items.CrowbarItem;
import io.github.colochampre.riskofrain_items.items.GoldenGunItem;
import io.github.colochampre.riskofrain_items.items.InfusionItem;
import io.github.colochampre.riskofrain_items.items.RepulsionArmorPlateItem;
import io.github.colochampre.riskofrain_items.items.RoseBucklerItem;
import io.github.colochampre.riskofrain_items.items.TougherTimesItem;
import io.github.colochampre.riskofrain_items.registry.RoRItems;
import io.github.colochampre.riskofrain_items.registry.RoRSounds;
import io.github.colochampre.riskofrain_items.utils.ItemFormulas;
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ModEvents {
  private static final Set<Item> GOLD_TYPE = Sets.newHashSet(Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.RAW_GOLD);
  private static final UUID INFUSION_MODIFIER_ID = UUID.fromString("0b6f5d2a-9c3e-4f81-a7d4-3e1c92b8f650");
  private static final String INFUSION_MODIFIER_NAME = "Infusion hp increase";

  public static void init() {
    PlayerEvent.PICKUP_ITEM_PRE.register(ModEvents::coinPickupSound);
    EntityEvent.LIVING_DEATH.register(ModEvents::onLivingDeath);
    PlayerEvent.PLAYER_CLONE.register(ModEvents::keepInfusionLeavingTheEnd);
  }

  // Architectury has no event that can change damage, so each platform calls this at the point where armor and
  // absorption are already applied: LivingDamageEvent on Forge, a mixin on Fabric
  public static float onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
    // Armor or absorption already stopped the whole hit, so there is nothing for an item to react to
    if (amount <= 0) {
      return amount;
    }
    amount = crowBarProc(entity, source, amount);
    amount = goldenGunProc(source, amount);
    amount = lensMakersGlassesProc(source, amount);
    amount = repulsionArmorPlateProc(entity, amount);
    roseBucklerProc(entity);
    return tougherTimesProc(entity, amount);
  }

  private static EventResult onLivingDeath(LivingEntity entity, DamageSource source) {
    infusionProc(source);
    smartShopperProc(entity, source);
    topazBroochProc(source);
    return EventResult.pass();
  }

  private static EventResult coinPickupSound(Player player, ItemEntity entity, ItemStack itemStack) {
    Level level = player.level();
    if (!level.isClientSide()) {
      if (GOLD_TYPE.contains(itemStack.getItem())) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.COIN_PROC.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
      }
    }
    return EventResult.pass();
  }

  private static float crowBarProc(LivingEntity entity, DamageSource source, float amount) {
    Player player = serverPlayer(source.getEntity());
    if (player == null) {
      return amount;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.CROWBAR.get());
    if (total == 0 || (double) entity.getHealth() < (double) entity.getMaxHealth() * 0.8D) {
      return amount;
    }
    double damageMultiplier = ItemFormulas.crowbarDamageMultiplier(total);
    double procVolume = Math.min(damageMultiplier / 10, 10);
    playSound(player, CrowbarItem.getProcSound(), (float) procVolume, 1.0F);
    return amount * (float) damageMultiplier;
  }

  private static float goldenGunProc(DamageSource source, float amount) {
    Player player = serverPlayer(source.getEntity());
    if (player == null) {
      return amount;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.GOLDEN_GUN.get());
    if (total == 0) {
      return amount;
    }
    int gold = ItemUtils.countGold(player);
    if (gold == 0) {
      return amount;
    }
    double damageAmplifier = ItemFormulas.goldenGunBonusPercent(total, gold);
    playSound(player, GoldenGunItem.getProcSound(), 0.4F, 1.0F);
    return amount * ((float) damageAmplifier / 100 + 1);
  }

  private static void infusionProc(DamageSource source) {
    Player player = serverPlayer(source.getEntity());
    if (player == null || player.isCreative()) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.INFUSION.get());
    AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
    if (total == 0 || maxHealth == null || player.getMaxHealth() >= 20 + ItemFormulas.infusionMaxBonus(total)) {
      return;
    }
    // Earlier versions added one more modifier on every kill. Fold them all into the single modifier kept now,
    // so the health a player already earned stays the same
    double bonus = 1.0D;
    for (AttributeModifier modifier : List.copyOf(maxHealth.getModifiers())) {
      if (isInfusion(modifier)) {
        bonus += modifier.getAmount();
        maxHealth.removeModifier(modifier.getId());
      }
    }
    maxHealth.addPermanentModifier(new AttributeModifier(INFUSION_MODIFIER_ID, INFUSION_MODIFIER_NAME, bonus, AttributeModifier.Operation.ADDITION));
    playSound(player, InfusionItem.getProcSound(), 0.4F, 1.0F);
  }

  // Vanilla gives a respawned player fresh attributes, so the health Infusion added is lost on death, as intended.
  // Leaving the End also creates a new player, and that one must keep it
  private static void keepInfusionLeavingTheEnd(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wonGame) {
    AttributeInstance oldMaxHealth = oldPlayer.getAttribute(Attributes.MAX_HEALTH);
    AttributeInstance newMaxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
    if (!wonGame || oldMaxHealth == null || newMaxHealth == null) {
      return;
    }
    for (AttributeModifier modifier : oldMaxHealth.getModifiers()) {
      if (isInfusion(modifier) && newMaxHealth.getModifier(modifier.getId()) == null) {
        newMaxHealth.addPermanentModifier(modifier);
      }
    }
    // The health copied before this event was capped at the max health the new player had then
    newPlayer.setHealth(oldPlayer.getHealth());
  }

  // Matches the current modifier and the ones earlier versions added with random ids
  private static boolean isInfusion(AttributeModifier modifier) {
    return modifier.getId().equals(INFUSION_MODIFIER_ID)
            || (modifier.getName().equals(INFUSION_MODIFIER_NAME) && modifier.getOperation() == AttributeModifier.Operation.ADDITION);
  }

  private static float lensMakersGlassesProc(DamageSource source, float amount) {
    Player player = serverPlayer(source.getEntity());
    if (player == null) {
      return amount;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.LENS_MAKERS_GLASSES.get());
    if (total == 0 || !roll(player, ItemFormulas.lensMakersGlassesCritPercent(total))) {
      return amount;
    }
    playSound(player, RoRSounds.LENS_CRIT_PROC.get(), 0.4F, 1.0F);
    return amount * 2.0F;
  }

  private static float repulsionArmorPlateProc(LivingEntity entity, float amount) {
    Player player = serverPlayer(entity);
    if (player == null) {
      return amount;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.REPULSION_ARMOR_PLATE.get());
    // A hit of 1 or less is already at the minimum, and reducing must never raise it
    if (total == 0 || amount <= 1.0F) {
      return amount;
    }
    playSound(player, RepulsionArmorPlateItem.getProcSound(), 0.4F, randomPitch(player));
    return Math.max(amount - ItemFormulas.repulsionArmorPlateReduction(total), 1.0F);
  }

  private static void roseBucklerProc(LivingEntity entity) {
    Player player = serverPlayer(entity);
    if (player == null || !player.isSprinting()) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.ROSE_BUCKLER.get());
    if (total > 0) {
      playSound(player, RoseBucklerItem.getProcSound(), Math.min(0.4F * total, 8.0F), randomPitch(player));
    }
  }

  private static void smartShopperProc(LivingEntity entity, DamageSource source) {
    Player player = serverPlayer(source.getEntity());
    if (player == null || !(entity instanceof Enemy)) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.SMART_SHOPPER.get());
    int nuggets = entity.getExperienceReward();
    if (total == 0 || nuggets <= 0 || !roll(player, ItemFormulas.smartShopperChancePercent(total))) {
      return;
    }
    Level level = player.level();
    level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.GOLD_NUGGET, nuggets)));
    playSound(entity, RoRSounds.COIN_PROC.get(), 0.4F, 1.0F);
  }

  private static void topazBroochProc(DamageSource source) {
    Player player = serverPlayer(source.getEntity());
    if (player == null) {
      return;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.TOPAZ_BROOCH.get());
    if (total == 0) {
      return;
    }
    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, ItemFormulas.topazBroochAmplifier(total, player.getMaxHealth()), true, false));
  }

  private static float tougherTimesProc(LivingEntity entity, float amount) {
    Player player = serverPlayer(entity);
    if (player == null) {
      return amount;
    }
    int total = ItemUtils.countItemStack(player, RoRItems.TOUGHER_TIMES.get());
    if (total == 0 || !roll(player, ItemFormulas.tougherTimesBlockPercent(total))) {
      return amount;
    }
    playSound(player, TougherTimesItem.getProcSound(), 0.4F, 1.0F);
    return 0;
  }

  // The player behind an entity, only on the server, where item effects are decided
  private static @Nullable Player serverPlayer(@Nullable Entity entity) {
    return entity instanceof Player player && !player.level().isClientSide() ? player : null;
  }

  // True with the given chance, in percent
  private static boolean roll(Player player, double percent) {
    return player.getRandom().nextDouble() * 100.0D < percent;
  }

  private static float randomPitch(Player player) {
    return 1.0F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F;
  }

  private static void playSound(Entity at, SoundEvent sound, float volume, float pitch) {
    at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
  }
}
