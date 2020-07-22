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

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

/**
 * This is used to retrieve the ExtendedState of a block for facade rendering.
 * It fakes the block at BlockPos provided as the BlockState provided.
 *
 * @author covers1624
 */
public class FacadeBlockAccess implements IBlockDisplayReader {

    private final IBlockDisplayReader world;
    private final BlockPos pos;
    private final Direction side;
    private final BlockState state;

    public FacadeBlockAccess(IBlockDisplayReader world, BlockPos pos, Direction side, BlockState state) {
        this.world = world;
        this.pos = pos;
        this.side = side;
        this.state = state;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return this.world.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.pos == pos) {
            return this.state;
        }
        return this.world.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return world.getFluidState(pos);
    }

    @Override
    public WorldLightManager getLightManager() {
        return world.getLightManager();
    }

    @Override
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return world.getBlockColor(blockPosIn, colorResolverIn);
    }
}
