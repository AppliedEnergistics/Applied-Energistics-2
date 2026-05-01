package appeng.client.integrations.itemlists;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class FluidBlockPictureInPictureRenderer
        extends PictureInPictureRenderer<FluidBlockPictureInPictureRenderer.State> {

    public FluidBlockPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected void renderToTexture(State renderState, PoseStack poseStack) {
        var minecraft = Minecraft.getInstance();
        var fluidModelSet = minecraft.getModelManager().getFluidStateModelSet();

        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);

        var fluidState = renderState.fluid.defaultFluidState();

        poseStack.pushPose();
        setupOrthographicProjection(poseStack);

        var fluidRenderer = new FluidRenderer(fluidModelSet);
        fluidRenderer.tesselate(
                BlockAndTintGetter.EMPTY,
                BlockPos.ZERO,
                layer -> {
                    // TODO 26.1: Unclear if this is still needed
                    var buffer = bufferSource.getBuffer(
                            layer.translucent() ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
                    return new LiquidVertexConsumer(buffer, poseStack.last());
                },
                fluidState.createLegacyBlock(), fluidState);

        poseStack.popPose();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "AE2 Fluid in GUI";
    }

    public record State(
            Matrix3x2f pose,
            int x0, int y0,
            int x1, int y1,
            ScreenRectangle bounds,
            @Nullable ScreenRectangle scissorArea,
            Fluid fluid) implements PictureInPictureRenderState {
        @Override
        public float scale() {
            return 16;
        }
    }

    private static void setupOrthographicProjection(PoseStack poseStack) {
        // Set up orthographic rendering for the block
        float angle = 36;
        float rotation = 45;

        poseStack.scale(1, 1, -1);
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -180));

        Quaternionf flip = new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 180);
        flip.mul(new Quaternionf().rotationX(Mth.DEG_TO_RAD * angle));

        Quaternionf rotate = new Quaternionf().rotationY(Mth.DEG_TO_RAD * rotation);
        poseStack.mulPose(flip);
        poseStack.mulPose(rotate);

        // Move into the center of the block for the transforms
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    /**
     * The only purpose of this vertex consumer proxy is to transform vertex positions emitted by the
     * {@link FluidRenderer} into absolute coordinates. The renderer assumes it is being called in the context of
     * tessellating a chunk section (16x16x16) and emits corresponding coordinates, while we batch all visible chunks in
     * the guidebook together.
     */
    private static class LiquidVertexConsumer extends VertexConsumerWrapper {
        private final PoseStack.Pose pose;

        public LiquidVertexConsumer(VertexConsumer delegate, PoseStack.Pose pose) {
            super(delegate);
            this.pose = pose;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            // add missing UV1 for entity format which is used to replace TRANSLUCENT in non-chunk-section render
            return parent.addVertex(pose, x, y, z).setUv1(0, 0);
        }
    }
}
