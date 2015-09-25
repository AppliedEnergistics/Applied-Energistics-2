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

package appeng.parts.layers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerBase;
import appeng.api.util.AEPartLocation;


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
public class LayerISidedInventory extends LayerBase implements ISidedInventory
{

	// a simple empty array for empty stuff..
	private static final int[] NULL_SIDES = {};

	InvLayerData invLayer = null;

	/**
	 * Recalculate inventory wrapper cache.
	 */
	@Override
	public void notifyNeighbors()
	{
		// cache of inventory state.
		int[][] sideData = null;
		List<ISidedInventory> inventories = null;
		List<InvSot> slots = null;

		inventories = new ArrayList<ISidedInventory>();
		int slotCount = 0;

		for( AEPartLocation side : AEPartLocation.SIDE_LOCATIONS )
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

				AEPartLocation currentSide = AEPartLocation.INTERNAL;
				for( AEPartLocation side : AEPartLocation.SIDE_LOCATIONS )
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
	public int getSizeInventory()
	{
		if( this.invLayer == null )
		{
			return 0;
		}

		return this.invLayer.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( int slot )
	{
		if( this.invLayer == null )
		{
			return null;
		}

		return this.invLayer.getStackInSlot( slot );
	}

	@Override
	public ItemStack decrStackSize( int slot, int amount )
	{
		if( this.invLayer == null )
		{
			return null;
		}

		return this.invLayer.decreaseStackSize( slot, amount );
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int slot )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( int slot, ItemStack itemstack )
	{
		if( this.invLayer == null )
		{
			return;
		}

		this.invLayer.setInventorySlotContents( slot, itemstack );
	}

	@Override
	public String getName()
	{
		return "AEMultiPart";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64; // no options here.
	}

	@Override
	public boolean isUseableByPlayer( EntityPlayer entityplayer )
	{
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot( int slot, ItemStack itemstack )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.isItemValidForSlot( slot, itemstack );
	}

	@Override
	public void markDirty()
	{
		if( this.invLayer != null )
		{
			this.invLayer.markDirty();
		}

		super.markForSave();
	}

	@Override
	public int[] getSlotsForFace( EnumFacing side )
	{
		if( this.invLayer != null )
		{
			return this.invLayer.getSlotsForFace( side );
		}

		return NULL_SIDES;
	}

	@Override
	public boolean canInsertItem( int slot, ItemStack itemstack, EnumFacing side )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.canInsertItem( slot, itemstack, side );
	}

	@Override
	public boolean canExtractItem( int slot, ItemStack itemstack, EnumFacing side )
	{
		if( this.invLayer == null )
		{
			return false;
		}

		return this.invLayer.canExtractItem( slot, itemstack, side );
	}

	@Override
	public int getField(
			int id )
	{
		return 0;
	}

	@Override
	public void setField(
			int id,
			int value )
	{
		
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return null;
	}
}
