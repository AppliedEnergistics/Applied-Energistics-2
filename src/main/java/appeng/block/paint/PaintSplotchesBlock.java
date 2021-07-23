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

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.PaintSplotchesTileEntity;

public class PaintSplotchesBlock extends AEBaseTileBlock<PaintSplotchesTileEntity> {
    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MaterialColor.NONE).noOcclusion());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void neighborChanged(net.minecraft.world.level.block.state.BlockState state, Level world, net.minecraft.core.BlockPos pos, Block blockIn, net.minecraft.core.BlockPos fromPos,
                                boolean isMoving) {
        final PaintSplotchesTileEntity tp = this.getTileEntity(world, pos);

        if (tp != null) {
            tp.neighborChanged();
        }
    }

    @Override
    public void handleRain(final Level w, final net.minecraft.core.BlockPos pos) {
        if (!w.isClientSide()) {
            w.removeBlock(pos, false);
        }
    }

    @Override
    public int getLightValue(final net.minecraft.world.level.block.state.BlockState state, final BlockGetter w, final net.minecraft.core.BlockPos pos) {
        final PaintSplotchesTileEntity tp = this.getTileEntity(w, pos);

        if (tp != null) {
            return tp.getLightLevel();
        }

        return 0;
    }

    @Override
    public boolean isAir(final BlockState state, final BlockGetter world, final net.minecraft.core.BlockPos pos) {
        return true;
    }

    @Override
    public boolean canBeReplaced(net.minecraft.world.level.block.state.BlockState state, BlockPlaceContext useContext) {
        return true;
    }

    @Override
    public boolean canBeReplaced(net.minecraft.world.level.block.state.BlockState state, Fluid fluid) {
        return true;
    }

}
