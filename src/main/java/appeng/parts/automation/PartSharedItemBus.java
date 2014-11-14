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

	private TileEntity getTileEntity(TileEntity self, int x, int y, int z)
	{
		World w = self.getWorldObj();

		if ( w.getChunkProvider().chunkExists( x >> 4, z >> 4 ) )
		{
			return w.getTileEntity( x, y, z );
		}

		return null;
	}

	public PartSharedItemBus(Class c, ItemStack is) {
		super( c, is );
	}

	@Override
	public void upgradesChanged()
	{
		updateState();
	}

	protected int availableSlots()
	{
		return Math.min( 1 + getInstalledUpgrades( Upgrades.CAPACITY ) * 4, config.getSizeInventory() );
	}

	@Override
	public void writeToNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		config.writeToNBT( extra, "config" );
	}

	@Override
	public void readFromNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		config.readFromNBT( extra, "config" );
	}

	final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );

	int adaptorHash = 0;
	InventoryAdaptor adaptor;

	InventoryAdaptor getHandler()
	{
		TileEntity self = getHost().getTile();
		TileEntity target = getTileEntity( self, self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newAdaptorHash = Platform.generateTileHash( target );

		if ( adaptorHash == newAdaptorHash && newAdaptorHash != 0 )
			return adaptor;

		adaptorHash = newAdaptorHash;
		adaptor = InventoryAdaptor.getAdaptor( target, side.getOpposite() );

		return adaptor;
	}

	boolean lastRedstone = false;

	abstract TickRateModulation doBusWork();

	@Override
	public void onNeighborChanged()
	{
		updateState();
		if ( lastRedstone != host.hasRedstone( side ) )
		{
			lastRedstone = !lastRedstone;
			if ( lastRedstone && getRSMode() == RedstoneMode.SIGNAL_PULSE )
				doBusWork();
		}
	}

	private void updateState()
	{
		try
		{
			if ( !isSleeping() )
				proxy.getTick().wakeDevice( proxy.getNode() );
			else
				proxy.getTick().sleepDevice( proxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return config;

		return super.getInventoryByName( name );
	}

}
