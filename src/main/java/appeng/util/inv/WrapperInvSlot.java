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

package appeng.util.inv;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class WrapperInvSlot
{

	private final IInventory inv;

	public WrapperInvSlot( IInventory inv )
	{
		this.inv = inv;
	}

	public final IInventory getWrapper( int slot )
	{
		return new InternalInterfaceWrapper( this.inv, slot );
	}

	protected boolean isItemValid( ItemStack itemstack )
	{
		return true;
	}

	final class InternalInterfaceWrapper implements IInventory
	{

		private final IInventory inv;
		private final int slot;

		public InternalInterfaceWrapper( IInventory target, int slot )
		{
			this.inv = target;
			this.slot = slot;
		}

		@Override
		public final int getSizeInventory()
		{
			return 1;
		}

		@Override
		public final ItemStack getStackInSlot( int i )
		{
			return this.inv.getStackInSlot( this.slot );
		}

		@Override
		public final ItemStack decrStackSize( int i, int num )
		{
			return this.inv.decrStackSize( this.slot, num );
		}

		@Override
		public final ItemStack getStackInSlotOnClosing( int i )
		{
			return this.inv.getStackInSlotOnClosing( this.slot );
		}

		@Override
		public final void setInventorySlotContents( int i, ItemStack itemstack )
		{
			this.inv.setInventorySlotContents( this.slot, itemstack );
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
			return WrapperInvSlot.this.isItemValid( itemstack ) && this.inv.isItemValidForSlot( this.slot, itemstack );
		}
	}
}
