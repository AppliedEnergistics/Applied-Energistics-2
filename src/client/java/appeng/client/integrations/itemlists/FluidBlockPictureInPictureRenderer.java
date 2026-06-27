package appeng.client.integrations.itemlists;

import java.util.Objects;

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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class FluidBlockPictureInPictureRenderer
        extends PictureInPictureRenderer<FluidBlockPictureInPictureRenderer.State> {
    private final FluidBlockAndTintGetter fluidBlockAndTintGetter;

    public FluidBlockPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
        fluidBlockAndTintGetter = new FluidBlockAndTintGetter(Biomes.PLAINS);
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
                fluidBlockAndTintGetter,
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
            return parent.addVertex(pose, x, y, z);
        }
    }

    private static class FluidBlockAndTintGetter implements BlockAndTintGetter {
        private final Holder<Biome> biome;

        public FluidBlockAndTintGetter(ResourceKey<Biome> biomeKey) {
            var level = Minecraft.getInstance().level;
            Objects.requireNonNull(level, "level");
            this.biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        }

        public CardinalLighting cardinalLighting() {
            return CardinalLighting.DEFAULT;
        }

        public LevelLightEngine getLightEngine() {
            return LevelLightEngine.EMPTY;
        }

        public int getBlockTint(BlockPos pos, ColorResolver color) {
            return color.getColor(this.biome.value(), pos.getX(), pos.getZ());
        }

        public @org.jspecify.annotations.Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        public BlockState getBlockState(BlockPos pos) {
            return Blocks.AIR.defaultBlockState();
        }

        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        public int getHeight() {
            return 0;
        }

        public int getMinY() {
            return 0;
        }
    }

}
