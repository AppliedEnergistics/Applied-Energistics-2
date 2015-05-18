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

package appeng.parts.layers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerBase;


/**
 * Inventory wrapper for parts,
 *
 * this is considerably more complicated then the other wrappers as it requires creating a "unified inventory".
 *
 * You must use {@link ISidedInventory} instead of {@link IInventory}.
 *
 * If your inventory changes in between placement and removal, you must trigger a PartChange on the {@link IPartHost} so
 * it can recalculate the inventory wrapper.
 */
public final class LayerISidedInventory extends LayerBase implements ISidedInventory
{

	// a simple empty array for empty stuff..
	private static final int[] NULL_SIDES = new int[] {};

	InvLayerData invLayer = null;

	/**
	 * Recalculate inventory wrapper cache.
	 */
	@Override
	public final void notifyNeighbors()
	{
		// cache of inventory state.
		int[][] sideData = null;
		List<ISidedInventory> inventories = null;
		List<InvSot> slots = null;

		inventories = new ArrayList<ISidedInventory>();
		int slotCount = 0;

		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			IPart bp = this.getPart( side );
			if( bp instanceof ISidedInventory )
			{
				ISidedInventory part = (ISidedInventory) bp;
				slotCount += part.getSizeInventory();
				inventories.add( part );
			}
		}

		if( inventories.isEmpty() || slotCount == 0 )
		{
			inventories = null;
		}
		else
		{
			sideData = new int[][] { NULL_SIDES, NULL_SIDES, NULL_SIDES, NULL_SIDES, NULL_SIDES, NULL_SIDES };
			slots = new ArrayList<InvSot>( Collections.nCopies( slotCount, (InvSot) null ) );

			int offsetForLayer = 0;
			int offsetForPart = 0;
			for( ISidedInventory sides : inventories )
			{
				offsetForPart = 0;
				slotCount = sides.getSizeInventory();

				ForgeDirection currentSide = ForgeDirection.UNKNOWN;
				for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
				{
					if( this.getPart( side ) == sides )
					{
						currentSide = side;
						break;
					}
				}

				int[] cSidesList = sideData[currentSide.ordinal()] = new int[slotCount];
				for( int cSlot = 0; cSlot < slotCount; cSlot++ )
				{
					cSidesList[cSlot] = offsetForLayer;
					slots.set( offsetForLayer, new InvSot( sides, offsetForPart ) );
					offsetForLayer++;
					offsetForPart++;
				}
			}
		}

		if( sideData == null || slots == null )
		{
			this.invLayer = null;
		}
		else
		{
			this.invLayer = new InvLayerData( sideData, inventories, slots );
		}

		// make sure inventory is updated before we call FMP.
		super.notifyNeighbors();
	}

	@Override
	public final int getSizeInventory()
	{
		if( this.invLayer == null )
		{
			return 0;
		}

		return this.invLayer.getSizeInventory();
	}

	@Override
	public final ItemStack getStackInSlot( int slot )
	{
		if( this.invLayer == null )
		{
			return null;
		}

		return this.invLayer.getStackInSlot( slot );
	}

	@Override
	public final ItemStack decrStackSize( int slot, int amount )
	{
		if( this.invLayer == null )
		{
			return null;
		}

		return this.invLayer.decreaseStackSize( slot, amount );
	}

	@Override
	public final ItemStack getStackInSlotOnClosing( int slot )
	{
		return null;
	}

	@Override
	public final void setInventorySlotContents( int slot, ItemStack itemstack )
	{
		if( this.invLayer == null )
		{
			return;
		}

		this.invLayer.setInventorySlotContents( slot, itemstack );
	}

	@Override
	public final String getInventoryName()
	{
		return "AEMultiPart";
	}

	@Override
	public final boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public final int getInventoryStackLimit()
	{
		return 64; // no options here.
	}

	@Override
	public final boolean isUseableByPlayer( EntityPlayer entityplayer )
	{
		return false;
	}

	@Override
	public final void openInventory()
	{
	}

	@Override
	public final void closeInventory()
	{
	}

	@Override
	public final boolean isItemValidForSlot( int slot, ItemStack itemstack )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.isItemValidForSlot( slot, itemstack );
	}

	@Override
	public final void markDirty()
	{
		if( this.invLayer != null )
		{
			this.invLayer.markDirty();
		}

		super.markForSave();
	}

	@Override
	public final int[] getAccessibleSlotsFromSide( int side )
	{
		if( this.invLayer != null )
		{
			return this.invLayer.getAccessibleSlotsFromSide( side );
		}

		return NULL_SIDES;
	}

	@Override
	public final boolean canInsertItem( int slot, ItemStack itemstack, int side )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.canInsertItem( slot, itemstack, side );
	}

	@Override
	public final boolean canExtractItem( int slot, ItemStack itemstack, int side )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.canExtractItem( slot, itemstack, side );
	}
}
