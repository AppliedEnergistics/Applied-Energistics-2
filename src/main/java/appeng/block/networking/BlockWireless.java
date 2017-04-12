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
import appeng.client.render.blocks.RenderBlockWireless;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;


public class BlockWireless extends AEBaseTileBlock implements ICustomCollision
{

	public BlockWireless()
	{
		super( AEGlassMaterial.INSTANCE );
		this.setTileEntity( TileWireless.class );
		this.setLightOpacity( 0 );
		this.isFullSize = false;
		this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.Core, AEFeature.WirelessAccessTerminal ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockWireless getRenderer()
	{
		return new RenderBlockWireless();
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer p, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileWireless tg = this.getTileEntity( w, x, y, z );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_WIRELESS );
			}
			return true;
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity e, final boolean isVisual )
	{
		final TileWireless tile = this.getTileEntity( w, x, y, z );
		if( tile != null )
		{
			final ForgeDirection forward = tile.getForward();

			double minX = 0;
			double minY = 0;
			double minZ = 0;
			double maxX = 1;
			double maxY = 1;
			double maxZ = 1;

			switch( forward )
			{
				case DOWN:
					minZ = minX = 3.0 / 16.0;
					maxZ = maxX = 13.0 / 16.0;
					maxY = 1.0;
					minY = 5.0 / 16.0;
					break;
				case EAST:
					minZ = minY = 3.0 / 16.0;
					maxZ = maxY = 13.0 / 16.0;
					maxX = 11.0 / 16.0;
					minX = 0.0;
					break;
				case NORTH:
					minY = minX = 3.0 / 16.0;
					maxY = maxX = 13.0 / 16.0;
					maxZ = 1.0;
					minZ = 5.0 / 16.0;
					break;
				case SOUTH:
					minY = minX = 3.0 / 16.0;
					maxY = maxX = 13.0 / 16.0;
					maxZ = 11.0 / 16.0;
					minZ = 0.0;
					break;
				case UP:
					minZ = minX = 3.0 / 16.0;
					maxZ = maxX = 13.0 / 16.0;
					maxY = 11.0 / 16.0;
					minY = 0.0;
					break;
				case WEST:
					minZ = minY = 3.0 / 16.0;
					maxZ = maxY = 13.0 / 16.0;
					maxX = 1.0;
					minX = 5.0 / 16.0;
					break;
				default:
					break;
			}

			return Collections.singletonList( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		final TileWireless tile = this.getTileEntity( w, x, y, z );
		if( tile != null )
		{
			final ForgeDirection forward = tile.getForward();

			double minX = 0;
			double minY = 0;
			double minZ = 0;
			double maxX = 1;
			double maxY = 1;
			double maxZ = 1;

			switch( forward )
			{
				case DOWN:
					minZ = minX = 3.0 / 16.0;
					maxZ = maxX = 13.0 / 16.0;
					maxY = 1.0;
					minY = 5.0 / 16.0;
					break;
				case EAST:
					minZ = minY = 3.0 / 16.0;
					maxZ = maxY = 13.0 / 16.0;
					maxX = 11.0 / 16.0;
					minX = 0.0;
					break;
				case NORTH:
					minY = minX = 3.0 / 16.0;
					maxY = maxX = 13.0 / 16.0;
					maxZ = 1.0;
					minZ = 5.0 / 16.0;
					break;
				case SOUTH:
					minY = minX = 3.0 / 16.0;
					maxY = maxX = 13.0 / 16.0;
					maxZ = 11.0 / 16.0;
					minZ = 0.0;
					break;
				case UP:
					minZ = minX = 3.0 / 16.0;
					maxZ = maxX = 13.0 / 16.0;
					maxY = 11.0 / 16.0;
					minY = 0.0;
					break;
				case WEST:
					minZ = minY = 3.0 / 16.0;
					maxZ = maxY = 13.0 / 16.0;
					maxX = 1.0;
					minX = 5.0 / 16.0;
					break;
				default:
					break;
			}

			out.add( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		else
		{
			out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
		}
	}
}
