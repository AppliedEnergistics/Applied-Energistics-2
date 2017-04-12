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

package appeng.container.implementations;


import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;


public class ContainerSpatialIOPort extends AEBaseContainer
{

	@GuiSync( 0 )
	public long currentPower;
	@GuiSync( 1 )
	public long maxPower;
	@GuiSync( 2 )
	public long reqPower;
	@GuiSync( 3 )
	public long eff;
	private IGrid network;
	private int delay = 40;

	public ContainerSpatialIOPort( final InventoryPlayer ip, final TileSpatialIOPort spatialIOPort )
	{
		super( ip, spatialIOPort, null );

		if( Platform.isServer() )
		{
			this.network = spatialIOPort.getGridNode( ForgeDirection.UNKNOWN ).getGrid();
		}

		this.addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.SPATIAL_STORAGE_CELLS, spatialIOPort, 0, 52, 48, this.getInventoryPlayer() ) );
		this.addSlotToContainer( new SlotOutput( spatialIOPort, 1, 113, 48, SlotRestrictedInput.PlacableItemType.SPATIAL_STORAGE_CELLS.IIcon ) );

		this.bindPlayerInventory( ip, 0, 197 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.delay++;
			if( this.delay > 15 && this.network != null )
			{
				this.delay = 0;

				final IEnergyGrid eg = this.network.getCache( IEnergyGrid.class );
				final ISpatialCache sc = this.network.getCache( ISpatialCache.class );
				if( eg != null )
				{
					this.setCurrentPower( (long) ( 100.0 * eg.getStoredPower() ) );
					this.setMaxPower( (long) ( 100.0 * eg.getMaxStoredPower() ) );
					this.setRequiredPower( (long) ( 100.0 * sc.requiredPower() ) );
					this.setEfficency( (long) ( 100.0f * sc.currentEfficiency() ) );
				}
			}
		}

		super.detectAndSendChanges();
	}

	public long getCurrentPower()
	{
		return this.currentPower;
	}

	private void setCurrentPower( final long currentPower )
	{
		this.currentPower = currentPower;
	}

	public long getMaxPower()
	{
		return this.maxPower;
	}

	private void setMaxPower( final long maxPower )
	{
		this.maxPower = maxPower;
	}

	public long getRequiredPower()
	{
		return this.reqPower;
	}

	private void setRequiredPower( final long reqPower )
	{
		this.reqPower = reqPower;
	}

	public long getEfficency()
	{
		return this.eff;
	}

	private void setEfficency( final long eff )
	{
		this.eff = eff;
	}
}
