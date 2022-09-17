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


import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class StandardWorld implements IMeteoriteWorld {

    private final World w;

    public StandardWorld(final World w) {
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
    public boolean isNether() {
        return !this.getWorld().provider.isNether();
    }

    @Override
    public Block getBlock(final int x, final int y, final int z) {
        if (this.range(x, y, z)) {
            return this.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock();
        }
        return Platform.AIR_BLOCK;
    }

    @Override
    public boolean canBlockSeeTheSky(final int x, final int y, final int z) {
        if (this.range(x, y, z)) {
            return this.getWorld().canBlockSeeSky(new BlockPos(x, y, z));
        }
        return false;
    }

    @Override
    public TileEntity getTileEntity(final int x, final int y, final int z) {
        if (this.range(x, y, z)) {
            return this.getWorld().getTileEntity(new BlockPos(x, y, z));
        }
        return null;
    }

    @Override
    public World getWorld() {
        return this.w;
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final Block blk) {
        if (this.range(x, y, z)) {
            this.getWorld().setBlockState(new BlockPos(x, y, z), blk.getDefaultState());
        }
    }

    @Override
    public void done() {

    }

    public boolean range(final int x, final int y, final int z) {
        return true;
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final IBlockState state, final int l) {
        if (this.range(x, y, z)) {
            this.w.setBlockState(new BlockPos(x, y, z), state, l);
        }
    }

    @Override
    public IBlockState getBlockState(final int x, final int y, final int z) {
        if (this.range(x, y, z)) {
            return this.w.getBlockState(new BlockPos(x, y, z));
        }
        return Blocks.AIR.getDefaultState();
    }
}
