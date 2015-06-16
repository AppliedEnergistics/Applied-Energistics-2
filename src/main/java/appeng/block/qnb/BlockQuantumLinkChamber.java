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
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import appeng.api.util.AEPartLocation;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.GuiBridge;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;


public class BlockQuantumLinkChamber extends BlockQuantumBase
{

	public BlockQuantumLinkChamber()
	{
		super( AEGlassMaterial.INSTANCE );
	}
	
	@Override
	public void randomDisplayTick(
			World w,
			BlockPos pos,
			IBlockState state,
			Random rand )
	{
		TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			if( bridge.hasQES() )
			{
				if( CommonHelper.proxy.shouldAddParticles( rand ) )
				{
					CommonHelper.proxy.spawnEffect( EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, null );
				}
			}
		}
	}

	@Override
	public boolean onActivated(
			World w,
			BlockPos pos,
			EntityPlayer p,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		TileQuantumBridge tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_QNB );
			}
			return true;
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(
			World w,
			BlockPos pos,
			Entity thePlayer,
			boolean b )
	{
		double onePixel = 2.0 / 16.0;
		return Collections.singletonList( AxisAlignedBB.fromBounds( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}
	
	@Override
	public void addCollidingBlockToList(
			World w,
			BlockPos pos,
			AxisAlignedBB bb,
			List<AxisAlignedBB> out,
			Entity e )
	{
		double onePixel = 2.0 / 16.0;
		out.add( AxisAlignedBB.fromBounds( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}
}
