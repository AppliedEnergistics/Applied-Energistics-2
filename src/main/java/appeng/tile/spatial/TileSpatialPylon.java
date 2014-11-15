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

package appeng.tile.spatial;

import io.netty.buffer.ByteBuf;

import java.util.EnumSet;

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

public class TileSpatialPylon extends AENetworkTile implements IAEMultiBlock
{

	public final int DISPLAY_END_MIN = 0x01;
	public final int DISPLAY_END_MAX = 0x02;
	public final int DISPLAY_MIDDLE = 0x01 + 0x02;
	public final int DISPLAY_X = 0x04;
	public final int DISPLAY_Y = 0x08;
	public final int DISPLAY_Z = 0x04 + 0x08;
	public final int MB_STATUS = 0x01 + 0x02 + 0x04 + 0x08;

	public final int DISPLAY_ENABLED = 0x10;
	public final int DISPLAY_POWERED_ENABLED = 0x20;
	public final int NET_STATUS = 0x10 + 0x20;

	int displayBits = 0;
	SpatialPylonCluster cluster;
	final SpatialPylonCalculator calc = new SpatialPylonCalculator( this );

	boolean didHaveLight = false;

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", getItemFromTile( this ), true );
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileSpatialPylon(ByteBuf data)
	{
		int old = displayBits;
		displayBits = data.readByte();
		return old != displayBits;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileSpatialPylon(ByteBuf data)
	{
		data.writeByte( displayBits );
	}

	public TileSpatialPylon() {
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK );
		gridProxy.setIdlePowerUsage( 0.5 );
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public void onReady()
	{
		super.onReady();
		onNeighborBlockChange();
	}

	@Override
	public void markForUpdate()
	{
		super.markForUpdate();
		boolean hasLight = getLightValue() > 0;
		if ( hasLight != didHaveLight )
		{
			didHaveLight = hasLight;
			worldObj.func_147451_t( xCoord, yCoord, zCoord );
			// worldObj.updateAllLightTypes( xCoord, yCoord, zCoord );
		}
	}

	public int getLightValue()
	{
		if ( (displayBits & DISPLAY_POWERED_ENABLED) == DISPLAY_POWERED_ENABLED )
		{
			return 8;
		}
		return 0;
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void activeRender(MENetworkChannelsChanged c)
	{
		recalculateDisplay();
	}

	@Override
	public void invalidate()
	{
		disconnect( false );
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		disconnect( false );
		super.onChunkUnload();
	}

	public void onNeighborBlockChange()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	@Override
	public SpatialPylonCluster getCluster()
	{
		return cluster;
	}

	public void recalculateDisplay()
	{
		int oldBits = displayBits;

		displayBits = 0;

		if ( cluster != null )
		{
			if ( cluster.min.equals( getLocation() ) )
				displayBits = DISPLAY_END_MIN;
			else if ( cluster.max.equals( getLocation() ) )
				displayBits = DISPLAY_END_MAX;
			else
				displayBits = DISPLAY_MIDDLE;

			switch (cluster.currentAxis)
			{
			case X:
				displayBits |= DISPLAY_X;
				break;
			case Y:
				displayBits |= DISPLAY_Y;
				break;
			case Z:
				displayBits |= DISPLAY_Z;
				break;
			default:
				displayBits = 0;
				break;
			}

			try
			{
				if ( gridProxy.getEnergy().isNetworkPowered() )
					displayBits |= DISPLAY_POWERED_ENABLED;

				if ( cluster.isValid && gridProxy.isActive() )
					displayBits |= DISPLAY_ENABLED;
			}
			catch (GridAccessException e)
			{
				// nothing?
			}

		}

		if ( oldBits != displayBits )
			markForUpdate();
	}

	public void updateStatus(SpatialPylonCluster c)
	{
		cluster = c;
		gridProxy.setValidSides( c == null ? EnumSet.noneOf( ForgeDirection.class ) : EnumSet.allOf( ForgeDirection.class ) );
		recalculateDisplay();
	}

	@Override
	public void disconnect(boolean b)
	{
		if ( cluster != null )
		{
			cluster.destroy();
			updateStatus( null );
		}
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	public int getDisplayBits()
	{
		return displayBits;
	}

}
