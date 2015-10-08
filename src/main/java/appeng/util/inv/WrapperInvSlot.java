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

package appeng.util.inv;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class WrapperInvSlot
{

	private final IInventory inv;

	public WrapperInvSlot( final IInventory inv )
	{
		this.inv = inv;
	}

	public IInventory getWrapper( final int slot )
	{
		return new InternalInterfaceWrapper( this.inv, slot );
	}

	protected boolean isItemValid( final ItemStack itemstack )
	{
		return true;
	}

	private class InternalInterfaceWrapper implements IInventory
	{

		private final IInventory inv;
		private final int slot;

		public InternalInterfaceWrapper( final IInventory target, final int slot )
		{
			this.inv = target;
			this.slot = slot;
		}

		@Override
		public int getSizeInventory()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot( final int i )
		{
			return this.inv.getStackInSlot( this.slot );
		}

		@Override
		public ItemStack decrStackSize( final int i, final int num )
		{
			return this.inv.decrStackSize( this.slot, num );
		}

		@Override
		public ItemStack getStackInSlotOnClosing( final int i )
		{
			return this.inv.getStackInSlotOnClosing( this.slot );
		}

		@Override
		public void setInventorySlotContents( final int i, final ItemStack itemstack )
		{
			this.inv.setInventorySlotContents( this.slot, itemstack );
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
			return WrapperInvSlot.this.isItemValid( itemstack ) && this.inv.isItemValidForSlot( this.slot, itemstack );
		}
	}
}
