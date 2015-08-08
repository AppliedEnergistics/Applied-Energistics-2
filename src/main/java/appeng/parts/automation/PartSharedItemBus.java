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

package appeng.parts.automation;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
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

	protected final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );
	private int adaptorHash = 0;
	private InventoryAdaptor adaptor;
	private boolean lastRedstone = false;

	public PartSharedItemBus( ItemStack is )
	{
		super( is );
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
	public IInventory getInventoryByName( String name )
	{
		if( name.equals( "config" ) )
		{
			return this.config;
		}

		return super.getInventoryByName( name );
	}

	@Override
	public void onNeighborChanged()
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

	protected InventoryAdaptor getHandler()
	{
		final TileEntity self = this.getHost().getTile();
		final TileEntity target = this.getTileEntity( self, self.getPos().offset( side.getFacing() ) );

		final int newAdaptorHash = Platform.generateTileHash( target );

		if( this.adaptorHash == newAdaptorHash && newAdaptorHash != 0 )
		{
			return this.adaptor;
		}

		this.adaptorHash = newAdaptorHash;
		this.adaptor = InventoryAdaptor.getAdaptor( target, this.side.getFacing().getOpposite() );

		return this.adaptor;
	}

	private TileEntity getTileEntity( TileEntity self, BlockPos pos )
	{
		World w = self.getWorld();

		if( w.getChunkProvider().chunkExists( pos.getX() >> 4, pos.getZ() >> 4 ) )
		{
			return w.getTileEntity( pos );
		}
		
		return null;
	}

	protected int availableSlots()
	{
		return Math.min( 1 + this.getInstalledUpgrades( Upgrades.CAPACITY ) * 4, this.config.getSizeInventory() );
	}

	protected int calculateItemsToSend()
	{
		switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
		{
			default:
			case 0:
				return 1;
			case 1:
				return 8;
			case 2:
				return 32;
			case 3:
				return 64;
			case 4:
				return 96;
		}
	}

	/**
	 * Checks if the bus can actually do something.
	 *
	 * Currently this tests if the chunk for the target is actually loaded.
	 *
	 * @return true, if the the bus should do its work.
	 */
	protected boolean canDoBusWork()
	{
		final TileEntity self = this.getHost().getTile();
		final BlockPos selfPos = self.getPos().offset( this.side.getFacing() )
		final int xCoordinate = selfPos.getX();
		final int zCoordinate = selfPos.getZ();

		final World world = self.getWorld();

		return world != null && world.getChunkProvider().chunkExists( xCoordinate >> 4, zCoordinate >> 4 );
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

	protected abstract TickRateModulation doBusWork();
}
