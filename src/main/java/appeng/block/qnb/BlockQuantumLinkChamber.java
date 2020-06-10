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

import javax.annotation.Nullable;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.client.EffectType;
import appeng.core.AppEng;
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
	public void randomDisplayTick( final BlockState state, final World w, final BlockPos pos, final Random rand )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, pos );
		if( bridge != null )
		{
			if( bridge.hasQES() )
			{
				if( AppEng.proxy.shouldAddParticles( rand ) )
				{
					AppEng.proxy.spawnEffect( EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, null );
				}
			}
		}
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileQuantumBridge tg = this.getTileEntity( w, pos );
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
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		final double onePixel = 2.0 / 16.0;
		return Collections.singletonList( new AxisAlignedBB( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		final double onePixel = 2.0 / 16.0;
		out.add( new AxisAlignedBB( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}
}
