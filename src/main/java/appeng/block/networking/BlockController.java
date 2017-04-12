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

package appeng.block.networking;


import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockController;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileController;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.EnumSet;


public class BlockController extends AEBaseTileBlock
{

	public BlockController()
	{
		super( Material.iron );
		this.setTileEntity( TileController.class );
		this.setHardness( 6 );
		this.setFeature( EnumSet.of( AEFeature.Channels ) );
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block neighborBlock )
	{
		final TileController tc = this.getTileEntity( w, x, y, z );
		if( tc != null )
		{
			tc.onNeighborChange( false );
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockController getRenderer()
	{
		return new RenderBlockController();
	}
}
