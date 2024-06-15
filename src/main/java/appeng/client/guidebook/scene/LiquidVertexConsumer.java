package appeng.client.guidebook.scene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    public VertexConsumer addVertex(float x, float y, float z) {
        x += sectionPos.getX() * SectionPos.SECTION_SIZE;
        y += sectionPos.getY() * SectionPos.SECTION_SIZE;
        z += sectionPos.getZ() * SectionPos.SECTION_SIZE;

        return delegate.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int i, int i1, int i2, int i3) {
        return delegate.setColor(i, i1, i2, i3);
    }

    @Override
    public VertexConsumer setUv(float v, float v1) {
        return delegate.setUv(v, v1);
    }

    @Override
    public VertexConsumer setUv1(int i, int i1) {
        return delegate.setUv1(i, i1);
    }

    @Override
    public VertexConsumer setUv2(int i, int i1) {
        return delegate.setUv2(i, i1);
    }

    @Override
    public VertexConsumer setNormal(float v, float v1, float v2) {
        return delegate.setNormal(v, v1, v2);
    }

    @Override
    public void addVertex(float p_351049_, float p_350528_, float p_351018_, int p_350427_, float p_350508_,
            float p_350864_, int p_350846_, int p_350731_, float p_350784_, float p_351051_, float p_350759_) {
        delegate.addVertex(p_351049_, p_350528_, p_351018_, p_350427_, p_350508_, p_350864_, p_350846_, p_350731_,
                p_350784_, p_351051_, p_350759_);
    }

    @Override
    public VertexConsumer setColor(float p_350350_, float p_350356_, float p_350623_, float p_350312_) {
        return delegate.setColor(p_350350_, p_350356_, p_350623_, p_350312_);
    }

    @Override
    public VertexConsumer setColor(int p_350809_) {
        return delegate.setColor(p_350809_);
    }

    @Override
    public VertexConsumer setWhiteAlpha(int p_350979_) {
        return delegate.setWhiteAlpha(p_350979_);
    }

    @Override
    public VertexConsumer setLight(int p_350855_) {
        return delegate.setLight(p_350855_);
    }

    @Override
    public VertexConsumer setOverlay(int p_350697_) {
        return delegate.setOverlay(p_350697_);
    }

    @Override
    public void putBulkData(PoseStack.Pose pPose, BakedQuad pQuad, float pRed, float pGreen, float pBlue, float pAlpha,
            int pPackedLight, int pPackedOverlay) {
        delegate.putBulkData(pPose, pQuad, pRed, pGreen, pBlue, pAlpha, pPackedLight, pPackedOverlay);
    }

    @Override
    public void putBulkData(PoseStack.Pose pPose, BakedQuad pQuad, float[] pBrightness, float pRed, float pGreen,
            float pBlue, float pAlpha, int[] pLightmap, int pPackedOverlay, boolean p_331268_) {
        delegate.putBulkData(pPose, pQuad, pBrightness, pRed, pGreen, pBlue, pAlpha, pLightmap, pPackedOverlay,
                p_331268_);
    }

    @Override
    public VertexConsumer addVertex(Vector3f p_350685_) {
        return delegate.addVertex(p_350685_);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose p_352288_, Vector3f p_352298_) {
        return delegate.addVertex(p_352288_, p_352298_);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose p_350506_, float p_350934_, float p_350873_, float p_350981_) {
        return delegate.addVertex(p_350506_, p_350934_, p_350873_, p_350981_);
    }

    @Override
    public VertexConsumer addVertex(Matrix4f p_350929_, float p_350884_, float p_350885_, float p_350942_) {
        return delegate.addVertex(p_350929_, p_350884_, p_350885_, p_350942_);
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose p_350592_, float p_350534_, float p_350411_, float p_350441_) {
        return delegate.setNormal(p_350592_, p_350534_, p_350411_, p_350441_);
    }
}
