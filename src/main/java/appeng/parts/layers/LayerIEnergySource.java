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

package appeng.parts.layers;


import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerBase;
import appeng.api.parts.LayerFlags;
import appeng.util.Platform;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;


public class LayerIEnergySource extends LayerBase implements IEnergySource
{

	private TileEntity getEnergySourceTile()
	{
		final IPartHost host = (IPartHost) this;
		return host.getTile();
	}

	private World getEnergySourceWorld()
	{
		if( this.getEnergySourceTile() == null )
		{
			return null;
		}
		return this.getEnergySourceTile().getWorldObj();
	}

	private boolean isTileValid()
	{
		final TileEntity te = this.getEnergySourceTile();
		if( te == null )
		{
			return false;
		}
		return !te.isInvalid();
	}

	private void addToENet()
	{
		if( this.getEnergySourceWorld() == null )
		{
			return;
		}

		// re-add
		this.removeFromENet();

		if( !this.isInIC2() && Platform.isServer() && this.isTileValid() )
		{
			this.getLayerFlags().add( LayerFlags.IC2_ENET );
			MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileLoadEvent( (IEnergyTile) this.getEnergySourceTile() ) );
		}
	}

	private void removeFromENet()
	{
		if( this.getEnergySourceWorld() == null )
		{
			return;
		}

		if( this.isInIC2() && Platform.isServer() )
		{
			this.getLayerFlags().remove( LayerFlags.IC2_ENET );
			MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileUnloadEvent( (IEnergyTile) this.getEnergySourceTile() ) );
		}
	}

	private boolean interestedInIC2()
	{
		if( !( (IPartHost) this ).isInWorld() )
		{
			return false;
		}

		int interested = 0;
		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			final IPart part = this.getPart( dir );
			if( part instanceof IEnergyTile )
			{
				interested++;
			}
		}
		return interested == 1;// if more then one tile is interested we need to abandon...
	}

	@Override
	public void partChanged()
	{
		super.partChanged();

		if( this.interestedInIC2() )
		{
			this.addToENet();
		}
		else
		{
			this.removeFromENet();
		}
	}

	@Override
	public boolean emitsEnergyTo( final TileEntity receiver, final ForgeDirection direction )
	{
		if( !this.isInIC2() )
		{
			return false;
		}

		final IPart part = this.getPart( direction );
		if( part instanceof IEnergySink )
		{
			return ( (IEnergyEmitter) part ).emitsEnergyTo( receiver, direction );
		}
		return false;
	}

	private boolean isInIC2()
	{
		return this.getLayerFlags().contains( LayerFlags.IC2_ENET );
	}

	@Override
	public double getOfferedEnergy()
	{
		if( !this.isInIC2() )
		{
			return 0;
		}

		// this is a flawed implementation, that requires a change to the IC2 API.

		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			final IPart part = this.getPart( dir );
			if( part instanceof IEnergySource )
			{
				// use lower number cause ic2 deletes power it sends that isn't received.
				return ( (IEnergySource) part ).getOfferedEnergy();
			}
		}

		return 0;
	}

	@Override
	public void drawEnergy( final double amount )
	{
		// this is a flawed implementation, that requires a change to the IC2 API.

		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			final IPart part = this.getPart( dir );
			if( part instanceof IEnergySource )
			{
				( (IEnergySource) part ).drawEnergy( amount );
				return;
			}
		}
	}

	@Override
	public int getSourceTier()
	{
		// this is a flawed implementation, that requires a change to the IC2 API.

		for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
		{
			final IPart part = this.getPart( dir );
			if( part instanceof IEnergySource )
			{
				return ( (IEnergySource) part ).getSourceTier();
			}
		}

		return 0;
	}
}
