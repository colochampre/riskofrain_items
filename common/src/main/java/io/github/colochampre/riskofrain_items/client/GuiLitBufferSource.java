package io.github.colochampre.riskofrain_items.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Makes items drawn in the world with the entity shader get the same diffuse shading they get in the inventory.
 * <p>
 * The item render types shade each vertex as {@code minecraft_mix_light(Light0, Light1, normal)}. While entities render,
 * {@code Lighting.setupLevel} puts the two light directions in view space, so they swing with the camera, and the
 * normals are the pose normals, also in view space. In the GUI {@code setupForFlatItems} / {@code setupFor3DItems}
 * puts them in a fixed screen frame instead. A flat item facing the camera ends up at ambient plus a varying part of
 * the light, darker than in the inventory, and the result changes with where the camera looks.
 * <p>
 * Both setups rotate the same two world directions {@code d}: level lights are {@code V d} (V is the view rotation)
 * and GUI lights are {@code G d}. Rewriting each normal as {@code n' = V * G^T * Sy * n} makes the dot products
 * identical ({@code (V d) . (V G^T Sy n) = (G d) . (Sy n)}), so a face shades in the level exactly as the face with the
 * same screen orientation does in the GUI. {@code Sy} flips y because the GUI pose is scaled by (16, -16, 16), which
 * puts its normals in a y-down frame. The normal is rewritten as vertices are written, so the buffer stays batched
 * with the other entities and no global lighting state is touched. Client only.
 * <p>
 * Approximation: the Nether's second light points down, so there front faces are about 2% darker than in the GUI.
 */
@Environment(EnvType.CLIENT)
final class GuiLitBufferSource implements MultiBufferSource {

  private static final float GUI_YAW = -0.3926991F;
  private static final float GUI_PITCH = 2.3561945F;

  // G^T * Sy for the GUI light setups of flat items (setupForFlatItems) and block items (setupFor3DItems)
  private static final Matrix3f FLAT_TO_WORLD = guiToWorld(new Matrix3f().scaling(1.0F, -1.0F, 1.0F).rotateY(GUI_YAW).rotateX(GUI_PITCH));
  private static final Matrix3f BLOCK_TO_WORLD = guiToWorld(new Matrix3f().rotationYXZ(1.0821041F, 3.2375858F, 0.0F).rotateYXZ(GUI_YAW, GUI_PITCH, 0.0F));

  private final MultiBufferSource delegate;
  private final Matrix3f flatNormals;
  private final Matrix3f blockNormals;
  private Matrix3f normals;

  /**
   * @param cameraOrientation the camera's rotation, as {@code EntityRenderDispatcher.cameraOrientation()}. The view the
   *                          level lights were set up with ({@code GameRenderer.renderLevel}: pitch, then yaw + 180) is
   *                          {@code Ry(180) * conj(cameraOrientation)}, not the plain inverse: the camera quaternion is
   *                          {@code rotationYXZ(-yaw, pitch, 0)}. This relies on the caller billboarding with
   *                          {@code mulPose(cameraOrientation)} plus a 180 degree turn around Y, which leaves the model
   *                          axes aligned with the eye.
   */
  GuiLitBufferSource(MultiBufferSource delegate, Quaternionf cameraOrientation) {
    this.delegate = delegate;
    Matrix3f view = new Matrix3f().rotation(new Quaternionf(cameraOrientation).conjugate()).rotateLocalY((float) Math.PI);
    this.flatNormals = new Matrix3f(view).mul(FLAT_TO_WORLD);
    this.blockNormals = new Matrix3f(view).mul(BLOCK_TO_WORLD);
    this.normals = flatNormals;
  }

  /**
   * @param blockLight {@code BakedModel.usesBlockLight()}: the inventory lights these like cubes, anything else flat
   */
  void forModel(boolean blockLight) {
    this.normals = blockLight ? blockNormals : flatNormals;
  }

  @Override
  public VertexConsumer getBuffer(RenderType type) {
    return new LitConsumer(delegate.getBuffer(type), normals);
  }

  private static Matrix3f guiToWorld(Matrix3f gui) {
    return gui.transpose().mul(new Matrix3f().scaling(1.0F, -1.0F, 1.0F));
  }

  /** Forwards everything, rewriting the normal of each vertex. */
  private static final class LitConsumer implements VertexConsumer {
    private final VertexConsumer out;
    private final Matrix3f matrix;
    private final Vector3f scratch = new Vector3f();

    private LitConsumer(VertexConsumer out, Matrix3f matrix) {
      this.out = out;
      this.matrix = matrix;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
      out.vertex(x, y, z);
      return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
      out.color(r, g, b, a);
      return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
      out.uv(u, v);
      return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
      out.overlayCoords(u, v);
      return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
      out.uv2(u, v);
      return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
      matrix.transform(x, y, z, scratch);
      out.normal(scratch.x(), scratch.y(), scratch.z());
      return this;
    }

    // BufferBuilder implements this directly, so quads written in bulk never reach normal()
    @Override
    public void vertex(float x, float y, float z, float r, float g, float b, float a, float u, float v,
                       int overlay, int light, float nx, float ny, float nz) {
      matrix.transform(nx, ny, nz, scratch);
      out.vertex(x, y, z, r, g, b, a, u, v, overlay, light, scratch.x(), scratch.y(), scratch.z());
    }

    @Override
    public void endVertex() {
      out.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
      out.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
      out.unsetDefaultColor();
    }
  }
}
