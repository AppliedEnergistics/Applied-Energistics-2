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


import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderSpatialPylon;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.spatial.TileSpatialPylon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.EnumSet;


public class BlockSpatialPylon extends AEBaseTileBlock
{

	public BlockSpatialPylon()
	{
		super( AEGlassMaterial.INSTANCE );
		this.setTileEntity( TileSpatialPylon.class );
		this.setFeature( EnumSet.of( AEFeature.SpatialIO ) );
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block junk )
	{
		final TileSpatialPylon tsp = this.getTileEntity( w, x, y, z );
		if( tsp != null )
		{
			tsp.onNeighborBlockChange();
		}
	}

	@Override
	public int getLightValue( final IBlockAccess w, final int x, final int y, final int z )
	{
		final TileSpatialPylon tsp = this.getTileEntity( w, x, y, z );
		if( tsp != null )
		{
			return tsp.getLightValue();
		}
		return super.getLightValue( w, x, y, z );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderSpatialPylon getRenderer()
	{
		return new RenderSpatialPylon();
	}
}
