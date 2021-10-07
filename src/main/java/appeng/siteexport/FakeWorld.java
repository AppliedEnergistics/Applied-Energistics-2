package appeng.siteexport;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

class FakeWorld implements BlockAndTintGetter {
    Map<BlockPos, BlockState> blocks = new HashMap<>();

    Map<BlockPos, FluidState> fluids = new HashMap<>();

    Map<BlockPos, BlockEntity> entities = new HashMap<>();

    @Override
    public LevelLightEngine getLightEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int ambientDarkening) {
        return 15 - ambientDarkening;
    }

    @Override
    public float getShade(Direction side, boolean shaded) {
        if (!shaded) {
            return 1.0F;
        } else {
            return switch (side) {
                case DOWN -> 0.5F;
                case UP -> 1.0F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
            };
        }
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        return color.getColor(Biomes.PLAINS, pos.getX(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return entities.get(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return fluids.getOrDefault(pos, Fluids.EMPTY.defaultFluidState());
    }

    @Override
    public int getHeight() {
        return 255;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }
}
