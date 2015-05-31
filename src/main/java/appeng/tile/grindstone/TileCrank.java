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

package appeng.tile.grindstone;


import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.ICrankable;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;


public final class TileCrank extends AEBaseTile implements ICustomCollision
{

	final int ticksPerRotation = 18;

	// sided values..
	public float visibleRotation = 0;
	public int charge = 0;

	public int hits = 0;
	public int rotation = 0;

	@TileEvent( TileEventType.TICK )
	public void Tick_TileCrank()
	{
		if( this.rotation > 0 )
		{
			this.visibleRotation -= 360 / ( this.ticksPerRotation );
			this.charge++;
			if( this.charge >= this.ticksPerRotation )
			{
				this.charge -= this.ticksPerRotation;
				ICrankable g = this.getGrinder();
				if( g != null )
				{
					g.applyTurn();
				}
			}

			this.rotation--;
		}
	}

	public final ICrankable getGrinder()
	{
		if( Platform.isClient() )
		{
			return null;
		}

		ForgeDirection grinder = this.getUp().getOpposite();
		TileEntity te = this.worldObj.getTileEntity( this.xCoord + grinder.offsetX, this.yCoord + grinder.offsetY, this.zCoord + grinder.offsetZ );
		if( te instanceof ICrankable )
		{
			return (ICrankable) te;
		}
		return null;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileCrank( ByteBuf data )
	{
		this.rotation = data.readInt();
		return false;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileCrank( ByteBuf data )
	{
		data.writeInt( this.rotation );
	}

	@Override
	public final void setOrientation( ForgeDirection inForward, ForgeDirection inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getBlockType().onNeighborBlockChange( this.worldObj, this.xCoord, this.yCoord, this.zCoord, Platform.AIR_BLOCK );
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	/**
	 * return true if this should count towards stats.
	 */
	public final boolean power()
	{
		if( Platform.isClient() )
		{
			return false;
		}

		if( this.rotation < 3 )
		{
			ICrankable g = this.getGrinder();
			if( g != null )
			{
				if( g.canTurn() )
				{
					this.hits = 0;
					this.rotation += this.ticksPerRotation;
					this.markForUpdate();
					return true;
				}
				else
				{
					this.hits++;
					if( this.hits > 10 )
					{
						this.worldObj.func_147480_a( this.xCoord, this.yCoord, this.zCoord, false );
						// worldObj.destroyBlock( xCoord, yCoord, zCoord, false );
					}
				}
			}
		}

		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( World w, int x, int y, int z, Entity e, boolean isVisual )
	{
		double xOff = -0.15 * this.getUp().offsetX;
		double yOff = -0.15 * this.getUp().offsetY;
		double zOff = -0.15 * this.getUp().offsetZ;
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85 ) );
	}

	@Override
	public void addCollidingBlockToList( World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e )
	{
		double xOff = -0.15 * this.getUp().offsetX;
		double yOff = -0.15 * this.getUp().offsetY;
		double zOff = -0.15 * this.getUp().offsetZ;
		out.add( AxisAlignedBB.getBoundingBox( xOff + 0.15, yOff + 0.15, zOff + 0.15,// ahh
				xOff + 0.85, yOff + 0.85, zOff + 0.85 ) );
	}
}
