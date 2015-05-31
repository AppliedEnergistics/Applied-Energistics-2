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

package appeng.me.storage;


import java.util.HashSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public final class CellInventory implements ICellInventory
{

	static final String ITEM_TYPE_TAG = "it";
	static final String ITEM_COUNT_TAG = "ic";
	static final String ITEM_SLOT = "#";
	static final String ITEM_SLOT_COUNT = "@";
	static final String ITEM_PRE_FORMATTED_COUNT = "PF";
	static final String ITEM_PRE_FORMATTED_SLOT = "PF#";
	static final String ITEM_PRE_FORMATTED_NAME = "PN";
	static final String ITEM_PRE_FORMATTED_FUZZY = "FP";
	private static final HashSet<Integer> BLACK_LIST = new HashSet<Integer>();
	protected static String[] itemSlots;
	protected static String[] itemSlotCount;
	protected final NBTTagCompound tagCompound;
	protected final ISaveProvider container;
	protected int maxItemTypes = 63;
	protected short storedItems = 0;
	protected int storedItemCount = 0;
	protected IItemList<IAEItemStack> cellItems;
	protected ItemStack i;
	protected IStorageCell CellType;

	protected CellInventory( NBTTagCompound data, ISaveProvider container )
	{
		this.tagCompound = data;
		this.container = container;
	}

	protected CellInventory( ItemStack o, ISaveProvider container ) throws AppEngException
	{
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

		this.CellType = null;
		this.i = o;

		Item type = this.i.getItem();
		if( type instanceof IStorageCell )
		{
			this.CellType = (IStorageCell) this.i.getItem();
			this.maxItemTypes = this.CellType.getTotalTypes( this.i );
		}

		if( this.CellType == null )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		if( !this.CellType.isStorageCell( this.i ) )
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

	public static IMEInventoryHandler getCell( ItemStack o, ISaveProvider container2 )
	{
		try
		{
			return new CellInventoryHandler( new CellInventory( o, container2 ) );
		}
		catch( AppEngException e )
		{
			return null;
		}
	}

	private static boolean isStorageCell( ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		try
		{
			Item type = i.getItem();
			if( type instanceof IStorageCell )
			{
				return !( (IStorageCell) type ).storableInStorageCell();
			}
		}
		catch( Throwable err )
		{
			return true;
		}

		return false;
	}

	public static boolean isCell( ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		Item type = i.getItem();
		if( type instanceof IStorageCell )
		{
			return ( (IStorageCell) type ).isStorageCell( i );
		}

		return false;
	}

	public static void addBasicBlackList( int itemID, int meta )
	{
		BLACK_LIST.add( ( meta << Platform.DEF_OFFSET ) | itemID );
	}

	public static boolean isBlackListed( IAEItemStack input )
	{
		if( BLACK_LIST.contains( ( OreDictionary.WILDCARD_VALUE << Platform.DEF_OFFSET ) | Item.getIdFromItem( input.getItem() ) ) )
		{
			return true;
		}
		return BLACK_LIST.contains( ( input.getItemDamage() << Platform.DEF_OFFSET ) | Item.getIdFromItem( input.getItem() ) );
	}

	private boolean isEmpty( IMEInventory meInventory )
	{
		return meInventory.getAvailableItems( AEApi.instance().storage().createItemList() ).isEmpty();
	}

	@Override
	public final IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		if( input == null )
		{
			return null;
		}
		if( input.getStackSize() == 0 )
		{
			return null;
		}

		if( isBlackListed( input ) || this.CellType.isBlackListed( this.i, input ) )
		{
			return input;
		}

		ItemStack sharedItemStack = input.getItemStack();

		if( CellInventory.isStorageCell( sharedItemStack ) )
		{
			IMEInventory meInventory = getCell( sharedItemStack, null );
			if( meInventory != null && !this.isEmpty( meInventory ) )
			{
				return input;
			}
		}

		IAEItemStack l = this.getCellItems().findPrecise( input );
		if( l != null )
		{
			long remainingItemSlots = this.getRemainingItemCount();
			if( remainingItemSlots < 0 )
			{
				return input;
			}

			if( input.getStackSize() > remainingItemSlots )
			{
				IAEItemStack r = input.copy();
				r.setStackSize( r.getStackSize() - remainingItemSlots );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + remainingItemSlots );
					this.updateItemCount( remainingItemSlots );
					this.saveChanges();
				}
				return r;
			}
			else
			{
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + input.getStackSize() );
					this.updateItemCount( input.getStackSize() );
					this.saveChanges();
				}
				return null;
			}
		}

		if( this.canHoldNewItem() ) // room for new type, and for at least one item!
		{
			int remainingItemCount = (int) this.getRemainingItemCount() - this.getBytesPerType() * 8;
			if( remainingItemCount > 0 )
			{
				if( input.getStackSize() > remainingItemCount )
				{
					ItemStack toReturn = Platform.cloneItemStack( sharedItemStack );
					toReturn.stackSize = sharedItemStack.stackSize - remainingItemCount;
					if( mode == Actionable.MODULATE )
					{
						ItemStack toWrite = Platform.cloneItemStack( sharedItemStack );
						toWrite.stackSize = remainingItemCount;

						this.cellItems.add( AEItemStack.create( toWrite ) );
						this.updateItemCount( toWrite.stackSize );

						this.saveChanges();
					}
					return AEItemStack.create( toReturn );
				}

				if( mode == Actionable.MODULATE )
				{
					this.updateItemCount( input.getStackSize() );
					this.cellItems.add( input );
					this.saveChanges();
				}

				return null;
			}
		}

		return input;
	}

	@Override
	public final IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( request == null )
		{
			return null;
		}

		long size = Math.min( Integer.MAX_VALUE, request.getStackSize() );

		IAEItemStack Results = null;

		IAEItemStack l = this.getCellItems().findPrecise( request );
		if( l != null )
		{
			Results = l.copy();

			if( l.getStackSize() <= size )
			{
				Results.setStackSize( l.getStackSize() );
				if( mode == Actionable.MODULATE )
				{
					this.updateItemCount( -l.getStackSize() );
					l.setStackSize( 0 );
					this.saveChanges();
				}
			}
			else
			{
				Results.setStackSize( size );
				if( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() - size );
					this.updateItemCount( -size );
					this.saveChanges();
				}
			}
		}

		return Results;
	}

	final IItemList<IAEItemStack> getCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = AEApi.instance().storage().createItemList();
			this.loadCellItems();
		}

		return this.cellItems;
	}

	private void updateItemCount( long delta )
	{
		this.storedItemCount += delta;
		this.tagCompound.setInteger( ITEM_COUNT_TAG, this.storedItemCount );
	}

	final void saveChanges()
	{
		// cellItems.clean();
		int itemCount = 0;

		// add new pretty stuff...
		int x = 0;
		for( IAEItemStack v : this.cellItems )
		{
			itemCount += v.getStackSize();

			NBTBase c = this.tagCompound.getTag( itemSlots[x] );
			if( c instanceof NBTTagCompound )
			{
				v.writeToNBT( (NBTTagCompound) c );
			}
			else
			{
				NBTTagCompound g = new NBTTagCompound();
				v.writeToNBT( g );
				this.tagCompound.setTag( itemSlots[x], g );
			}

			/*
			 * NBTBase tagSlotCount = tagCompound.getTag( itemSlotCount[x] ); if ( tagSlotCount instanceof
			 * NBTTagInt ) ((NBTTagInt) tagSlotCount).data = (int) v.getStackSize(); else
			 */
			this.tagCompound.setInteger( itemSlotCount[x], (int) v.getStackSize() );

			x++;
		}

		// NBTBase tagType = tagCompound.getTag( ITEM_TYPE_TAG );
		// NBTBase tagCount = tagCompound.getTag( ITEM_COUNT_TAG );
		short oldStoredItems = this.storedItems;

		/*
		 * if ( tagType instanceof NBTTagShort ) ((NBTTagShort) tagType).data = storedItems = (short) cellItems.size();
		 * else
		 */
		if( this.cellItems.isEmpty() )
		{
			this.tagCompound.removeTag( ITEM_TYPE_TAG );
		}
		else
		{
			this.storedItems = (short) this.cellItems.size();
			this.tagCompound.setShort( ITEM_TYPE_TAG, this.storedItems );
		}

		/*
		 * if ( tagCount instanceof NBTTagInt ) ((NBTTagInt) tagCount).data = storedItemCount = itemCount; else
		 */
		if( itemCount == 0 )
		{
			this.tagCompound.removeTag( ITEM_COUNT_TAG );
		}
		else
		{
			this.storedItemCount = itemCount;
			this.tagCompound.setInteger( ITEM_COUNT_TAG, itemCount );
		}

		// clean any old crusty stuff...
		for(; x < oldStoredItems && x < this.maxItemTypes; x++ )
		{
			this.tagCompound.removeTag( itemSlots[x] );
			this.tagCompound.removeTag( itemSlotCount[x] );
		}

		if( this.container != null )
		{
			this.container.saveChanges( this );
		}
	}

	protected final void loadCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = AEApi.instance().storage().createItemList();
		}

		this.cellItems.resetStatus(); // clears totals and stuff.

		int types = (int) this.getStoredItemTypes();

		for( int x = 0; x < types; x++ )
		{
			ItemStack t = ItemStack.loadItemStackFromNBT( this.tagCompound.getCompoundTag( itemSlots[x] ) );
			if( t != null )
			{
				t.stackSize = this.tagCompound.getInteger( itemSlotCount[x] );

				if( t.stackSize > 0 )
				{
					this.cellItems.add( AEItemStack.create( t ) );
				}
			}
		}

		// cellItems.clean();
	}

	@Override
	public final IItemList getAvailableItems( IItemList out )
	{
		for( IAEItemStack i : this.getCellItems() )
		{
			out.add( i );
		}

		return out;
	}

	@Override
	public final StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public final ItemStack getItemStack()
	{
		return this.i;
	}

	@Override
	public final double getIdleDrain()
	{
		return this.CellType.getIdleDrain();
	}

	@Override
	public final FuzzyMode getFuzzyMode()
	{
		return this.CellType.getFuzzyMode( this.i );
	}

	@Override
	public final IInventory getConfigInventory()
	{
		return this.CellType.getConfigInventory( this.i );
	}

	@Override
	public final IInventory getUpgradesInventory()
	{
		return this.CellType.getUpgradesInventory( this.i );
	}

	@Override
	public final int getBytesPerType()
	{
		return this.CellType.BytePerType( this.i );
	}

	@Override
	public final boolean canHoldNewItem()
	{
		long bytesFree = this.getFreeBytes();
		return ( bytesFree > this.getBytesPerType() || ( bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0 ) ) && this.getRemainingItemTypes() > 0;
	}

	@Override
	public final long getTotalBytes()
	{
		return this.CellType.getBytes( this.i );
	}

	@Override
	public final long getFreeBytes()
	{
		return this.getTotalBytes() - this.getUsedBytes();
	}

	@Override
	public final long getUsedBytes()
	{
		long bytesForItemCount = ( this.getStoredItemCount() + this.getUnusedItemCount() ) / 8;
		return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
	}

	@Override
	public final long getTotalItemTypes()
	{
		return this.maxItemTypes;
	}

	@Override
	public final long getStoredItemCount()
	{
		return this.storedItemCount;
	}

	@Override
	public final long getStoredItemTypes()
	{
		return this.storedItems;
	}

	@Override
	public final long getRemainingItemTypes()
	{
		long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
		long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
		return basedOnStorage > baseOnTotal ? baseOnTotal : basedOnStorage;
	}

	@Override
	public final long getRemainingItemCount()
	{
		long remaining = this.getFreeBytes() * 8 + this.getUnusedItemCount();
		return remaining > 0 ? remaining : 0;
	}

	@Override
	public final int getUnusedItemCount()
	{
		int div = (int) ( this.getStoredItemCount() % 8 );

		if( div == 0 )
		{
			return 0;
		}

		return 8 - div;
	}

	@Override
	public final int getStatusForCell()
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
