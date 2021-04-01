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

package appeng.block.spatial;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;

/**
 * This block is used to fill empty space in spatial dimensions and delinates the border of a spatial dimensions's
 * usable space.
 */
public class MatrixFrameBlock extends AEBaseBlock {

    private static final Material MATERIAL = new Material(MaterialColor.AIR, false, true, true, false, false, false,
            PushReaction.field_15970);

    public MatrixFrameBlock() {
        super(Properties.create(MATERIAL).hardnessAndResistance(-1.0F, 6000000.0F).notSolid().noDrops());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.field_11455;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> list) {
        // do nothing
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // This also prevents any blocks from being placed on this block!
        return VoxelShapes.empty();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        world.setBlockState(pos, getDefaultState(), 3);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return 1.0f;
    }

}
