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

package appeng.block.qnb;


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;


public class BlockQuantumRing extends AEBaseBlock implements ICustomCollision
{

	public BlockQuantumRing()
	{
		super( Material.iron );
		this.setTileEntity( TileQuantumBridge.class );
		float shave = 2.0f / 16.0f;
		this.setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		this.setLightOpacity( 1 );
		this.isFullSize = this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
	}

	@Override
	public void onNeighborBlockChange( World w, int x, int y, int z, Block pointlessNumber )
	{
		TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null )
		{
			bridge.neighborUpdate();
		}
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQNB.class;
	}

	@Override
	public void breakBlock( World w, int x, int y, int z, Block a, int b )
	{
		TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null )
		{
			bridge.breakCluster();
		}

		super.breakBlock( w, x, y, z, a, b );
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( World w, int x, int y, int z, Entity e, boolean isVisual )
	{
		double OnePx = 2.0 / 16.0;
		TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null && bridge.isCorner() )
		{
			OnePx = 4.0 / 16.0;
		}
		else if( bridge != null && bridge.isFormed() )
		{
			OnePx = 1.0 / 16.0;
		}
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) );
	}

	@Override
	public void addCollidingBlockToList( World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e )
	{
		double OnePx = 2.0 / 16.0;
		TileQuantumBridge bridge = this.getTileEntity( w, x, y, z );
		if( bridge != null && bridge.isCorner() )
		{
			OnePx = 4.0 / 16.0;
		}
		else if( bridge != null && bridge.isFormed() )
		{
			OnePx = 1.0 / 16.0;
		}
		out.add( AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) );
	}
}
