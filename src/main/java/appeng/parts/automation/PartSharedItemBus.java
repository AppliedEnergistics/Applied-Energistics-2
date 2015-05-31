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

package appeng.parts.automation;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public abstract class PartSharedItemBus extends PartUpgradeable implements IGridTickable
{

	final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );
	int adaptorHash = 0;
	InventoryAdaptor adaptor;
	boolean lastRedstone = false;

	public PartSharedItemBus( ItemStack is )
	{
		super( is );
	}

	protected final int availableSlots()
	{
		return Math.min( 1 + this.getInstalledUpgrades( Upgrades.CAPACITY ) * 4, this.config.getSizeInventory() );
	}

	@Override
	public void upgradesChanged()
	{
		this.updateState();
	}

	@Override
	public void readFromNBT( net.minecraft.nbt.NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.config.readFromNBT( extra, "config" );
	}

	@Override
	public void writeToNBT( net.minecraft.nbt.NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.config.writeToNBT( extra, "config" );
	}

	@Override
	public final IInventory getInventoryByName( String name )
	{
		if( name.equals( "config" ) )
		{
			return this.config;
		}

		return super.getInventoryByName( name );
	}

	private void updateState()
	{
		try
		{
			if( !this.isSleeping() )
			{
				this.proxy.getTick().wakeDevice( this.proxy.getNode() );
			}
			else
			{
				this.proxy.getTick().sleepDevice( this.proxy.getNode() );
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}

	final InventoryAdaptor getHandler()
	{
		TileEntity self = this.getHost().getTile();
		TileEntity target = this.getTileEntity( self, self.xCoord + this.side.offsetX, self.yCoord + this.side.offsetY, self.zCoord + this.side.offsetZ );

		int newAdaptorHash = Platform.generateTileHash( target );

		if( this.adaptorHash == newAdaptorHash && newAdaptorHash != 0 )
		{
			return this.adaptor;
		}

		this.adaptorHash = newAdaptorHash;
		this.adaptor = InventoryAdaptor.getAdaptor( target, this.side.getOpposite() );

		return this.adaptor;
	}

	private TileEntity getTileEntity( TileEntity self, int x, int y, int z )
	{
		World w = self.getWorldObj();

		if( w.getChunkProvider().chunkExists( x >> 4, z >> 4 ) )
		{
			return w.getTileEntity( x, y, z );
		}

		return null;
	}

	@Override
	public final void onNeighborChanged()
	{
		this.updateState();
		if( this.lastRedstone != this.host.hasRedstone( this.side ) )
		{
			this.lastRedstone = !this.lastRedstone;
			if( this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE )
			{
				this.doBusWork();
			}
		}
	}

	abstract TickRateModulation doBusWork();
}
