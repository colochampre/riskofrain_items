package io.github.colochampre.riskofrain_items.utils;

import net.minecraft.util.Mth;

/**
 * How strong each item is for a given count. The effects and the tooltip totals both read these, so a tooltip
 * always shows what the effect really does.
 */
public final class ItemFormulas {
  private ItemFormulas() {
  }

  // Heal per second, in percent of max health
  public static double bustlingFungusHealPercent(int count) {
    return 5.0D + 2.25D * (count - 1);
  }

  public static double crowbarDamageMultiplier(int count) {
    return 1.2D + 0.3D * (double) count;
  }

  // Movement speed while sprinting, as a MULTIPLY_TOTAL amount
  public static double goatHoofSpeedBonus(int count) {
    return 0.14D * count;
  }

  // The widest view Goat Hoof may add to the field of view, as much as Speed II; the speed itself has no cap
  public static final double GOAT_HOOF_FOV_BONUS_CAP = 0.4D;

  // Bonus damage in percent, from the gold carried counted in nuggets
  public static double goldenGunBonusPercent(int count, int gold) {
    if (count == 0 || gold == 0) {
      return 0.0D;
    }
    double neededGoldForMaxAmplifier = count > 9 ? 9 : (2304 / Math.pow(2, count)) * 2;
    return gold > neededGoldForMaxAmplifier ? 40.0D : Mth.floor(gold * 40 / neededGoldForMaxAmplifier);
  }

  // The max health Infusion can add, in health points
  public static int infusionMaxBonus(int count) {
    return count * 10 * 2;
  }

  public static double lensMakersGlassesCritPercent(int count) {
    return 6.25D * (double) count;
  }

  public static int repulsionArmorPlateReduction(int count) {
    return count;
  }

  // Resistance level minus one, up to Resistance IV
  public static int roseBucklerAmplifier(int count) {
    return Math.min(count - 1, 3);
  }

  public static double smartShopperChancePercent(int count) {
    return 4.0D * (double) count;
  }

  // Haste level minus one, one level more for every 4 syringes
  public static int soldiersSyringeAmplifier(int count) {
    return count / 4;
  }

  // Absorption level minus one: each level is 2 hearts, up to the player's max health
  public static int topazBroochAmplifier(int count, float maxHealth) {
    return count * 4 <= maxHealth ? count - 1 : (int) maxHealth / 4 - 1;
  }

  public static double tougherTimesBlockPercent(int count) {
    return (1.0D - 1.0D / (1.0D + 0.15D * (double) count) - 0.01D) * 100.0D;
  }
}
