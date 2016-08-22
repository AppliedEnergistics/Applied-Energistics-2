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


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;

public abstract class BlockQuantumBase extends AEBaseTileBlock implements ICustomCollision
{

	public BlockQuantumBase( final Material mat )
	{
		super( mat );
		this.setTileEntity( TileQuantumBridge.class );
		final float shave = 2.0f / 16.0f;
		this.boundingBox = new AxisAlignedBB( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		this.setLightOpacity( 0 );
		this.setFullSize( this.setOpaque( false ) );
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void neighborChanged( final IBlockState state, final World w, final BlockPos pos, final Block neighborBlock )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			bridge.neighborUpdate();
		}
	}

	@Override
	public void breakBlock( final World w, final BlockPos pos, final IBlockState state )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			bridge.breakCluster();
		}

		super.breakBlock( w, pos, state );
	}

}
