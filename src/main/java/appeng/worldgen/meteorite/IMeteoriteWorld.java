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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IMeteoriteWorld {
    int minX(int in);

    int minZ(int in);

    int maxX(int in);

    int maxZ(int in);

    boolean contains(BlockPos pos);

    Block getBlock(BlockPos pos);

    boolean canBlockSeeTheSky(BlockPos pos);

    TileEntity getTileEntity(BlockPos pos);

    IWorld getWorld();

    void setBlock(BlockPos pos, final BlockState state, final int flags);

    void setBlock(BlockPos pos, BlockState blk);

    BlockState getBlockState(BlockPos pos);
}