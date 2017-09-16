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

package appeng.me.storage;


import java.util.HashSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class CellInventory implements ICellInventory
{

	private static final String ITEM_TYPE_TAG = "it";
	private static final String ITEM_COUNT_TAG = "ic";
	private static final String ITEM_SLOT = "#";
	private static final String ITEM_SLOT_COUNT = "@";
	private static final String ITEM_PRE_FORMATTED_COUNT = "PF";
	private static final String ITEM_PRE_FORMATTED_SLOT = "PF#";
	private static final String ITEM_PRE_FORMATTED_NAME = "PN";
	private static final String ITEM_PRE_FORMATTED_FUZZY = "FP";
	private static final HashSet<Integer> BLACK_LIST = new HashSet<>();
	private static String[] itemSlots;
	private static String[] itemSlotCount;
	private final NBTTagCompound tagCompound;
	private final ISaveProvider container;
	private int maxItemTypes = 63;
	private short storedItems = 0;
	private int storedItemCount = 0;
	private IItemList<IAEItemStack> cellItems;
	private ItemStack i;
	private IStorageCell cellType;

	protected CellInventory( final NBTTagCompound data, final ISaveProvider container )
	{
		this.tagCompound = data;
		this.container = container;
	}

	private CellInventory( final ItemStack o, final ISaveProvider container ) throws AppEngException
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

	public static IMEInventoryHandler getCell( final ItemStack o, final ISaveProvider container2 )
	{
		try
		{
			return new CellInventoryHandler( new CellInventory( o, container2 ) );
		}
		catch( final AppEngException e )
		{
			return null;
		}
	}

	private static boolean isStorageCell( final ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		try
		{
			final Item type = i.getItem();
			if( type instanceof IStorageCell )
			{
				return !( (IStorageCell) type ).storableInStorageCell();
			}
		}
		catch( final Throwable err )
		{
			return true;
		}

		return false;
	}

	public static boolean isCell( final ItemStack i )
	{
		if( i == null )
		{
			return false;
		}

		final Item type = i.getItem();
		if( type instanceof IStorageCell )
		{
			return ( (IStorageCell) type ).isStorageCell( i );
		}

		return false;
	}

	public static void addBasicBlackList( final int itemID, final int meta )
	{
		BLACK_LIST.add( ( meta << Platform.DEF_OFFSET ) | itemID );
	}

	private static boolean isBlackListed( final IAEItemStack input )
	{
		if( BLACK_LIST.contains( ( OreDictionary.WILDCARD_VALUE << Platform.DEF_OFFSET ) | Item.getIdFromItem( input.getItem() ) ) )
		{
			return true;
		}
		return BLACK_LIST.contains( ( input.getItemDamage() << Platform.DEF_OFFSET ) | Item.getIdFromItem( input.getItem() ) );
	}

	private boolean isEmpty( final IMEInventory meInventory )
	{
		return meInventory.getAvailableItems( AEApi.instance().storage().createItemList() ).isEmpty();
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final IActionSource src )
	{
		if( input == null )
		{
			return null;
		}
		if( input.getStackSize() == 0 )
		{
			return null;
		}

		if( isBlackListed( input ) || this.cellType.isBlackListed( this.i, input ) )
		{
			return input;
		}

		final ItemStack sharedItemStack = input.createItemStack();

		if( CellInventory.isStorageCell( sharedItemStack ) )
		{
			final IMEInventory meInventory = getCell( sharedItemStack, null );
			if( meInventory != null && !this.isEmpty( meInventory ) )
			{
				return input;
			}
		}

		final IAEItemStack l = this.getCellItems().findPrecise( input );
		if( l != null )
		{
			final long remainingItemSlots = this.getRemainingItemCount();
			if( remainingItemSlots < 0 )
			{
				return input;
			}

			if( input.getStackSize() > remainingItemSlots )
			{
				final IAEItemStack r = input.copy();
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
			final int remainingItemCount = (int) this.getRemainingItemCount() - this.getBytesPerType() * 8;
			if( remainingItemCount > 0 )
			{
				if( input.getStackSize() > remainingItemCount )
				{
					final ItemStack toReturn = sharedItemStack.copy();
					toReturn.setCount( sharedItemStack.getCount() - remainingItemCount );
					if( mode == Actionable.MODULATE )
					{
						final ItemStack toWrite = sharedItemStack.copy();
						toWrite.setCount( remainingItemCount );

						this.cellItems.add( AEItemStack.create( toWrite ) );
						this.updateItemCount( toWrite.getCount() );

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
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final IActionSource src )
	{
		if( request == null )
		{
			return null;
		}

		final long size = Math.min( Integer.MAX_VALUE, request.getStackSize() );

		IAEItemStack Results = null;

		final IAEItemStack l = this.getCellItems().findPrecise( request );
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

	IItemList<IAEItemStack> getCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = AEApi.instance().storage().createItemList();
			this.loadCellItems();
		}

		return this.cellItems;
	}

	private void updateItemCount( final long delta )
	{
		this.storedItemCount += delta;
		this.tagCompound.setInteger( ITEM_COUNT_TAG, this.storedItemCount );
	}

	void saveChanges()
	{
		// cellItems.clean();
		int itemCount = 0;

		// add new pretty stuff...
		int x = 0;
		for( final IAEItemStack v : this.cellItems )
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

	protected void loadCellItems()
	{
		if( this.cellItems == null )
		{
			this.cellItems = AEApi.instance().storage().createItemList();
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

	private void loadCellItem( NBTTagCompound compoundTag, int stackSize )
	{

		// Now load the item stack
		final ItemStack t;
		try
		{
			t = new ItemStack( compoundTag );
			if( t.isEmpty() )
			{
				AELog.warn( "Removing item " + compoundTag + " from storage cell because the associated item type couldn't be found." );
				return;
			}
		}
		catch( Throwable ex )
		{
			if( AEConfig.instance().isRemoveCrashingItemsOnLoad() )
			{
				AELog.warn( ex, "Removing item " + compoundTag + " from storage cell because loading the ItemStack crashed." );
				return;
			}
			throw ex;
		}

		t.setCount( stackSize );

		if( t.getCount() > 0 )
		{
			try
			{
				this.cellItems.add( AEItemStack.create( t ) );
			}
			catch( Throwable ex )
			{
				if( AEConfig.instance().isRemoveCrashingItemsOnLoad() )
				{
					AELog.warn( ex, "Removing item " + t + " from storage cell because processing the loaded item crashed." );
					return;
				}
				throw ex;
			}
		}
	}

	@Override
	public IItemList getAvailableItems( final IItemList out )
	{
		for( final IAEItemStack i : this.getCellItems() )
		{
			out.add( i );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
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
	public long getUsedBytes()
	{
		final long bytesForItemCount = ( this.getStoredItemCount() + this.getUnusedItemCount() ) / 8;
		return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
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
	public long getRemainingItemCount()
	{
		final long remaining = this.getFreeBytes() * 8 + this.getUnusedItemCount();
		return remaining > 0 ? remaining : 0;
	}

	@Override
	public int getUnusedItemCount()
	{
		final int div = (int) ( this.getStoredItemCount() % 8 );

		if( div == 0 )
		{
			return 0;
		}

		return 8 - div;
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
