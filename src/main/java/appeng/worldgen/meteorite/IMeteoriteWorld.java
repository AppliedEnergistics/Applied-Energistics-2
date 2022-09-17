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
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public interface IMeteoriteWorld {
    int minX(int in);

    int minZ(int in);

    int maxX(int in);

    int maxZ(int in);

    boolean isNether();

    Block getBlock(int x, int y, int z);

    boolean canBlockSeeTheSky(int i, int j, int k);

    TileEntity getTileEntity(int x, int y, int z);

    World getWorld();

    void setBlock(int i, int j, int k, Block blk);

    void setBlock(int i, int j, int k, IBlockState state, int l);

    void done();

    IBlockState getBlockState(int x, int y, int z);
}