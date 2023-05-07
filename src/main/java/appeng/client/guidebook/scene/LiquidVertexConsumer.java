package appeng.client.guidebook.scene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.SectionPos;

/**
 * The only purpose of this vertex consumer proxy is to transform vertex positions emitted by the
 * {@link net.minecraft.client.renderer.block.LiquidBlockRenderer} into absolute coordinates. The renderer assumes it is
 * being called in the context of tessellating a chunk section (16x16x16) and emits corresponding coordinates, while we
 * batch all visible chunks in the guidebook together.
 */
public class LiquidVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final SectionPos sectionPos;

    public LiquidVertexConsumer(VertexConsumer delegate, SectionPos sectionPos) {
        this.delegate = delegate;
        this.sectionPos = sectionPos;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        x += sectionPos.getX() * SectionPos.SECTION_SIZE;
        y += sectionPos.getY() * SectionPos.SECTION_SIZE;
        z += sectionPos.getZ() * SectionPos.SECTION_SIZE;

        return delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return delegate.uv(u, v);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return delegate.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return delegate.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU,
            float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        delegate.vertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        delegate.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }

    @Override
    public VertexConsumer color(float red, float green, float blue, float alpha) {
        return delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer color(int i) {
        return delegate.color(i);
    }

    @Override
    public VertexConsumer uv2(int lightmapUV) {
        return delegate.uv2(lightmapUV);
    }

    @Override
    public VertexConsumer overlayCoords(int overlayUV) {
        return delegate.overlayCoords(overlayUV);
    }

    @Override
    public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float red, float green, float blue,
            int combinedLight, int combinedOverlay) {
        delegate.putBulkData(poseEntry, quad, red, green, blue, combinedLight, combinedOverlay);
    }

    @Override
    public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green,
            float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
        delegate.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
    }

    @Override
    public VertexConsumer vertex(Matrix4f matrix4f, float f, float g, float h) {
        return delegate.vertex(matrix4f, f, g, h);
    }

    @Override
    public VertexConsumer normal(Matrix3f matrix3f, float f, float g, float h) {
        return delegate.normal(matrix3f, f, g, h);
    }
}
