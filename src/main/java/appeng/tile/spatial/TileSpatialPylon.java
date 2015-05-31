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

package appeng.tile.spatial;


import java.util.EnumSet;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.SpatialPylonCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;


public final class TileSpatialPylon extends AENetworkTile implements IAEMultiBlock
{

	public static final int DISPLAY_END_MIN = 0x01;
	public static final int DISPLAY_END_MAX = 0x02;
	public static final int DISPLAY_MIDDLE = 0x01 + 0x02;
	public static final int DISPLAY_X = 0x04;
	public static final int DISPLAY_Y = 0x08;
	public static final int DISPLAY_Z = 0x04 + 0x08;
	public static final int MB_STATUS = 0x01 + 0x02 + 0x04 + 0x08;

	public static final int DISPLAY_ENABLED = 0x10;
	public static final int DISPLAY_POWERED_ENABLED = 0x20;
	public static final int NET_STATUS = 0x10 + 0x20;
	final SpatialPylonCalculator calc = new SpatialPylonCalculator( this );
	int displayBits = 0;
	SpatialPylonCluster cluster;
	boolean didHaveLight = false;

	public TileSpatialPylon()
	{
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK );
		this.gridProxy.setIdlePowerUsage( 0.5 );
		this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", this.getItemFromTile( this ), true );
	}

	@Override
	public final void onChunkUnload()
	{
		this.disconnect( false );
		super.onChunkUnload();
	}

	@Override
	public void onReady()
	{
		super.onReady();
		this.onNeighborBlockChange();
	}

	@Override
	public final void invalidate()
	{
		this.disconnect( false );
		super.invalidate();
	}

	public final void onNeighborBlockChange()
	{
		this.calc.calculateMultiblock( this.worldObj, this.getLocation() );
	}

	@Override
	public final void disconnect( boolean b )
	{
		if( this.cluster != null )
		{
			this.cluster.destroy();
			this.updateStatus( null );
		}
	}

	@Override
	public final SpatialPylonCluster getCluster()
	{
		return this.cluster;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	public final void updateStatus( SpatialPylonCluster c )
	{
		this.cluster = c;
		this.gridProxy.setValidSides( c == null ? EnumSet.noneOf( ForgeDirection.class ) : EnumSet.allOf( ForgeDirection.class ) );
		this.recalculateDisplay();
	}

	public final void recalculateDisplay()
	{
		int oldBits = this.displayBits;

		this.displayBits = 0;

		if( this.cluster != null )
		{
			if( this.cluster.min.equals( this.getLocation() ) )
			{
				this.displayBits = this.DISPLAY_END_MIN;
			}
			else if( this.cluster.max.equals( this.getLocation() ) )
			{
				this.displayBits = this.DISPLAY_END_MAX;
			}
			else
			{
				this.displayBits = this.DISPLAY_MIDDLE;
			}

			switch( this.cluster.currentAxis )
			{
				case X:
					this.displayBits |= this.DISPLAY_X;
					break;
				case Y:
					this.displayBits |= this.DISPLAY_Y;
					break;
				case Z:
					this.displayBits |= this.DISPLAY_Z;
					break;
				default:
					this.displayBits = 0;
					break;
			}

			try
			{
				if( this.gridProxy.getEnergy().isNetworkPowered() )
				{
					this.displayBits |= this.DISPLAY_POWERED_ENABLED;
				}

				if( this.cluster.isValid && this.gridProxy.isActive() )
				{
					this.displayBits |= this.DISPLAY_ENABLED;
				}
			}
			catch( GridAccessException e )
			{
				// nothing?
			}
		}

		if( oldBits != this.displayBits )
		{
			this.markForUpdate();
		}
	}

	@Override
	public final void markForUpdate()
	{
		super.markForUpdate();
		boolean hasLight = this.getLightValue() > 0;
		if( hasLight != this.didHaveLight )
		{
			this.didHaveLight = hasLight;
			this.worldObj.func_147451_t( this.xCoord, this.yCoord, this.zCoord );
			// worldObj.updateAllLightTypes( xCoord, yCoord, zCoord );
		}
	}

	@Override
	public final boolean canBeRotated()
	{
		return false;
	}

	public final int getLightValue()
	{
		if( ( this.displayBits & this.DISPLAY_POWERED_ENABLED ) == this.DISPLAY_POWERED_ENABLED )
		{
			return 8;
		}
		return 0;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSpatialPylon( ByteBuf data )
	{
		int old = this.displayBits;
		this.displayBits = data.readByte();
		return old != this.displayBits;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSpatialPylon( ByteBuf data )
	{
		data.writeByte( this.displayBits );
	}

	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void activeRender( MENetworkChannelsChanged c )
	{
		this.recalculateDisplay();
	}

	public final int getDisplayBits()
	{
		return this.displayBits;
	}
}
