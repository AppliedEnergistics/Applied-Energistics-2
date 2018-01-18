/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.me.storage;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;


/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public abstract class CellInventoryBase<T extends IAEStack<T>> implements ICellInventory<T>
{

	private static final String ITEM_TYPE_TAG = "it";
	private static final String ITEM_COUNT_TAG = "ic";
	private static final String ITEM_SLOT = "#";
	private static final String ITEM_SLOT_COUNT = "@";
	protected static final String ITEM_PRE_FORMATTED_COUNT = "PF";
	protected static final String ITEM_PRE_FORMATTED_SLOT = "PF#";
	protected static final String ITEM_PRE_FORMATTED_NAME = "PN";
	protected static final String ITEM_PRE_FORMATTED_FUZZY = "FP";
	private static String[] itemSlots;
	private static String[] itemSlotCount;
	private final NBTTagCompound tagCompound;
	protected final ISaveProvider container;
	private int maxItemTypes = 63;
	private short storedItems = 0;
	private int storedItemCount = 0;
	protected IItemList<T> cellItems;
	protected ItemStack i;
	protected IStorageCell cellType;
	private final int itemsPerByte;

	protected CellInventoryBase( final NBTTagCompound data, final ISaveProvider container, final int itemsPerByte )
	{
		this.tagCompound = data;
		this.container = container;
		this.itemsPerByte = itemsPerByte;
	}

	protected CellInventoryBase( final ItemStack o, final ISaveProvider container, final int itemsPerByte ) throws AppEngException
	{
		this.itemsPerByte = itemsPerByte;
		if( itemSlots == null )
		{
			itemSlots = new String[this.maxItemTypes];
			itemSlotCount = new String[this.maxItemTypes];

			for( int x = 0; x < this.maxItemTypes; x++ )
			{
				itemSlots[x] = ITEM_SLOT + x;
				itemSlotCount[x] = ITEM_SLOT_COUNT + x;
			}
		}

		if( o == null )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		this.cellType = null;
		this.i = o;

		final Item type = this.i.getItem();
		if( type instanceof IStorageCell )
		{
			this.cellType = (IStorageCell) this.i.getItem();
			this.maxItemTypes = this.cellType.getTotalTypes( this.i );
		}

		if( this.cellType == null )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		if( !this.cellType.isStorageCell( this.i ) )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		if( this.maxItemTypes > 63 )
		{
			this.maxItemTypes = 63;
		}
		if( this.maxItemTypes < 1 )
		{
			this.maxItemTypes = 1;
		}

		this.container = container;
		this.tagCompound = Platform.openNbtData( o );
		this.storedItems = this.tagCompound.getShort( ITEM_TYPE_TAG );
		this.storedItemCount = this.tagCompound.getInteger( ITEM_COUNT_TAG );
		this.cellItems = null;
	}

	protected boolean isEmpty( final IMEInventory meInventory )
	{
		return meInventory.getAvailableItems( getChannel().createList() ).isEmpty();
	}

	protected IItemList<T> getCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = getChannel().createList();
			this.loadCellItems();
		}

		return this.cellItems;
	}

	protected void updateItemCount( final long delta )
	{
		this.storedItemCount += delta;
		this.tagCompound.setInteger( ITEM_COUNT_TAG, this.storedItemCount );
	}

	protected void saveChanges()
	{
		// cellItems.clean();
		int itemCount = 0;

		// add new pretty stuff...
		int x = 0;
		for( final T v : this.cellItems )
		{
			itemCount += v.getStackSize();

			final NBTTagCompound g = new NBTTagCompound();
			v.writeToNBT( g );
			this.tagCompound.setTag( itemSlots[x], g );

			this.tagCompound.setInteger( itemSlotCount[x], (int) v.getStackSize() );

			x++;
		}

		// NBTBase tagType = tagCompound.getTag( ITEM_TYPE_TAG );
		// NBTBase tagCount = tagCompound.getTag( ITEM_COUNT_TAG );
		final short oldStoredItems = this.storedItems;

		/*
		 * if ( tagType instanceof NBTTagShort ) ((NBTTagShort) tagType).data = storedItems = (short) cellItems.size();
		 * else
		 */
		this.storedItems = (short) this.cellItems.size();
		if( this.cellItems.isEmpty() )
		{
			this.tagCompound.removeTag( ITEM_TYPE_TAG );
		}
		else
		{
			this.tagCompound.setShort( ITEM_TYPE_TAG, this.storedItems );
		}

		/*
		 * if ( tagCount instanceof NBTTagInt ) ((NBTTagInt) tagCount).data = storedItemCount = itemCount; else
		 */
		this.storedItemCount = itemCount;
		if( itemCount == 0 )
		{
			this.tagCompound.removeTag( ITEM_COUNT_TAG );
		}
		else
		{
			this.tagCompound.setInteger( ITEM_COUNT_TAG, itemCount );
		}

		// clean any old crusty stuff...
		for( ; x < oldStoredItems && x < this.maxItemTypes; x++ )
		{
			this.tagCompound.removeTag( itemSlots[x] );
			this.tagCompound.removeTag( itemSlotCount[x] );
		}

		if( this.container != null )
		{
			this.container.saveChanges( this );
		}
	}

	private void loadCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = getChannel().createList();
		}

		this.cellItems.resetStatus(); // clears totals and stuff.

		final int types = (int) this.getStoredItemTypes();

		for( int slot = 0; slot < types; slot++ )
		{
			NBTTagCompound compoundTag = this.tagCompound.getCompoundTag( itemSlots[slot] );
			int stackSize = this.tagCompound.getInteger( itemSlotCount[slot] );
			this.loadCellItem( compoundTag, stackSize );
		}

		// cellItems.clean();
	}

	protected abstract void loadCellItem( NBTTagCompound compoundTag, int stackSize );

	@Override
	public IItemList getAvailableItems( final IItemList out )
	{
		for( final T i : this.getCellItems() )
		{
			out.add( i );
		}

		return out;
	}


	@Override
	public ItemStack getItemStack()
	{
		return this.i;
	}

	@Override
	public double getIdleDrain()
	{
		return this.cellType.getIdleDrain();
	}

	@Override
	public FuzzyMode getFuzzyMode()
	{
		return this.cellType.getFuzzyMode( this.i );
	}

	@Override
	public IItemHandler getConfigInventory()
	{
		return this.cellType.getConfigInventory( this.i );
	}

	@Override
	public IItemHandler getUpgradesInventory()
	{
		return this.cellType.getUpgradesInventory( this.i );
	}

	@Override
	public int getBytesPerType()
	{
		return this.cellType.getBytesPerType( this.i );
	}

	@Override
	public boolean canHoldNewItem()
	{
		final long bytesFree = this.getFreeBytes();
		return ( bytesFree > this.getBytesPerType() || ( bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0 ) ) && this
				.getRemainingItemTypes() > 0;
	}

	@Override
	public long getTotalBytes()
	{
		return this.cellType.getBytes( this.i );
	}

	@Override
	public long getFreeBytes()
	{
		return this.getTotalBytes() - this.getUsedBytes();
	}

	@Override
	public long getTotalItemTypes()
	{
		return this.maxItemTypes;
	}

	@Override
	public long getStoredItemCount()
	{
		return this.storedItemCount;
	}

	@Override
	public long getStoredItemTypes()
	{
		return this.storedItems;
	}

	@Override
	public long getRemainingItemTypes()
	{
		final long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
		final long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
		return basedOnStorage > baseOnTotal ? baseOnTotal : basedOnStorage;
	}


	@Override
	public long getUsedBytes()
	{
		final long bytesForItemCount = ( this.getStoredItemCount() + this.getUnusedItemCount() ) / itemsPerByte;
		return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
	}

	@Override
	public long getRemainingItemCount()
	{
		final long remaining = this.getFreeBytes() * itemsPerByte + this.getUnusedItemCount();
		return remaining > 0 ? remaining : 0;
	}

	@Override
	public int getUnusedItemCount()
	{
		final int div = (int) ( this.getStoredItemCount() % 8000 );

		if( div == 0 )
		{
			return 0;
		}

		return itemsPerByte - div;
	}

	@Override
	public int getStatusForCell()
	{
		if( this.canHoldNewItem() )
		{
			return 1;
		}
		if( this.getRemainingItemCount() > 0 )
		{
			return 2;
		}
		return 3;
	}
}

