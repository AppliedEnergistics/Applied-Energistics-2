/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.client.render.cablebus;

import javax.annotation.Nullable;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.lighting.LevelLightEngine;

/**
 * This is used to retrieve the ExtendedState of a block for facade rendering. It fakes the block at BlockPos provided
 * as the BlockState provided.
 *
 * @author covers1624
 */
public class FacadeBlockAccess implements BlockAndTintGetter {

    private final BlockAndTintGetter world;
    private final BlockPos pos;
    private final net.minecraft.core.Direction side;
    private final BlockState state;

    public FacadeBlockAccess(BlockAndTintGetter world, BlockPos pos, net.minecraft.core.Direction side, net.minecraft.world.level.block.state.BlockState state) {
        this.world = world;
        this.pos = pos;
        this.side = side;
        this.state = state;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.pos == pos) {
            return this.state;
        }
        return this.world.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(net.minecraft.core.BlockPos pos) {
        return world.getFluidState(pos);
    }

    // This is for diffuse lighting
    @Override
    public float getShade(net.minecraft.core.Direction p_230487_1_, boolean p_230487_2_) {
        return world.getShade(p_230487_1_, p_230487_2_);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return world.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return world.getBlockTint(blockPosIn, colorResolverIn);
    }
}
