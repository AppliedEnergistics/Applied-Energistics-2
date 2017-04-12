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

package appeng.block.qnb;


import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.EnumSet;


public abstract class BlockQuantumBase extends AEBaseTileBlock implements ICustomCollision
{

	public BlockQuantumBase( final Material mat )
	{
		super( mat );
		this.setTileEntity( TileQuantumBridge.class );
		final float shave = 2.0f / 16.0f;
		this.setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		this.setLightOpacity( 0 );
		this.isFullSize = this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block pointlessNumber )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null )
		{
			bridge.neighborUpdate();
		}
	}

	@Override
	public void breakBlock( final World w, final int x, final int y, final int z, final Block a, final int b )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null )
		{
			bridge.breakCluster();
		}

		super.breakBlock( w, x, y, z, a, b );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderQNB getRenderer()
	{
		return new RenderQNB();
	}

}
