package io.github.colochampre.riskofrain_items.registry;

import io.github.colochampre.riskofrain_items.RoRMod;
import net.minecraft.world.entity.decoration.PaintingVariant;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;

public class RoRPaintings {
  public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS = DeferredRegister.create(RoRMod.MOD_ID, Registries.PAINTING_VARIANT);

  public static final RegistrySupplier<PaintingVariant> FANART_99HUNTERS = PAINTING_VARIANTS.register("fanart_99hunters_1",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_AYMBORD = PAINTING_VARIANTS.register("fanart_aymbord_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_BETAWHO_1 = PAINTING_VARIANTS.register("fanart_betawho_1",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_BETAWHO_2 = PAINTING_VARIANTS.register("fanart_betawho_2",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_BETAWHO_3 = PAINTING_VARIANTS.register("fanart_betawho_3",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_BETAWHO_4 = PAINTING_VARIANTS.register("fanart_betawho_4",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_BETAWHO_5 = PAINTING_VARIANTS.register("fanart_betawho_5",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_BURN = PAINTING_VARIANTS.register("fanart_burn_1",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_CORRECT_APPLE = PAINTING_VARIANTS.register("fanart_correct_apple_1",
          () -> new PaintingVariant(16, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_DINAMITTIN = PAINTING_VARIANTS.register("fanart_dinamittin_1",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_DRANSVITRY = PAINTING_VARIANTS.register("fanart_dransvitry_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_IVANGOGH99 = PAINTING_VARIANTS.register("fanart_ivangogh99_1",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_LAMBOSAURUS = PAINTING_VARIANTS.register("fanart_lambosaurus_1",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_MAFFI1996 = PAINTING_VARIANTS.register("fanart_maffi1996_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_MATHIEUBAL = PAINTING_VARIANTS.register("fanart_mathieubal_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_PCMYTH = PAINTING_VARIANTS.register("fanart_pcmyth_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_SHAMAN_INFINITUS = PAINTING_VARIANTS.register("fanart_shaman_1",
          () -> new PaintingVariant(32, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_SUPERUBERKRUBER = PAINTING_VARIANTS.register("fanart_superuberkruber_1",
          () -> new PaintingVariant(32, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_THICK_JUDGE_JUDY = PAINTING_VARIANTS.register("fanart_thick_judge_judy_1",
          () -> new PaintingVariant(16, 16));
  public static final RegistrySupplier<PaintingVariant> FANART_TINK29 = PAINTING_VARIANTS.register("fanart_tink29_1",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_ZOOCHMAN_1 = PAINTING_VARIANTS.register("fanart_zoochman_1",
          () -> new PaintingVariant(16, 32));
  public static final RegistrySupplier<PaintingVariant> FANART_ZOOCHMAN_2 = PAINTING_VARIANTS.register("fanart_zoochman_2",
          () -> new PaintingVariant(16, 32));
  public static final RegistrySupplier<PaintingVariant> OFFICIAL_COMMANDO = PAINTING_VARIANTS.register("official_commando",
          () -> new PaintingVariant(16, 16));
  public static final RegistrySupplier<PaintingVariant> OFFICIAL_ENGINEER = PAINTING_VARIANTS.register("official_engineer",
          () -> new PaintingVariant(16, 16));
  public static final RegistrySupplier<PaintingVariant> OFFICIAL_OMEGA = PAINTING_VARIANTS.register("official_omega_1",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> OFFICIAL_ROR_RETURNS = PAINTING_VARIANTS.register("official_risk_of_rain_returns",
          () -> new PaintingVariant(64, 32));
  public static final RegistrySupplier<PaintingVariant> OFFICIAL_STONE_GOLEM = PAINTING_VARIANTS.register("official_stone_golem",
          () -> new PaintingVariant(16, 16));

  public static void init() {
    PAINTING_VARIANTS.register();
  }
}
