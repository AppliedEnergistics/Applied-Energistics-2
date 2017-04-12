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

package appeng.items.contents;


import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGridHost;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public class NetworkToolViewer implements INetworkTool
{

	private final AppEngInternalInventory inv;
	private final ItemStack is;
	private final IGridHost gh;

	public NetworkToolViewer( final ItemStack is, final IGridHost gHost )
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
	public int getSizeInventory()
	{
		return this.inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return this.inv.getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return this.inv.decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( final int i )
	{
		return this.inv.getStackInSlotOnClosing( i );
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.inv.setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInventoryName()
	{
		return this.inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.inv.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		this.inv.markDirty();
		this.inv.writeToNBT( Platform.openNbtData( this.is ), "inv" );
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer entityplayer )
	{
		return this.inv.isUseableByPlayer( entityplayer );
	}

	@Override
	public void openInventory()
	{
		this.inv.openInventory();
	}

	@Override
	public void closeInventory()
	{
		this.inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return this.inv.isItemValidForSlot( i, itemstack ) && itemstack.getItem() instanceof IUpgradeModule && ( (IUpgradeModule) itemstack.getItem() ).getType( itemstack ) != null;
	}

	@Override
	public ItemStack getItemStack()
	{
		return this.is;
	}

	@Override
	public IGridHost getGridHost()
	{
		return this.gh;
	}
}
