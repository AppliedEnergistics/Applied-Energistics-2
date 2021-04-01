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
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.PaintSplotchesTileEntity;
import appeng.util.Platform;

public class PaintSplotchesBlock extends AEBaseTileBlock<PaintSplotchesTileEntity> {
    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MaterialColor.AIR).notSolid().setAir());
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> list) {
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final PaintSplotchesTileEntity tp = this.getTileEntity(world, pos);

        if (tp != null) {
            tp.neighborUpdate();
        }
    }

    @Override
    public void fillWithRain(World world, BlockPos pos) {
        if (Platform.isServer()) {
            world.removeBlock(pos, false);
        }
    }

// FIXME FABRIC currently no equivalent
// FIXME FABRIC   @Override
// FIXME FABRIC   public int getLightValue(final BlockState state, final BlockView w, final BlockPos pos) {
// FIXME FABRIC       final PaintSplotchesTileEntity tp = this.getBlockEntity(w, pos);

// FIXME FABRIC       if (tp != null) {
// FIXME FABRIC           return tp.getLightLevel();
// FIXME FABRIC       }

// FIXME FABRIC       return 0;
// FIXME FABRIC   }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return true;
    }

}
