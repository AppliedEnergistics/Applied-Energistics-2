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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class FluidBlockPictureInPictureRenderer
        extends PictureInPictureRenderer<FluidBlockPictureInPictureRenderer.State> {

    // PictureInPictureRenderer no longer takes a MultiBufferSource.BufferSource (MultiBufferSource
    // is gone) and NeoForge's RegisterPictureInPictureRenderersEvent registers a Supplier, so the renderer
    // needs a no-arg constructor.
    public FluidBlockPictureInPictureRenderer() {
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    /*
     * renderToTexture gained the SubmitNodeCollector and no longer draws anything itself - it only submits nodes, which
     * PictureInPictureRenderer#prepare then prepares into a frame and executes in its own render pass afterwards.
     * Setting up the level lighting here is still correct (vanilla's own GuiBannerResultRenderer / GuiEntityRenderer do
     * exactly the same) because Lighting#setupFor installs a persistent uniform buffer that stays bound for the rest of
     * prepare().
     */
    @Override
    protected void renderToTexture(State renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        var minecraft = Minecraft.getInstance();
        var fluidModelSet = minecraft.getModelManager().getFluidStateModelSet();

        // GameRenderer#getLighting() -> GameRenderer#lighting()
        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.LEVEL);

        var fluidState = renderState.fluid.defaultFluidState();

        poseStack.pushPose();
        setupOrthographicProjection(poseStack);

        // There is no buffer source to pull a VertexConsumer from any more. FluidRenderer#tesselate
        // asks its Output for exactly one buffer - the one for the fluid model's own ChunkSectionLayer (see
        // FluidRenderer line ~95) - so the render type can be resolved up front from the model and the whole
        // tesselation moved inside a single submitCustomGeometry callback, which is where the VertexConsumer
        // now comes from.
        // Sheets.cutoutBlockSheet()/translucentBlockSheet() were renamed to
        // cutoutBlockItemSheet()/translucentBlockItemSheet().
        var layer = fluidModelSet.get(fluidState).layer();
        var renderType = layer.translucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();

        var fluidRenderer = new FluidRenderer(fluidModelSet);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            var wrapped = new LiquidVertexConsumer(buffer, pose);
            fluidRenderer.tesselate(
                    BlockAndTintGetter.EMPTY,
                    BlockPos.ZERO,
                    ignoredLayer -> wrapped,
                    fluidState.createLegacyBlock(), fluidState);
        });

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
        // PoseStack#mulPose(Quaternionf) was renamed to PoseStack#rotate(Quaternionfc).
        poseStack.rotate(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -180));

        Quaternionf flip = new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 180);
        flip.mul(new Quaternionf().rotationX(Mth.DEG_TO_RAD * angle));

        Quaternionf rotate = new Quaternionf().rotationY(Mth.DEG_TO_RAD * rotation);
        poseStack.rotate(flip);
        poseStack.rotate(rotate);

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
