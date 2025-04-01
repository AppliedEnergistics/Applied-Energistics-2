package appeng.integration.modules.itemlists;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;

import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidBlockRendering {
    private FluidBlockRendering() {
    }

    public static void render(GuiGraphics guiGraphics, Fluid fluid, int x, int y, int width, int height) {
        var fluidState = fluid.defaultFluidState();

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

        // TODO 1.21.5 RenderSystem.disableDepthTest();

        var worldMatStack = RenderSystem.getModelViewStack();
        worldMatStack.pushMatrix();
        worldMatStack.mul(guiGraphics.pose().last().pose());
        worldMatStack.translate(x, y, 0);

        RenderSystem.setShaderFog(FogParameters.NO_FOG);

        // The fluid block will render [-0.5,0.5] since it's intended for world-rendering
        // we need to scale it to the rectangle's size, and then move it to the center
        worldMatStack.translate(width / 2.f, height / 2.f, 0);
        worldMatStack.scale(width, height, 1);

        setupOrthographicProjection(worldMatStack);

        var builder = Tesselator.getInstance().begin(renderType.mode(), renderType.format());
        blockRenderer.renderLiquid(
                BlockPos.ZERO,
                new FakeWorld(fluidState),
                builder,
                fluidState.createLegacyBlock(),
                fluidState);
        var meshData = builder.build();
        if (meshData != null) {
            renderType.draw(meshData);
        }

        // Reset the render state and return to the previous modelview matrix
        renderType.clearRenderState();
        worldMatStack.popMatrix();
    }

    private static void setupOrthographicProjection(Matrix4fStack worldMatStack) {
        // Set up orthographic rendering for the block
        float angle = 36;
        float rotation = 45;

        worldMatStack.scale(1, 1, -1);
        worldMatStack.rotate(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -180));

        Quaternionf flip = new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 180);
        flip.mul(new Quaternionf().rotationX(Mth.DEG_TO_RAD * angle));

        Quaternionf rotate = new Quaternionf().rotationY(Mth.DEG_TO_RAD * rotation);
        worldMatStack.rotate(flip);
        worldMatStack.rotate(rotate);

        // Move into the center of the block for the transforms
        worldMatStack.translate(-0.5f, -0.5f, -0.5f);
    }

    private static class FakeWorld implements BlockAndTintGetter {
        private final FluidState fluidState;

        public FakeWorld(FluidState fluidState) {
            this.fluidState = fluidState;
        }

        @Override
        public float getShade(Direction direction, boolean bl) {
            return 1.0f;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getBrightness(LightLayer lightLayer, BlockPos blockPos) {
            return 15;
        }

        @Override
        public int getRawBrightness(BlockPos blockPos, int i) {
            return 15;
        }

        @Override
        public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var biome = Minecraft.getInstance().level.getBiome(blockPos);
                return colorResolver.getColor(biome.value(), 0, 0);
            } else {
                return -1;
            }
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos blockPos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos blockPos) {
            if (blockPos.equals(BlockPos.ZERO)) {
                return fluidState.createLegacyBlock();
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        }

        @Override
        public FluidState getFluidState(BlockPos blockPos) {
            if (blockPos.equals(BlockPos.ZERO)) {
                return fluidState;
            } else {
                return Fluids.EMPTY.defaultFluidState();
            }
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getMinY() {
            return 0;
        }
    }

}
