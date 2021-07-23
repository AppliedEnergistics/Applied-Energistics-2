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

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseBlock;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * This block is used to fill empty space in spatial dimensions and delinates the border of a spatial dimensions's
 * usable space.
 */
public class MatrixFrameBlock extends AEBaseBlock {

    private static final Material MATERIAL = new Material(MaterialColor.NONE, false, true, true, false, false, false,
            PushReaction.PUSH_ONLY);

    public MatrixFrameBlock() {
        super(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of(MATERIAL).strength(-1.0F, 6000000.0F).noOcclusion().noDrops());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public VoxelShape getCollisionShape(net.minecraft.world.level.block.state.BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos,
                                        CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos, CollisionContext context) {
        // This also prevents any blocks from being placed on this block!
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(net.minecraft.world.level.block.state.BlockState state, LevelReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public void wasExploded(final Level world, final BlockPos pos, final Explosion explosion) {
        // Don't explode.
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean canEntityDestroy(final net.minecraft.world.level.block.state.BlockState state, final BlockGetter world, final BlockPos pos,
                                    final Entity entity) {
        return false;
    }
}
