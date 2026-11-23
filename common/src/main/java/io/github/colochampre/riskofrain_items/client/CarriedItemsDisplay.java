package io.github.colochampre.riskofrain_items.client;

import io.github.colochampre.riskofrain_items.RoRMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  private static final Map<EntityType<?>, ToDoubleFunction<Entity>> RENDER_SCALES = new ConcurrentHashMap<>();
  private static final List<Pending> PENDING = new ArrayList<>();
  private static volatile boolean pendingResolved = true;

  private record Pending(Supplier<? extends EntityType<?>> type, ToDoubleFunction<Entity> scale) {
  }

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
    if (type == null || scale == null) {
      throw new IllegalArgumentException("type and scale must not be null");
    }
    synchronized (PENDING) {
      PENDING.add(new Pending(type, scale));
      pendingResolved = false;
    }
  }

  private static void resolvePending() {
    synchronized (PENDING) {
      for (Pending pending : PENDING) {
        EntityType<?> type;
        try {
          type = pending.type().get();
        } catch (RuntimeException e) {
          RoRMod.LOGGER.error("Could not resolve the entity type of a render scale; it is ignored", e);
          continue;
        }
        if (type == null) {
          RoRMod.LOGGER.error("A render scale supplied a null entity type; it is ignored");
        } else if (RENDER_SCALES.putIfAbsent(type, pending.scale()) != null) {
          RoRMod.LOGGER.error("A render scale is already registered for {}; the duplicate is ignored", type);
        }
      }
      PENDING.clear();
      pendingResolved = true;
    }
  }

  /** The scale registered for the entity's type, 1 when there is none. */
  public static float renderScale(Entity entity) {
    if (!pendingResolved) {
      resolvePending();
    }
    ToDoubleFunction<Entity> scale = RENDER_SCALES.get(entity.getType());
    return scale == null ? 1.0F : (float) scale.applyAsDouble(entity);
  }
}
