/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.block.paint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.block.AEBaseEntityBlock;
import appeng.block.orientation.IOrientationStrategy;
import appeng.block.orientation.OrientationStrategies;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;

public class PaintSplotchesBlock extends AEBaseEntityBlock<PaintSplotchesBlockEntity> {

    /**
     * Lumen paint splotches contribute light-level 12, two or more have light-level 15. We model this with 0 = 0, 1 =
     * 12, 2 = 15.
     */
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 2);

    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MaterialColor.NONE).noOcclusion().air().lightLevel(state -> {
            var lightLevel = state.getValue(LIGHT_LEVEL);
            return switch (lightLevel) {
                default -> 0;
                case 1 -> 12;
                case 2 -> 15;
            };
        }));
        registerDefaultState(defaultBlockState().setValue(LIGHT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facingAttached(false);
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        // do nothing
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final PaintSplotchesBlockEntity tp = this.getBlockEntity(level, pos);

        if (tp != null) {
            tp.neighborChanged();
        }
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        if (!level.isClientSide() && precipitation == Biome.Precipitation.RAIN) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }

}
