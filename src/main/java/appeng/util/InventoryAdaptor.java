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

package appeng.util;


import appeng.util.inv.*;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;

/**
 * Universal Facade for other inventories. Used to conveniently interact with various types of inventories. This is not
 * used for
 * actually monitoring an inventory. It is just for insertion and extraction, and is primarily used by import/export
 * buses.
 */
public abstract class InventoryAdaptor implements Iterable<ItemSlot>
{
	@CapabilityInject( IItemRepository.class)
	public static Capability<IItemRepository> ITEM_REPOSITORY_CAPABILITY = null;

	public static InventoryAdaptor getAdaptor( final TileEntity te, final EnumFacing d )
	{
		if( te != null )
		{
			if( ITEM_REPOSITORY_CAPABILITY != null && te.hasCapability( ITEM_REPOSITORY_CAPABILITY, d ) )
			{
				IItemRepository itemRepository = te.getCapability( ITEM_REPOSITORY_CAPABILITY, d );
				if (itemRepository != null){
					return new AdaptorItemRepository( itemRepository );
				}
			}
			else if( te.hasCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d ) )
			{

				// Attempt getting an IItemHandler for the given side via caps
				IItemHandler itemHandler = te.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d );
				if( itemHandler != null )
				{
					return new AdaptorItemHandler( itemHandler );
				}
			}
		}
		return null;
	}

	public static InventoryAdaptor getAdaptor( final EntityPlayer te )
	{
		if( te != null )
		{
			return new AdaptorItemHandlerPlayerInv( te );
		}
		return null;
	}

	// return what was extracted.
	public abstract ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination );

	public abstract ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination );

	// return what was extracted.
	public abstract ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination );

	public abstract ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination );

	// return what isn't used...
	public abstract ItemStack addItems( ItemStack toBeAdded );

	public abstract ItemStack simulateAdd( ItemStack toBeSimulated );

	public abstract boolean containsItems();

	public abstract boolean hasSlots();

}
