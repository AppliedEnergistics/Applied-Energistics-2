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

package appeng.container.slot;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.container.AEBaseContainer;
import appeng.tile.inventory.AppEngInternalInventory;


public class AppEngSlot extends Slot
{

	public final int defX;
	public final int defY;
	public boolean isDraggable = true;
	public boolean isPlayerSide = false;
	public AEBaseContainer myContainer = null;
	public int IIcon = -1;
	public hasCalculatedValidness isValid;
	public boolean isDisplay = false;

	public AppEngSlot( IInventory inv, int idx, int x, int y )
	{
		super( inv, idx, x, y );
		this.defX = x;
		this.defY = y;
		this.isValid = hasCalculatedValidness.NotAvailable;
	}

	public Slot setNotDraggable()
	{
		this.isDraggable = false;
		return this;
	}

	public Slot setPlayerSide()
	{
		this.isPlayerSide = true;
		return this;
	}

	public String getTooltip()
	{
		return null;
	}

	public void clearStack()
	{
		super.putStack( null );
	}

	@Override
	public boolean isItemValid( ItemStack par1ItemStack )
	{
		if( this.isEnabled() )
		{
			return super.isItemValid( par1ItemStack );
		}
		return false;
	}

	@Override
	public ItemStack getStack()
	{
		if( !this.isEnabled() )
		{
			return null;
		}

		if( this.inventory.getSizeInventory() <= this.getSlotIndex() )
		{
			return null;
		}

		if( this.isDisplay )
		{
			this.isDisplay = false;
			return this.getDisplayStack();
		}
		return super.getStack();
	}

	@Override
	public void putStack( ItemStack par1ItemStack )
	{
		if( this.isEnabled() )
		{
			super.putStack( par1ItemStack );

			if( this.myContainer != null )
			{
				this.myContainer.onSlotChange( this );
			}
		}
	}

	@Override
	public void onSlotChanged()
	{
		if( this.inventory instanceof AppEngInternalInventory )
		{
			( (AppEngInternalInventory) this.inventory ).markDirty( this.getSlotIndex() );
		}
		else
		{
			super.onSlotChanged();
		}

		this.isValid = hasCalculatedValidness.NotAvailable;
	}

	@Override
	public boolean canTakeStack( EntityPlayer par1EntityPlayer )
	{
		if( this.isEnabled() )
		{
			return super.canTakeStack( par1EntityPlayer );
		}
		return false;
	}

	@Override
	public boolean func_111238_b()
	{
		return this.isEnabled();
	}

	public ItemStack getDisplayStack()
	{
		return super.getStack();
	}

	public boolean isEnabled()
	{
		return true;
	}

	public float getOpacityOfIcon()
	{
		return 0.4f;
	}

	public boolean renderIconWithItem()
	{
		return false;
	}

	public int getIcon()
	{
		return this.IIcon;
	}

	public boolean isPlayerSide()
	{
		return this.isPlayerSide;
	}

	public boolean shouldDisplay()
	{
		return this.isEnabled();
	}

	public enum hasCalculatedValidness
	{
		NotAvailable, Valid, Invalid
	}
}
