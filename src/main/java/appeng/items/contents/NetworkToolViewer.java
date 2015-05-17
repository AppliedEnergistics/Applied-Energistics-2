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

package appeng.items.contents;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGridHost;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;


public final class NetworkToolViewer implements INetworkTool
{

	final AppEngInternalInventory inv;
	final ItemStack is;
	final IGridHost gh;

	public NetworkToolViewer( ItemStack is, IGridHost gHost )
	{
		this.is = is;
		this.gh = gHost;
		this.inv = new AppEngInternalInventory( null, 9 );
		if( is.hasTagCompound() ) // prevent crash when opening network status screen.
		{
			this.inv.readFromNBT( Platform.openNbtData( is ), "inv" );
		}
	}

	@Override
	public final int getSizeInventory()
	{
		return this.inv.getSizeInventory();
	}

	@Override
	public final ItemStack getStackInSlot( int i )
	{
		return this.inv.getStackInSlot( i );
	}

	@Override
	public final ItemStack decrStackSize( int i, int j )
	{
		return this.inv.decrStackSize( i, j );
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int i )
	{
		return this.inv.getStackInSlotOnClosing( i );
	}

	@Override
	public final void setInventorySlotContents( int i, ItemStack itemstack )
	{
		this.inv.setInventorySlotContents( i, itemstack );
	}

	@Override
	public final String getInventoryName()
	{
		return this.inv.getInventoryName();
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return this.inv.hasCustomInventoryName();
	}

	@Override
	public final int getInventoryStackLimit()
	{
		return this.inv.getInventoryStackLimit();
	}

	@Override
	public final void markDirty()
	{
		this.inv.markDirty();
		this.inv.writeToNBT( Platform.openNbtData( this.is ), "inv" );
	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer entityplayer )
	{
		return this.inv.isUseableByPlayer( entityplayer );
	}

	@Override
	public final void openInventory()
	{
		this.inv.openInventory();
	}

	@Override
	public final void closeInventory()
	{
		this.inv.closeInventory();
	}

	@Override
	public final boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return this.inv.isItemValidForSlot( i, itemstack ) && itemstack.getItem() instanceof IUpgradeModule && ( (IUpgradeModule) itemstack.getItem() ).getType( itemstack ) != null;
	}

	@Override
	public final ItemStack getItemStack()
	{
		return this.is;
	}

	@Override
	public final IGridHost getGridHost()
	{
		return this.gh;
	}
}
