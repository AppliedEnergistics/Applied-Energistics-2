/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.worldgen.meteorite;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import appeng.util.Platform;

public class StandardWorld implements IMeteoriteWorld {

    private final IWorld w;

    public StandardWorld(final IWorld w) {
        this.w = w;
    }

    @Override
    public int minX(final int in) {
        return in;
    }

    @Override
    public int minZ(final int in) {
        return in;
    }

    @Override
    public int maxX(final int in) {
        return in;
    }

    @Override
    public int maxZ(final int in) {
        return in;
    }

    @Override
    public boolean contains(BlockPos pos) {
        return true;
    }

    @Override
    public Block getBlock(BlockPos pos) {
        if (this.range(pos)) {
            return this.getWorld().getBlockState(new BlockPos(pos)).getBlock();
        }
        return Platform.AIR_BLOCK;
    }

    @Override
    public boolean canBlockSeeTheSky(BlockPos pos) {
        if (this.range(pos)) {
            return this.getWorld().canBlockSeeSky(new BlockPos(pos));
        }
        return false;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (this.range(pos)) {
            return this.getWorld().getTileEntity(new BlockPos(pos));
        }
        return null;
    }

    @Override
    public IWorld getWorld() {
        return this.w;
    }

    @Override
    public void setBlock(BlockPos pos, final BlockState blk) {
        if (this.range(pos)) {
            // FIXME: Attempt at reducing impact of placing meteors by passing 16|32 here
            this.getWorld().setBlockState(new BlockPos(pos), blk, 16 | 32);
        }
    }

    public boolean range(BlockPos pos) {
        return true;
    }

    @Override
    public void setBlock(BlockPos pos, final BlockState state, final int l) {
        if (this.range(pos)) {
            this.w.setBlockState(new BlockPos(pos), state, l);
        }
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (this.range(pos)) {
            return this.w.getBlockState(new BlockPos(pos));
        }
        return Blocks.AIR.getDefaultState();
    }
}
