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

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderSpatialPylon;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.spatial.TileSpatialPylon;

public class BlockSpatialPylon extends AEBaseBlock
{

	public BlockSpatialPylon() {
		super( BlockSpatialPylon.class, AEGlassMaterial.instance );
		this.setFeature( EnumSet.of( AEFeature.SpatialIO ) );
		this.setTileEntity( TileSpatialPylon.class );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileSpatialPylon tsp = this.getTileEntity( w, x, y, z );
		if ( tsp != null )
			tsp.onNeighborBlockChange();
	}

	@Override
	public int getLightValue(IBlockAccess w, int x, int y, int z)
	{
		TileSpatialPylon tsp = this.getTileEntity( w, x, y, z );
		if ( tsp != null )
			return tsp.getLightValue();
		return super.getLightValue( w, x, y, z );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderSpatialPylon.class;
	}

}
