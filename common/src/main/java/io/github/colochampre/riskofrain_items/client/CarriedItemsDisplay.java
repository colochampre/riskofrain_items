package io.github.colochampre.riskofrain_items.client;

import io.github.colochampre.riskofrain_items.carrier.LazyTypeMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Client side hook for mods whose renderer scales the model (for example with {@code poseStack.scale}) without changing
 * the entity's bounding box. The carried item icons are placed from the box height, so on such a mob they would float
 * too high or sink into the model; registering the scale here lets them follow it. Client only: call it from the
 * client initializer, never from common or server code.
 */
@Environment(EnvType.CLIENT)
public final class CarriedItemsDisplay {

  private static final LazyTypeMap<ToDoubleFunction<Entity>> RENDER_SCALES = new LazyTypeMap<>("render scale");

  private CarriedItemsDisplay() {
  }

  /**
   * Registers the scale the renderer of the type applies to the model and its name tag, so the carried item icons
   * sit right above the scaled mob. The type is resolved lazily (the first time an icon is placed), so a registry
   * supplier can be passed straight from the client initializer, for example
   * {@code registerRenderScale(MyEntities.DRONE, e -> 0.9D)}; do not call {@code get()} yourself at init. The function
   * runs every frame for each entity of the type that carries items, so keep it cheap; return 1 for an unscaled
   * entity. Call once per type, before or after riskofrain_items' own client init; a duplicate is logged and ignored
   * when the suppliers are resolved.
   *
   * @throws IllegalArgumentException if an argument is null
   */
  public static void registerRenderScale(Supplier<? extends EntityType<?>> type, ToDoubleFunction<Entity> scale) {
    RENDER_SCALES.register(type, scale);
  }

  /** The scale registered for the entity's type, 1 when there is none. */
  public static float renderScale(Entity entity) {
    ToDoubleFunction<Entity> scale = RENDER_SCALES.get(entity.getType());
    return scale == null ? 1.0F : (float) scale.applyAsDouble(entity);
  }
}
