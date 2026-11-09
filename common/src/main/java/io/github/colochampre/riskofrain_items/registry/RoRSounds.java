package io.github.colochampre.riskofrain_items.registry;

import io.github.colochampre.riskofrain_items.RoRMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;

public class RoRSounds {
  public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(RoRMod.MOD_ID, Registries.SOUND_EVENT);

  public static final RegistrySupplier<SoundEvent> COIN_PROC = registerSoundEvent("interactive.coin.proc");

  public static final RegistrySupplier<SoundEvent> BUNGUS_PROC_1 =
          registerSoundEvent("item.bustling_fungus.proc_1");
  public static final RegistrySupplier<SoundEvent> BUNGUS_PROC_2 =
          registerSoundEvent("item.bustling_fungus.proc_2");
  public static final RegistrySupplier<SoundEvent> CROWBAR_PROC =
          registerSoundEvent("item.crowbar.proc");
  public static final RegistrySupplier<SoundEvent> GOLD_GUN_PROC =
          registerSoundEvent("item.gold_gun.proc");
  public static final RegistrySupplier<SoundEvent> INFUSION_PROC =
          registerSoundEvent("item.infusion.proc");
  public static final RegistrySupplier<SoundEvent> LENS_CRIT_PROC =
          registerSoundEvent("item.lens_crit.proc");
  public static final RegistrySupplier<SoundEvent> REPULSION_ARMOR_PLATE_PROC =
          registerSoundEvent("item.repulsion_armor_plate.proc");
  public static final RegistrySupplier<SoundEvent> ROSE_BUCKLER_PROC =
          registerSoundEvent("item.rose_buckler.proc");
  public static final RegistrySupplier<SoundEvent> TOUGHER_TIMES_PROC =
          registerSoundEvent("item.tougher_times.proc");

  private static RegistrySupplier<SoundEvent> registerSoundEvent(final String soundName) {
    return SOUNDS.register(soundName, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RoRMod.MOD_ID, soundName)));
  }

  public static void init() {
    SOUNDS.register();
  }
}
