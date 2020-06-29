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
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.block.AEBaseTileBlock;
import appeng.tile.misc.PaintSplotchesBlockEntity;
import appeng.util.Platform;

public class PaintSplotchesBlock extends AEBaseTileBlock<PaintSplotchesBlockEntity> {
    public PaintSplotchesBlock() {
        super(defaultProps(Material.WATER, MaterialColor.CLEAR));
        this.setFullSize(false);
        this.setOpaque(false);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> itemStacks) {
        // do nothing
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
    public void fillWithRain(final World w, final BlockPos pos) {
        if (Platform.isServer()) {
            w.removeBlock(pos, false);
        }
    }

    @Override
    public int getLightValue(final BlockState state, final BlockView w, final BlockPos pos) {
        final PaintSplotchesBlockEntity tp = this.getBlockEntity(w, pos);

        if (tp != null) {
            return tp.getLightLevel();
        }

        return 0;
    }

    @Override
    public boolean isAir(final BlockState state, final BlockView world, final BlockPos pos) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state, ItemPlacementContext useContext) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState p_225541_1_, Fluid p_225541_2_) {
        return true;
    }

}
