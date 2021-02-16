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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;

/**
 * This is used to retrieve the ExtendedState of a block for facade rendering. It fakes the block at BlockPos provided
 * as the BlockState provided.
 *
 * @author covers1624
 */
public class FacadeBlockAccess implements BlockRenderView {

    private final BlockRenderView world;
    private final BlockPos pos;
    private final Direction side;
    private final BlockState state;

    public FacadeBlockAccess(BlockRenderView world, BlockPos pos, Direction side, BlockState state) {
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
    public FluidState getFluidState(BlockPos pos) {
        return world.getFluidState(pos);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return world.getLightingProvider();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return world.getBrightness(direction, shaded);
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return world.getColor(pos, colorResolver);
    }

    @Override
    public int getHeight() {
        return 0; // FIXME 1.17
    }

    @Override
    public int getBottomY() {
        return 0; // FIXME 1.17
    }
}
