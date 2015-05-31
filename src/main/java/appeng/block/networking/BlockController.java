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

package appeng.block.networking;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockController;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileController;


public final class BlockController extends AEBaseBlock
{

	public BlockController()
	{
		super( Material.iron );
		this.setTileEntity( TileController.class );
		this.setHardness( 6 );
		this.setFeature( EnumSet.of( AEFeature.Channels ) );
	}

	@Override
	public final void onNeighborBlockChange( World w, int x, int y, int z, Block neighborBlock )
	{
		TileController tc = this.getTileEntity( w, x, y, z );
		if( tc != null )
		{
			tc.onNeighborChange( false );
		}
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockController.class;
	}
}
