package appeng.integration.modules.jei;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class FluidBlockRenderer implements IIngredientRenderer<IJeiFluidIngredient> {
    @Override
    public void render(PoseStack stack, IJeiFluidIngredient ingredient) {
        int xOffset = 0;
        int yOffset = 0;

        var fluidState = ingredient.getFluid().defaultFluidState();

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

        renderType.setupRenderState();
        RenderSystem.disableDepthTest();

        var worldMatStack = RenderSystem.getModelViewStack();
        worldMatStack.pushPose();
        worldMatStack.mulPoseMatrix(stack.last().pose());
        worldMatStack.translate(xOffset, yOffset, 0);

        FogRenderer.setupNoFog();

        // The fluid block will render [-0.5,0.5] since it's intended for world-rendering
        // we need to scale it to the rectangle's size, and then move it to the center
        worldMatStack.translate(getWidth() / 2.f, getHeight() / 2.f, 0);
        worldMatStack.scale(getWidth(), getHeight(), 1);

        setupOrtographicProjection(worldMatStack);

        var tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(renderType.mode(), renderType.format());
        blockRenderer.renderLiquid(
                BlockPos.ZERO,
                new FakeWorld(fluidState),
                builder,
                fluidState.createLegacyBlock(),
                fluidState);
        if (builder.building()) {
            tesselator.end();
        }

        // Reset the render state and return to the previous modelview matrix
        renderType.clearRenderState();
        worldMatStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void setupOrtographicProjection(PoseStack worldMatStack) {
        // Set up ortographic rendering for the block
        float angle = 36;
        float rotation = 45;

        worldMatStack.scale(1, 1, -1);
        worldMatStack.mulPose(Vector3f.YP.rotationDegrees(-180));

        Quaternion flip = Vector3f.ZP.rotationDegrees(180);
        flip.mul(Vector3f.XP.rotationDegrees(angle));

        Quaternion rotate = Vector3f.YP.rotationDegrees(rotation);
        worldMatStack.mulPose(flip);
        worldMatStack.mulPose(rotate);

        // Move into the center of the block for the transforms
        worldMatStack.translate(-0.5f, -0.5f, -0.5f);

        RenderSystem.applyModelViewMatrix();
    }

    private class FakeWorld implements BlockAndTintGetter {
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
            return colorResolver.getColor(BuiltinRegistries.BIOME.getOrThrow(Biomes.THE_VOID), 0, 0);
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
        public int getMinBuildHeight() {
            return 0;
        }
    }

    @Override
    public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
        FluidVariant fluidVariant = FluidVariant.of(ingredient.getFluid());
        return FluidVariantRendering.getTooltip(fluidVariant, tooltipFlag);
    }
}