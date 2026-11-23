package io.github.colochampre.riskofrain_items.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

/**
 * Client side hook for mods whose renderer scales the model (for example with {@code poseStack.scale}) without changing
 * the entity's bounding box. The carried item icons are placed from the box height, so on such a mob they would float
 * too high or sink into the model; registering the scale here lets them follow it. Client only: call it from the
 * client initializer, never from common or server code.
 */
@Environment(EnvType.CLIENT)
public final class CarriedItemsDisplay {

  private static final Map<EntityType<?>, ToDoubleFunction<Entity>> RENDER_SCALES = new ConcurrentHashMap<>();

  private CarriedItemsDisplay() {
  }

  /**
   * Registers the scale the renderer of {@code type} applies to the model and its name tag, so the carried item icons
   * sit right above the scaled mob. The function runs every frame for each entity of the type that carries items, so
   * keep it cheap; return 1 for an unscaled entity. Call once per type, before or after riskofrain_items' own client init.
   *
   * @throws IllegalArgumentException if an argument is null
   * @throws IllegalStateException    if the type is already registered
   */
  public static void registerRenderScale(EntityType<?> type, ToDoubleFunction<Entity> scale) {
    if (type == null || scale == null) {
      throw new IllegalArgumentException("type and scale must not be null");
    }
    if (RENDER_SCALES.putIfAbsent(type, scale) != null) {
      throw new IllegalStateException("A render scale is already registered for " + type);
    }
  }

  /** The scale registered for the entity's type, 1 when there is none. */
  public static float renderScale(Entity entity) {
    ToDoubleFunction<Entity> scale = RENDER_SCALES.get(entity.getType());
    return scale == null ? 1.0F : (float) scale.applyAsDouble(entity);
  }
}
