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

package appeng.block.misc;


import java.util.EnumSet;
import java.util.Random;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.core.features.AEFeature;
import appeng.tile.misc.TileLightDetector;


public class BlockLightDetector extends BlockQuartzTorch
{

	public BlockLightDetector()
	{
		super( BlockLightDetector.class );
		this.setTileEntity( TileLightDetector.class );
		this.setFeature( EnumSet.of( AEFeature.LightDetector ) );
	}

	@Override
	public int isProvidingWeakPower( IBlockAccess w, int x, int y, int z, int side )
	{
		if( w instanceof World && ( (TileLightDetector) this.getTileEntity( w, x, y, z ) ).isReady() )
		{
			return ( (World) w ).getBlockLightValue( x, y, z ) - 6;
		}

		return 0;
	}

	@Override
	public void onNeighborChange( IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ )
	{
		super.onNeighborChange( world, x, y, z, tileX, tileY, tileZ );

		TileLightDetector tld = this.getTileEntity( world, x, y, z );
		if( tld != null )
		{
			tld.updateLight();
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( World w, int x, int y, int z, Random r )
	{
		// cancel out lightning
	}
}
