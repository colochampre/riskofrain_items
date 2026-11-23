package io.github.colochampre.riskofrain_items.registry;

import io.github.colochampre.riskofrain_items.RoRMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;

@Config(name = RoRMod.MOD_ID)
public class RoRConfigs implements ConfigData {

  @ConfigEntry.Gui.Excluded
  private static RoRConfigs INSTANCE;

  // Which mobs pick up Risk of Rain items from the ground
  public boolean VANILLA_HOSTILE_MOBS_PICK_UP = false;
  public boolean PETS_PICK_UP = false;
  public boolean MODDED_MOBS_PICK_UP = false;
  public boolean NEUTRAL_MOBS_PICK_UP = false;

  @ConfigEntry.BoundedDiscrete(min = 1, max = 27)
  public int CARRIED_SLOTS = 8;
  // With this on, a mob carrying at least one item never despawns, so its items are not lost
  public boolean CARRIERS_NEVER_DESPAWN = true;

  // Read by the client only: the icons above carrying mobs
  public boolean SHOW_CARRIED_ITEMS = true;
  @ConfigEntry.BoundedDiscrete(min = 1, max = 64)
  public int DISPLAY_DISTANCE = 16;
  @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
  public CarriedItemsDisplayMode DISPLAY_MODE = CarriedItemsDisplayMode.WHEN_LOOKING;

  public static void init() {
    AutoConfig.register(RoRConfigs.class, GsonConfigSerializer::new);
    ConfigHolder<RoRConfigs> holder = AutoConfig.getConfigHolder(RoRConfigs.class);
    // Only loading runs validatePostLoad; the GUI saves the live instance, so clamp it on save too.
    // Save listeners run before the file is written, so the clamped values are what gets saved
    holder.registerSaveListener((configHolder, config) -> {
      config.validatePostLoad();
      return InteractionResult.PASS;
    });
    INSTANCE = holder.getConfig();
  }

  public static RoRConfigs get() {
    return INSTANCE;
  }

  @Override
  public void validatePostLoad() {
    // GUI bounds are not enforced on hand-edited files
    CARRIED_SLOTS = Mth.clamp(CARRIED_SLOTS, 1, 27);
    DISPLAY_DISTANCE = Mth.clamp(DISPLAY_DISTANCE, 1, 64);
    // Files written before this entry existed, or with an unknown value, load it as null
    if (DISPLAY_MODE == null) {
      DISPLAY_MODE = CarriedItemsDisplayMode.WHEN_LOOKING;
    }
  }

  public enum CarriedItemsDisplayMode implements SelectionListEntry.Translatable {
    ALWAYS,
    WHEN_LOOKING;

    // AutoConfig's default name provider translates the key returned here, otherwise it would show the raw toString
    @Override
    public String getKey() {
      return "text.autoconfig." + RoRMod.MOD_ID + ".option.DISPLAY_MODE." + name();
    }
  }
}
