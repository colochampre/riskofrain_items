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
import io.github.colochampre.riskofrain_items.utils.ItemUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Set;

public class ModEvents {
  private static final Set<Item> GOLD_TYPE = Sets.newHashSet(Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.RAW_GOLD);

  public static void init() {
    PlayerEvent.PICKUP_ITEM_PRE.register(ModEvents::coinPickupSound);
    EntityEvent.LIVING_DEATH.register(ModEvents::onLivingDeath);
  }

  // Architectury has no event that can change damage, so each platform calls this at the point where armor and
  // absorption are already applied: LivingDamageEvent on Forge, a mixin on Fabric
  public static float onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
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
    if (source.getEntity() instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.CROWBAR.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.CROWBAR.get());
          boolean canProc = (double) entity.getHealth() >= (double) entity.getMaxHealth() * 0.8D;
          if (!canProc) {
            return amount;
          }
          double damageMultiplier = 1.2D + 0.3D * (double) total;
          double procVolume = damageMultiplier / 10 > 10 ? 10 : damageMultiplier / 10;
          float crowbarDamage = amount * (float) damageMultiplier;
          level.playSound(null, player.getX(), player.getY(), player.getZ(), CrowbarItem.getProcSound(), SoundSource.PLAYERS, (float) procVolume, 1.0F);
          return crowbarDamage;
        }
      }
    }
    return amount;
  }

  private static float goldenGunProc(DamageSource source, float amount) {
    if (source.getEntity() instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.GOLDEN_GUN.get())) {
          int total = 0;
          int gold = 0;
          Inventory inv = player.getInventory();
          for (int i = 0; i <= 35; ++i) {
            ItemStack stack = inv.getItem(i);
            if (inv.getItem(i).getItem().equals(RoRItems.GOLDEN_GUN.get())) {
              total += stack.getCount();
            }
            if (GOLD_TYPE.contains(inv.getItem(i).getItem())) {
              if (stack.getItem().equals(Items.GOLD_INGOT)) {
                gold += stack.getCount() * 9;
              } else if (stack.getItem().equals(Items.RAW_GOLD)) {
                gold += stack.getCount() * 6;
              } else {
                gold += stack.getCount();
              }
            }
          }
          if (gold == 0) {
            return amount;
          }
          double neededGoldForMaxAmplifier = total > 9 ? 9 : (2304 / Math.pow(2, total)) * 2;
          double damageAmplifier = gold > neededGoldForMaxAmplifier ? 40.0D : Mth.floor(gold * 40 / neededGoldForMaxAmplifier);
          float goldenGunDamage = amount * ((float) damageAmplifier / 100 + 1);
          level.playSound(null, player.getX(), player.getY(), player.getZ(), GoldenGunItem.getProcSound(), SoundSource.PLAYERS, 0.4F, 1.0F);
          return goldenGunDamage;
        }
      }
    }
    return amount;
  }

  private static void infusionProc(DamageSource source) {
    if (source.getEntity() instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (!player.isCreative()) {
          if (ItemUtils.hasItemStack(player, RoRItems.INFUSION.get())) {
            int total = ItemUtils.countItemStack(player, RoRItems.INFUSION.get());
            if (player.getMaxHealth() < 20 + (total * 10 * 2)) {
              player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Infusion hp increase", 1.0, AttributeModifier.Operation.ADDITION));
              level.playSound(null, player.getX(), player.getY(), player.getZ(), InfusionItem.getProcSound(), SoundSource.PLAYERS, 0.4F, 1.0F);
            }
          }
        }
      }
    }
  }

  private static float lensMakersGlassesProc(DamageSource source, float amount) {
    if (source.getEntity() instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.LENS_MAKERS_GLASSES.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.LENS_MAKERS_GLASSES.get());
          double chance = 1.5625D * (double) total - 1.0D;
          int random = RandomSource.create().nextInt(100);
          boolean proc = random <= chance;
          if (!proc) {
            return amount;
          }
          float critDamage = amount * 2.0F;
          level.playSound(null, player.getX(), player.getY(), player.getZ(), RoRSounds.LENS_CRIT_PROC.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
          return critDamage;
        }
      }
    }
    return amount;
  }

  private static float repulsionArmorPlateProc(LivingEntity entity, float amount) {
    if (entity instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.REPULSION_ARMOR_PLATE.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.REPULSION_ARMOR_PLATE.get());
          float reducedDamage = amount - total < 1 ? 1 : amount - total;
          level.playSound(null, player.getX(), player.getY(), player.getZ(), RepulsionArmorPlateItem.getProcSound(), SoundSource.PLAYERS, 0.4F, 1.0F + (RandomSource.create().nextFloat() - RandomSource.create().nextFloat()) * 0.2F);
          return reducedDamage;
        }
      }
    }
    return amount;
  }

  private static void roseBucklerProc(LivingEntity entity) {
    if (entity instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.ROSE_BUCKLER.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.ROSE_BUCKLER.get());
          if (player.isSprinting()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), RoseBucklerItem.getProcSound(), SoundSource.PLAYERS, Math.min(0.4F * total, 8.0F), 1.0F + (RandomSource.create().nextFloat() - RandomSource.create().nextFloat()) * 0.2F);
          }
        }
      }
    }
  }

  private static void smartShopperProc(LivingEntity entity, DamageSource source) {
    if (source.getEntity() instanceof Player player && entity instanceof Enemy) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.SMART_SHOPPER.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.SMART_SHOPPER.get());
          ItemStack goldItem = new ItemStack(Items.GOLD_NUGGET, entity.getExperienceReward());
          ItemEntity goldItemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), goldItem);
          double chance = 4 * (double) total;
          int random = RandomSource.create().nextInt(100);
          boolean proc = random <= chance;
          if (!proc) {
            return;
          }
          level.addFreshEntity(goldItemEntity);
          level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), RoRSounds.COIN_PROC.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
        }
      }
    }
  }

  private static void topazBroochProc(DamageSource source) {
    if (source.getEntity() instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.TOPAZ_BROOCH.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.TOPAZ_BROOCH.get());
          if (total * 4 <= player.getMaxHealth()) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, total - 1, true, false));
          } else {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, (int) player.getMaxHealth() / 4 - 1, true, false));
          }
        }
      }
    }
  }

  private static float tougherTimesProc(LivingEntity entity, float amount) {
    if (entity instanceof Player player) {
      Level level = player.level();
      if (!level.isClientSide()) {
        if (ItemUtils.hasItemStack(player, RoRItems.TOUGHER_TIMES.get())) {
          int total = ItemUtils.countItemStack(player, RoRItems.TOUGHER_TIMES.get());
          double chance = (1.0D - 1.0D / (1.0D + 0.15D * (double) total) - 0.01D) * 100.0D;
          int random = RandomSource.create().nextInt(100);
          boolean proc = random <= chance;
          if (!proc) {
            return amount;
          }
          level.playSound(null, player.getX(), player.getY(), player.getZ(), TougherTimesItem.getProcSound(), SoundSource.PLAYERS, 0.4F, 1.0F);
          return 0;
        }
      }
    }
    return amount;
  }
}
