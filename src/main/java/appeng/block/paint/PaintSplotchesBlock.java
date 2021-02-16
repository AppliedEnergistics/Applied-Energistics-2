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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.PaintSplotchesBlockEntity;
import appeng.util.Platform;

public class PaintSplotchesBlock extends AEBaseTileBlock<PaintSplotchesBlockEntity> {
    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MapColor.CLEAR).nonOpaque().air());
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final PaintSplotchesBlockEntity tp = this.getBlockEntity(world, pos);

        if (tp != null) {
            tp.neighborUpdate();
        }
    }

    @Override
    public void precipitationTick(World world, BlockPos pos) {
        if (Platform.isServer()) {
            world.removeBlock(pos, false);
        }
    }

// FIXME FABRIC currently no equivalent
// FIXME FABRIC   @Override
// FIXME FABRIC   public int getLightValue(final BlockState state, final BlockView w, final BlockPos pos) {
// FIXME FABRIC       final PaintSplotchesBlockEntity tp = this.getBlockEntity(w, pos);

// FIXME FABRIC       if (tp != null) {
// FIXME FABRIC           return tp.getLightLevel();
// FIXME FABRIC       }

// FIXME FABRIC       return 0;
// FIXME FABRIC   }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return true;
    }

}
