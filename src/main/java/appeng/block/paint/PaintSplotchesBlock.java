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
import net.minecraft.fluid.Fluid;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.PaintSplotchesTileEntity;
import appeng.util.Platform;

public class PaintSplotchesBlock extends AEBaseTileBlock<PaintSplotchesTileEntity> {
    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MaterialColor.NONE).noOcclusion());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> itemStacks) {
        // do nothing
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
            tp.neighborChanged();
        }
    }

    @Override
    public void handleRain(final World w, final BlockPos pos) {
        if (Platform.isServer()) {
            w.removeBlock(pos, false);
        }
    }

    @Override
    public int getLightValue(final BlockState state, final IBlockReader w, final BlockPos pos) {
        final PaintSplotchesTileEntity tp = this.getTileEntity(w, pos);

        if (tp != null) {
            return tp.getLightLevel();
        }

        return 0;
    }

    @Override
    public boolean isAir(final BlockState state, final IBlockReader world, final BlockPos pos) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }

}
