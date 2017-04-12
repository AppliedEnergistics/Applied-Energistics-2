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


import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.GuiBridge;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;
import java.util.Random;


public class BlockQuantumLinkChamber extends BlockQuantumBase
{

	public BlockQuantumLinkChamber()
	{
		super( AEGlassMaterial.INSTANCE );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World w, final int bx, final int by, final int bz, final Random r )
	{
		final TileQuantumBridge bridge = this.getTileEntity( w, bx, by, bz );
		if( bridge != null )
		{
			if( bridge.hasQES() )
			{
				if( CommonHelper.proxy.shouldAddParticles( r ) )
				{
					CommonHelper.proxy.spawnEffect( EffectType.Energy, w, bx + 0.5, by + 0.5, bz + 0.5, null );
				}
			}
		}
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer p, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileQuantumBridge tg = this.getTileEntity( w, x, y, z );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_QNB );
			}
			return true;
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity e, final boolean isVisual )
	{
		final double onePixel = 2.0 / 16.0;
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		final double onePixel = 2.0 / 16.0;
		out.add( AxisAlignedBB.getBoundingBox( onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel ) );
	}
}
