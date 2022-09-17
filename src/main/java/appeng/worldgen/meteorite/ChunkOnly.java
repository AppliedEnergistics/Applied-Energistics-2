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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class ChunkOnly extends StandardWorld {

    private final Chunk target;
    private final int cx;
    private final int cz;
    private int verticalBits = 0;

    public ChunkOnly(final World w, final int cx, final int cz) {
        super(w);
        this.target = w.getChunkFromChunkCoords(cx, cz);
        this.cx = cx;
        this.cz = cz;
    }

    @Override
    public int minX(final int in) {
        return Math.max(in, this.cx << 4);
    }

    @Override
    public int minZ(final int in) {
        return Math.max(in, this.cz << 4);
    }

    @Override
    public int maxX(final int in) {
        return Math.min(in, (this.cx + 1) << 4);
    }

    @Override
    public int maxZ(final int in) {
        return Math.min(in, (this.cz + 1) << 4);
    }

    @Override
    public Block getBlock(final int x, final int y, final int z) {
        if (this.range(x, y, z)) {
            return this.target.getBlockState(x, y, z).getBlock();
        }
        return Platform.AIR_BLOCK;
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final Block blk) {
        if (this.range(x, y, z)) {
            this.verticalBits |= 1 << (y >> 4);
            this.getWorld().setBlockState(new BlockPos(x, y, z), blk.getDefaultState());
        }
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final IBlockState state, final int flags) {
        if (this.range(x, y, z)) {
            this.verticalBits |= 1 << (y >> 4);
            this.getWorld().setBlockState(new BlockPos(x, y, z), state, flags & (~2));
        }
    }

    @Override
    public void done() {
        if (this.verticalBits != 0) {
            Platform.sendChunk(this.target, this.verticalBits);
        }
    }

    @Override
    public boolean range(final int x, final int y, final int z) {
        return this.cx == (x >> 4) && this.cz == (z >> 4);
    }
}
