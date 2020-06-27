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

import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;

import appeng.block.AEBaseBlock;

/**
 * This block is used to fill empty space in spatial dimensions and delinates
 * the border of a spatial dimensions's usable space.
 */
public class MatrixFrameBlock extends AEBaseBlock {

    private static final Material MATERIAL = new Material(MaterialColor.AIR, false, true, true, false, false, false,
            false, PushReaction.PUSH_ONLY);

    public MatrixFrameBlock() {
        super(Properties.create(MATERIAL).hardnessAndResistance(-1.0F, 6000000.0F).notSolid().noDrops());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos,
            ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        // This also prevents any blocks from being placed on this block!
        return VoxelShapes.empty();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public void onExplosionDestroy(final World world, final BlockPos pos, final Explosion explosion) {
        // Don't explode.
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightValue(BlockState state, BlockView worldIn, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean canEntityDestroy(final BlockState state, final BlockView world, final BlockPos pos,
            final Entity entity) {
        return false;
    }
}
