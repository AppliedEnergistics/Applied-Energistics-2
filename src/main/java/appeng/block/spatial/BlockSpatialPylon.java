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


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.spatial.TileSpatialPylon;

public class BlockSpatialPylon extends AEBaseTileBlock
{

	public BlockSpatialPylon()
	{
		super( AEGlassMaterial.INSTANCE );
		this.setTileEntity( TileSpatialPylon.class );
	}

	@Override
	public void neighborChanged( final IBlockState state, final World w, final BlockPos pos, final Block neighborBlock )
	{
		final TileSpatialPylon tsp = this.getTileEntity( w, pos );
		if( tsp != null )
		{
			tsp.neighborChanged();
		}
	}

	@Override
	public int getLightValue( final IBlockState state, final IBlockAccess w, final BlockPos pos )
	{
		final TileSpatialPylon tsp = this.getTileEntity( w, pos );
		if( tsp != null )
		{
			return tsp.getLightValue();
		}
		return super.getLightValue( state, w, pos );
	}
}
