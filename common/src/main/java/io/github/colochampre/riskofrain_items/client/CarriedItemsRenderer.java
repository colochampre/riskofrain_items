package io.github.colochampre.riskofrain_items.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.colochampre.riskofrain_items.registry.RoRConfigs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Draws the items a mob carries as billboarded icons above its head, in rows of {@link #ITEMS_PER_ROW}. Called from the
 * entity render dispatcher (see EntityRenderDispatcherMixin) after the entity is drawn, so any renderer gets the icons. Client only.
 */
@Environment(EnvType.CLIENT)
public final class CarriedItemsRenderer {

  public static final int ITEMS_PER_ROW = 4;

  private static final float ICON_SIZE = 0.4F;
  private static final float ICON_GAP = 0.12F;
  private static final float COUNT_GAP = 0.04F;
  private static final float ROW_GAP = 0.05F;
  // The count text is drawn at this fraction of the size it had at 0.03 per pixel
  private static final float COUNT_TEXT_RATIO = 2.0F / 3.0F;
  private static final float TEXT_SCALE = 0.03F * COUNT_TEXT_RATIO;
  private static final float FONT_HEIGHT = 8.0F;
  // Rows are as tall as their tallest element, so a smaller text tightens them when it is the taller one
  private static final float ROW_HEIGHT = Math.max(ICON_SIZE, FONT_HEIGHT * TEXT_SCALE) + ROW_GAP;
  // Vanilla name tags hang this far below their anchor (9 px at 0.025 per px)
  private static final float NAME_TAG_HEIGHT = 0.225F;
  private static final float GAP_ABOVE_MODEL = 0.2F;
  private static final float GAP_ABOVE_NAME = 0.05F;
  // How far past the bounding box the view ray may pass and still count as looking at the mob
  private static final double LOOK_MARGIN = 0.5D;

  record Carried(ItemStack stack, String label) {
  }


  private CarriedItemsRenderer() {
  }

  /** Whether the entity has icons to draw at all (a map lookup), so callers can skip the rest of the work. */
  public static boolean hasIcons(Entity entity) {
    return !CarriedItemsClient.get(entity).isEmpty();
  }

  /**
   * @param nameShown  whether the renderer is drawing the entity's name tag, so the rows go above it
   * @param renderScale scale the renderer applied to the model and name tag, 1 when it applies none
   */
  public static void render(Entity entity, boolean nameShown, float renderScale, PoseStack poseStack, MultiBufferSource buffer) {
    RoRConfigs config = RoRConfigs.get();
    Minecraft minecraft = Minecraft.getInstance();
    if (!config.SHOW_CARRIED_ITEMS || minecraft.options.hideGui || entity.isInvisible()) {
      return;
    }
    List<Carried> items = CarriedItemsClient.get(entity);
    if (items.isEmpty()) {
      return;
    }
    double maxDistance = config.DISPLAY_DISTANCE;
    if (minecraft.getEntityRenderDispatcher().distanceToSqr(entity) > maxDistance * maxDistance) {
      return;
    }
    if (config.DISPLAY_MODE != RoRConfigs.CarriedItemsDisplayMode.ALWAYS && !isLookedAt(entity, minecraft, maxDistance)) {
      return;
    }

    Font font = minecraft.font;
    float baseY = nameShown
            ? entity.getNameTagOffsetY() * renderScale + GAP_ABOVE_NAME
            : entity.getBbHeight() * renderScale + GAP_ABOVE_MODEL;
    int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

    poseStack.pushPose();
    poseStack.translate(0.0F, baseY, 0.0F);
    poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    // The camera orientation leaves +x pointing to the viewer's left, as vanilla name tags compensate with a negative scale
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    // Items shade as they do in the inventory; text keeps the original buffer, it has no normals to shade
    GuiLitBufferSource itemBuffer = new GuiLitBufferSource(buffer, minecraft.getEntityRenderDispatcher().cameraOrientation());

    for (int rowStart = 0; rowStart < items.size(); rowStart += ITEMS_PER_ROW) {
      List<Carried> row = items.subList(rowStart, Math.min(rowStart + ITEMS_PER_ROW, items.size()));
      float rowY = (rowStart / ITEMS_PER_ROW) * ROW_HEIGHT + ICON_SIZE / 2.0F;
      float x = -rowWidth(row, font) / 2.0F;
      for (Carried carried : row) {
        renderIcon(entity, carried.stack(), x + ICON_SIZE / 2.0F, rowY, poseStack, itemBuffer);
        x += ICON_SIZE;
        if (carried.label() != null) {
          renderLabel(font, carried.label(), x + COUNT_GAP, rowY, background, poseStack, buffer);
          x += COUNT_GAP + font.width(carried.label()) * TEXT_SCALE;
        }
        x += ICON_GAP;
      }
    }
    poseStack.popPose();
  }

  /**
   * Whether the camera's view ray, within the display distance, reaches the mob's box widened by {@link #LOOK_MARGIN}
   * with no block in between. Cheap math first, the block clip only for a mob that is already being aimed at.
   */
  private static boolean isLookedAt(Entity entity, Minecraft minecraft, double maxDistance) {
    Camera camera = minecraft.gameRenderer.getMainCamera();
    Entity cameraEntity = camera.getEntity();
    if (cameraEntity == null) {
      return false;
    }
    Vec3 from = camera.getPosition();
    Vector3f look = camera.getLookVector();
    Vec3 to = new Vec3(from.x + look.x() * maxDistance, from.y + look.y() * maxDistance, from.z + look.z() * maxDistance);
    AABB box = entity.getBoundingBox().inflate(LOOK_MARGIN);
    // A camera inside the widened box has no entry point, and nothing can stand between it and the mob
    Vec3 hit = box.contains(from) ? from : box.clip(from, to).orElse(null);
    if (hit == null) {
      return false;
    }
    // The hit point sits on the view ray, so a block before it means the mob is behind that block
    BlockHitResult block = entity.level().clip(new ClipContext(from, hit, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cameraEntity));
    return block.getType() == HitResult.Type.MISS;
  }

  private static float rowWidth(List<Carried> row, Font font) {
    float width = -ICON_GAP;
    for (Carried carried : row) {
      width += ICON_SIZE + ICON_GAP;
      if (carried.label() != null) {
        width += COUNT_GAP + font.width(carried.label()) * TEXT_SCALE;
      }
    }
    return width;
  }

  private static void renderIcon(Entity entity, ItemStack stack, float x, float y, PoseStack poseStack, GuiLitBufferSource buffer) {
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    // Same choice as the inventory: models that use block light are lit like cubes, the rest like flat sprites
    buffer.forModel(itemRenderer.getModel(stack, entity.level(), null, entity.getId()).usesBlockLight());
    poseStack.pushPose();
    poseStack.translate(x, y, 0.0F);
    poseStack.scale(ICON_SIZE, ICON_SIZE, ICON_SIZE);
    itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
    poseStack.popPose();
  }

  // Same two passes as a vanilla name tag: a faint see-through one with the background, then the solid text
  private static void renderLabel(Font font, String label, float x, float y, int background, PoseStack poseStack, MultiBufferSource buffer) {
    poseStack.pushPose();
    poseStack.translate(x, y + FONT_HEIGHT * TEXT_SCALE / 2.0F, 0.0F);
    poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    Matrix4f matrix = poseStack.last().pose();
    font.drawInBatch(label, 0.0F, 0.0F, 0x20FFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, background, LightTexture.FULL_BRIGHT);
    font.drawInBatch(label, 0.0F, 0.0F, -1, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    poseStack.popPose();
  }
}
